package pl.wataha.app.data.repo

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import pl.wataha.app.comm.CommManager
import pl.wataha.app.comm.CommResult
import pl.wataha.app.comm.CommPayload
import pl.wataha.app.data.db.AppDao
import pl.wataha.app.data.db.Badges
import pl.wataha.app.data.model.BadgeEntity
import pl.wataha.app.data.model.CrisisSignalEntity
import pl.wataha.app.data.model.ExchangeEntity
import pl.wataha.app.data.model.ExchangeStatus
import pl.wataha.app.data.model.FeedbackEntity
import pl.wataha.app.data.model.GeoMarkerEntity
import pl.wataha.app.data.model.MessageEntity
import pl.wataha.app.data.model.NetworkNodeEntity
import pl.wataha.app.data.model.PendingSyncEntity
import pl.wataha.app.data.model.PostEntity
import pl.wataha.app.data.model.PostStatus
import pl.wataha.app.data.model.PostType
import pl.wataha.app.data.model.ThreadEntity
import pl.wataha.app.data.model.UserEntity
import pl.wataha.app.data.seed.SeedData
import pl.wataha.app.service.NotificationHelper
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Repository — jedyne źródło danych dla UI (offline-first).
 * Wszystkie zapisy trafiają najpierw do lokalnej bazy Room; przy braku internetu
 * dodatkowo do kolejki synchronizacji (pendings), wysyłanej później przez InternetAdapter.
 */
class Repository(
    private val ctx: Context,
    private val dao: AppDao,
    val comm: CommManager
) {

    private val prefs: SharedPreferences = ctx.getSharedPreferences("wataha_prefs", Context.MODE_PRIVATE)

    private val _me = MutableStateFlow<UserEntity?>(null)
    val me: StateFlow<UserEntity?> = _me

    val online: StateFlow<Boolean> = comm.online
    val forceOffline: StateFlow<Boolean> = comm.forceOffline

    val posts: Flow<List<PostEntity>> = dao.postsFlow()
    val users: Flow<List<UserEntity>> = dao.usersFlow()
    val threads: Flow<List<ThreadEntity>> = dao.threadsFlow()
    val exchanges: Flow<List<ExchangeEntity>> = dao.exchangesFlow()
    val crisis: Flow<List<CrisisSignalEntity>> = dao.crisisFlow()
    val nodes: Flow<List<NetworkNodeEntity>> = dao.nodesFlow()
    val markers: Flow<List<GeoMarkerEntity>> = dao.markersFlow()
    val pendingCount: Flow<Int> = dao.pendingCountFlow()

    /** true gdy baza jest zainicjalizowana (seed + sesja) — po tym można pokazać główny ekran */
    val ready = MutableStateFlow(false)

    fun badges(uid: String): Flow<List<BadgeEntity>> = dao.badgesFlow(uid)

    suspend fun init() {
        SeedData.seedIfEmpty(dao)
        val uid = prefs.getString("session_uid", null)
        if (uid != null) {
            _me.value = dao.findUserById(uid)
        }
        ready.value = true
    }

    val apiBase: String get() = comm.apiBase
    fun setApiBase(url: String) = comm.setApiBase(url)

    fun setForceOffline(v: Boolean) = comm.setForceOffline(v)

    // ---------------- AUTH ----------------

    suspend fun register(displayName: String, username: String, phone: String, password: String): Result<UserEntity> {
        if (username.isBlank() || displayName.isBlank() || password.length < 4) {
            return Result.failure(IllegalArgumentException("Uzupełnij dane (hasło min. 4 znaki)."))
        }
        if (dao.findUserByUsername(username.trim().lowercase()) != null) {
            return Result.failure(IllegalArgumentException("Ta nazwa użytkownika jest już zajęta."))
        }
        val user = UserEntity(
            id = "u_" + UUID.randomUUID().toString().substring(0, 8),
            username = username.trim().lowercase(),
            displayName = displayName.trim(),
            phone = phone.trim(),
            passwordHash = password,
            bio = "Członek Watahy — ${displayName.trim()}.",
            reputation = 3.0,
            isDemo = false,
            lat = myLocation().first,
            lng = myLocation().second,
            lastSeen = System.currentTimeMillis()
        )
        dao.upsertUser(user)
        ackBadge(user.id, "ORZEL")
        enqueue("users", "UPSERT", userJson(user))
        setSession(user)
        return Result.success(user)
    }

    suspend fun login(username: String, password: String): Result<UserEntity> {
        val local = dao.findUserByUsername(username.trim().lowercase())
        if (local != null) {
            if (local.isDemo || local.passwordHash == password) {
                setSession(local)
                return Result.success(local)
            }
            return Result.failure(IllegalArgumentException("Błędne hasło."))
        }
        // Próba logowania do backendu (jeśli dostępny)
        if (online.value) {
            try {
                val res = comm.internet.request(
                    "POST", "/api/auth/login",
                    JSONObject().put("username", username.trim()).put("password", password)
                )
                if (res.code in 200..299) {
                    val j = JSONObject(res.body).optJSONObject("user")
                    if (j != null) {
                        val u = UserEntity(
                            id = j.optString("id", "u_" + UUID.randomUUID().toString().substring(0, 8)),
                            username = j.optString("username", username),
                            displayName = j.optString("displayName", username),
                            phone = j.optString("phone", ""),
                            bio = j.optString("bio", ""),
                            reputation = j.optDouble("reputation", 3.0),
                            badges = j.optString("badges", ""),
                            lastSeen = System.currentTimeMillis()
                        )
                        dao.upsertUser(u)
                        setSession(u)
                        return Result.success(u)
                    }
                }
            } catch (_: Exception) {
            }
        }
        return Result.failure(IllegalArgumentException("Nie znaleziono konta „$username”."))
    }

    suspend fun registerFromServerIfPossible() = Unit

    fun setSession(u: UserEntity) {
        prefs.edit().putString("session_uid", u.id).apply()
        _me.value = u
    }

    fun logout() {
        prefs.edit().remove("session_uid").apply()
        _me.value = null
    }

    suspend fun refreshMe() {
        val uid = _me.value?.id ?: return
        _me.value = dao.findUserById(uid)
    }

    suspend fun getUser(uid: String): UserEntity? = dao.findUserById(uid)

    // ---------------- OGŁOSZENIA ----------------

    suspend fun createPost(type: PostType, title: String, body: String, imagePath: String, lat: Double, lng: Double): PostEntity {
        val me = _me.value ?: throw IllegalStateException("Brak zalogowania")
        val post = PostEntity(
            id = "p_" + UUID.randomUUID().toString().substring(0, 10),
            authorId = me.id,
            authorName = me.displayName,
            type = type.name,
            title = title,
            body = body,
            imagePath = imagePath,
            lat = lat,
            lng = lng,
            createdAt = System.currentTimeMillis(),
            status = PostStatus.OPEN,
            pending = !online.value
        )
        dao.upsertPost(post)
        val cnt = dao.myPostCount(me.id)
        if (cnt == 1) ackBadge(me.id, "PIERWSZA_SZARZA")
        if (cnt == 5) ackBadge(me.id, "WILK")
        if (type == PostType.OFFER) ackBadge(me.id, "SOLIDARNOSC")
        enqueue("posts", "UPSERT", postJson(post))
        return post
    }

    suspend fun closePost(id: String) = dao.setPostStatus(id, PostStatus.CLOSED)

    suspend fun post(id: String): PostEntity? = dao.postById(id)

    // ---------------- WIADOMOŚCI ----------------

    fun messages(threadId: String): Flow<List<MessageEntity>> = dao.messagesFlow(threadId)
    suspend fun thread(threadId: String): ThreadEntity? = dao.threadById(threadId)

    suspend fun sendMessage(to: UserEntity, text: String): MessageEntity {
        val me = _me.value ?: throw IllegalStateException("Brak zalogowania")
        val threadId = "t_" + listOf(me.id, to.id).sorted().joinToString("_")
        val msg = MessageEntity(
            id = "m_" + UUID.randomUUID().toString().substring(0, 10),
            threadId = threadId,
            fromId = me.id,
            toId = to.id,
            text = text,
            createdAt = System.currentTimeMillis(),
            pending = !online.value
        )
        dao.upsertMessage(msg)
        dao.upsertThread(
            ThreadEntity(
                threadId = threadId, userId = to.id, userName = to.displayName,
                lastText = text, lastAt = msg.createdAt, unread = 0
            )
        )
        ackBadge(me.id, "KOTWICA")
        val cnt = dao.myMessageCount(me.id)
        if (cnt == 10) ackBadge(me.id, "WETERAN")
        enqueue("messages", "UPSERT", messageJson(msg))
        return msg
    }

    /** DEMO: symulowana odpowiedź rozmówcy (wiadomość + powiadomienie w kanale Wiadomości). */
    suspend fun simulateReply(threadId: String) {
        val me = _me.value ?: return
        val t = dao.threadById(threadId) ?: return
        val responses = listOf(
            "Przyjąłem. Daj znać, jak coś się zmieni.",
            "Dzięki za info! Przekazuję dalej wataham.",
            "OK, mogę pomóc — napisz godzinę.",
            "Rozumiem. Trzymaj się, wataha czuwa! 🇵🇱"
        )
        val text = responses[(t.lastAt % responses.size).toInt()]
        val msg = MessageEntity(
            id = "m_" + UUID.randomUUID().toString().substring(0, 10),
            threadId = threadId,
            fromId = t.userId,
            toId = me.id,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        dao.upsertMessage(msg)
        dao.upsertThread(t.copy(lastText = text, lastAt = msg.createdAt, unread = t.unread + 1))
        NotificationHelper.notify(
            NotificationHelper.CHAT, 1001, "Wiadomość od ${t.userName}", text,
            bigText = text
        )
    }

    suspend fun markThreadRead(threadId: String) {
        dao.markThreadRead(threadId)
        dao.resetUnread(threadId)
    }

    // ---------------- WYMIANY ----------------

    suspend fun proposeExchange(post: PostEntity, message: String): ExchangeEntity {
        val me = _me.value ?: throw IllegalStateException("Brak zalogowania")
        val ex = ExchangeEntity(
            id = "e_" + UUID.randomUUID().toString().substring(0, 10),
            postId = post.id,
            postTitle = post.title,
            proposerId = me.id,
            proposerName = me.displayName,
            ownerId = post.authorId,
            message = message,
            status = ExchangeStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            pending = !online.value
        )
        dao.upsertExchange(ex)
        enqueue("exchanges", "UPSERT", exchangeJson(ex))
        NotificationHelper.notify(
            NotificationHelper.EXCHANGE, 2001, "Propozycja wymiany wysłana",
            "„${post.title}” — czekamy na decyzję właściciela."
        )
        val cnt = dao.exchangesFlow().first().count { it.proposerId == me.id && it.status != ExchangeStatus.REJECTED }
        if (cnt >= 3) ackBadge(me.id, "GONIEC")
        return ex
    }

    suspend fun respondExchange(exchangeId: String, accept: Boolean) {
        dao.setExchangeStatus(exchangeId, if (accept) ExchangeStatus.ACCEPTED else ExchangeStatus.REJECTED)
        val ex = dao.exchangesFlow().first().firstOrNull { it.id == exchangeId } ?: return
        if (accept) {
            dao.setPostStatus(ex.postId, PostStatus.CLOSED)
            NotificationHelper.notify(
                NotificationHelper.EXCHANGE, 2002, "Wymiana przyjęta ✅",
                "„${ex.postTitle}” — uzgodnij szczegóły w wiadomościach."
            )
        } else {
            NotificationHelper.notify(
                NotificationHelper.EXCHANGE, 2003, "Wymiana odrzucona",
                "„${ex.postTitle}” — propozycja nie została przyjęta."
            )
        }
    }

    /** DEMO: symulowana odpowiedź właściciela ogłoszenia (kanał Wymiany). */
    suspend fun simulateIncomingAccept(exchangeId: String) {
        val ex = dao.exchangesFlow().first().firstOrNull { it.id == exchangeId } ?: return
        dao.setExchangeStatus(exchangeId, ExchangeStatus.ACCEPTED)
        dao.setPostStatus(ex.postId, PostStatus.CLOSED)
        NotificationHelper.notify(
            NotificationHelper.EXCHANGE, 2004, "Właściciel przyjął wymianę ✅",
            "„${ex.postTitle}” — kontakt: napisz wiadomość, aby ustalić odbiór.",
            bigText = "Wymiana przyjęta. Umów się na odbiór w wątku wiadomości."
        )
    }

    // ---------------- POMOC / KRYZYS ----------------

    suspend fun reportCrisis(type: String, text: String): CrisisSignalEntity {
        val me = _me.value ?: throw IllegalStateException("Brak zalogowania")
        val loc = myLocation()
        val sig = CrisisSignalEntity(
            id = "c_" + UUID.randomUUID().toString().substring(0, 10),
            userId = me.id,
            userName = me.displayName,
            type = type,
            text = text,
            lat = loc.first,
            lng = loc.second,
            createdAt = System.currentTimeMillis(),
            pending = !online.value
        )
        dao.upsertCrisis(sig)
        ackBadge(me.id, "STRAZNIK")
        enqueue("crisis_signals", "UPSERT", crisisJson(sig))
        NotificationHelper.notify(
            NotificationHelper.CRISIS, 3001, "🚨 SYGNAŁ KRYZYSOWY WYSŁANY",
            "Wataha została powiadomiona. Trzymaj się — jesteśmy z Tobą.",
            bigText = "Sygnał „$type” przesłany do sieci. W trybie offline trafi do kolejki i zostanie wysłany po odzyskaniu łączności.",
            fullScreen = true
        )
        return sig
    }

    // ---------------- ODZNAKI ----------------

    private suspend fun ackBadge(uid: String, code: String) {
        if (dao.badgeCount(uid, code) > 0) return
        val def = Badges.byCode(code) ?: return
        val b = BadgeEntity(
            id = "b_" + UUID.randomUUID().toString().substring(0, 10),
            userId = uid, code = def.code, name = def.name, desc = def.desc,
            emoji = def.emoji, earnedAt = System.currentTimeMillis()
        )
        dao.insertBadge(b)
        try {
            val u = dao.findUserById(uid) ?: return
            val merged = (u.badges.split(",") + def.code).filter { it.isNotBlank() }.distinct().joinToString(",")
            dao.upsertUser(u.copy(badges = merged))
        } catch (_: Exception) {
        }
        NotificationHelper.notify(
            NotificationHelper.LOCAL, 4001, "Nowa odznaka: ${def.emoji} ${def.name}",
            "${def.desc} — ${if (uid == _me.value?.id) "gratulacje! 💪" else ""}"
        )
    }

    // ---------------- POMYSŁY → ADMINISTRACJA ----------------

    fun myFeedback(uid: String): Flow<List<FeedbackEntity>> = dao.myFeedbackFlow(uid)

    suspend fun submitFeedback(subject: String, text: String): FeedbackEntity {
        val me = _me.value ?: throw IllegalStateException("Brak zalogowania")
        val f = FeedbackEntity(
            id = "f_" + UUID.randomUUID().toString().substring(0, 10),
            userId = me.id,
            userName = me.displayName,
            subject = subject,
            text = text,
            createdAt = System.currentTimeMillis(),
            pending = !online.value
        )
        dao.upsertFeedback(f)
        enqueue("feedback", "UPSERT", feedbackJson(f))
        NotificationHelper.notify(
            NotificationHelper.LOCAL, 5001, "💡 Zgłoszenie wysłane do Administracji",
            "„$subject” — dziękujemy, wataha czyta każdy pomysł!"
        )
        return f
    }

    // ---------------- LOKALIZACJA ----------------

    fun myLocation(): Pair<Double, Double> {
        val lat = prefs.getFloat("my_lat", 0f)
        val lng = prefs.getFloat("my_lng", 0f)
        return if (lat != 0f && lng != 0f) lat.toDouble() to lng.toDouble()
        else 52.2297 to 21.0122 // Warszawa (domyślna tylko w trybie demo/GPS off)
    }

    fun setMyLocation(lat: Double, lng: Double) {
        prefs.edit().putFloat("my_lat", lat.toFloat()).putFloat("my_lng", lng.toFloat()).apply()
    }

    fun distanceKm(lat: Double, lng: Double): Float {
        val (mylat, mylng) = myLocation()
        val r = 6371.0
        val dLat = Math.toRadians(lat - mylat)
        val dLng = Math.toRadians(lng - mylng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(mylat)) * cos(Math.toRadians(lat)) * sin(dLng / 2) * sin(dLng / 2)
        return (2 * r * atan2(sqrt(a), sqrt(1 - a))).toFloat()
    }

    // ---------------- SYNCHRONIZACJA ----------------

    private suspend fun enqueue(entity: String, operation: String, payload: JSONObject) {
        val p = PendingSyncEntity(
            entity = entity,
            operation = operation,
            payload = payload.toString(),
            createdAt = System.currentTimeMillis()
        )
        dao.insertPending(p)
        if (online.value) pushRemote(p) // natychmiastowa próba
    }

    private suspend fun pushRemote(p: PendingSyncEntity): Boolean {
        val route = when (p.entity) {
            "users" -> "/api/users"
            "posts" -> "/api/posts"
            "messages" -> "/api/messages"
            "exchanges" -> "/api/exchanges"
            "crisis_signals" -> "/api/crisis"
            "badges" -> "/api/badges"
            "nodes" -> "/api/nodes"
            "markers" -> "/api/markers"
            "feedback" -> "/api/feedback"
            else -> return false
        }
        return try {
            val data = JSONObject(p.payload)
            val res = comm.internet.request("POST", route, JSONObject().put("data", data))
            if (res.code in 200..299) {
                dao.deletePending(p)
                true
            } else {
                dao.bumpTries(p.id)
                false
            }
        } catch (e: Exception) {
            dao.bumpTries(p.id)
            false
        }
    }

    /** Ręczna / automatyczna synchronizacja kolejki. Zwraca liczbę przesłanych rekordów. */
    suspend fun syncNow(): Int {
        if (!online.value) return 0
        val list = dao.pendingFlow().first()
        var ok = 0
        for (p in list) {
            if (pushRemote(p)) ok++
        }
        if (ok > 0) {
            NotificationHelper.notify(
                NotificationHelper.LOCAL, 4002, "Synchronizacja zakończona",
                "Wysłano $ok rekordów do backendu."
            )
        }
        return ok
    }

    // ---------------- JSON ----------------

    private fun userJson(u: UserEntity) = JSONObject()
        .put("id", u.id).put("username", u.username).put("displayName", u.displayName)
        .put("phone", u.phone).put("bio", u.bio).put("reputation", u.reputation)
        .put("badges", u.badges).put("isDemo", u.isDemo).put("lat", u.lat).put("lng", u.lng)

    private fun postJson(p: PostEntity) = JSONObject()
        .put("id", p.id).put("authorId", p.authorId).put("authorName", p.authorName)
        .put("type", p.type).put("title", p.title).put("body", p.body)
        .put("imagePath", p.imagePath).put("lat", p.lat).put("lng", p.lng)
        .put("createdAt", p.createdAt).put("status", p.status)

    private fun messageJson(m: MessageEntity) = JSONObject()
        .put("id", m.id).put("threadId", m.threadId).put("fromId", m.fromId)
        .put("toId", m.toId).put("text", m.text).put("createdAt", m.createdAt)

    private fun exchangeJson(e: ExchangeEntity) = JSONObject()
        .put("id", e.id).put("postId", e.postId).put("postTitle", e.postTitle)
        .put("proposerId", e.proposerId).put("proposerName", e.proposerName)
        .put("ownerId", e.ownerId).put("message", e.message).put("status", e.status)
        .put("createdAt", e.createdAt)

    private fun crisisJson(c: CrisisSignalEntity) = JSONObject()
        .put("id", c.id).put("userId", c.userId).put("userName", c.userName)
        .put("type", c.type).put("text", c.text).put("lat", c.lat).put("lng", c.lng)
        .put("createdAt", c.createdAt)

    private fun feedbackJson(f: FeedbackEntity) = JSONObject()
        .put("id", f.id).put("userId", f.userId).put("userName", f.userName)
        .put("subject", f.subject).put("text", f.text).put("createdAt", f.createdAt)

    /** Test kanału internet — wysyłka sygnału ping przez InternetAdapter. */
    suspend fun pingServer(): Pair<Boolean, String> {
        if (!online.value) return false to "Offline — test niemożliwy (kolejka przechowa dane)."
        val res = comm.internet.send(CommPayload(from = _me.value?.id ?: "app", to = "server", type = "PING", body = "ping"))
        return when (res) {
            is CommResult.Ok -> true to "Internet: odpowiedź ${res.detail}"
            is CommResult.Fail -> false to res.reason
        }
    }
}
