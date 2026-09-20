import Foundation
import Combine

/// Repository — jedyne źródło danych dla UI (offline-first). Port Android Repository.
@MainActor
final class Repo: ObservableObject {
    let db: Database
    let comm: CommManager

    @Published private(set) var me: User?
    @Published private(set) var users: [User] = []
    @Published private(set) var posts: [Post] = []
    @Published private(set) var threads: [ThreadRow] = []
    @Published private(set) var exchanges: [Exchange] = []
    @Published private(set) var crisis: [CrisisSignal] = []
    @Published private(set) var nodes: [NetworkNode] = []
    @Published private(set) var markers: [GeoMarker] = []
    @Published private(set) var feedback: [FeedbackItem] = []
    @Published private(set) var pendingCount: Int = 0
    @Published var ready = false

    var online: Bool { comm.online }
    var forceOffline: Bool { comm.forceOffline }

    init(db: Database, comm: CommManager) {
        self.db = db
        self.comm = comm
    }

    // ---------- inicjalizacja ----------
    func initAsync() {
        Seed.seedIfEmpty(db)
        reloadAll()
        let uid = UserDefaults.standard.string(forKey: "session_uid")
        if let uid { me = db.findUserById(uid) }
        ready = true
    }

    private func reloadAll() {
        users = db.allUsers()
        posts = db.allPosts()
        threads = db.allThreads()
        exchanges = db.allExchanges()
        crisis = db.allCrisis()
        nodes = db.allNodes()
        markers = db.allMarkers()
        if let me { feedback = db.feedbackFor(me.id) }
        pendingCount = db.pendingCount()
    }

    // ---------- AUTH ----------
    func register(displayName: String, username: String, phone: String, password: String) -> Result<User, Error> {
        if username.trimmingCharacters(in: .whitespaces).isEmpty ||
            displayName.trimmingCharacters(in: .whitespaces).isEmpty || password.count < 4 {
            return .failure(NSError(domain: "wataha", code: 1, userInfo: [NSLocalizedDescriptionKey: "Uzupełnij dane (hasło min. 4 znaki)."]))
        }
        if db.findUserByUsername(username.trimmingCharacters(in: .whitespaces).lowercased()) != nil {
            return .failure(NSError(domain: "wataha", code: 2, userInfo: [NSLocalizedDescriptionKey: "Ta nazwa użytkownika jest już zajęta."]))
        }
        let loc = myLocation()
        let user = User(id: uuid("u"), username: username.trimmingCharacters(in: .whitespaces).lowercased(),
                        displayName: displayName.trimmingCharacters(in: .whitespaces), phone: phone.trimmingCharacters(in: .whitespaces),
                        passwordHash: password, bio: "Członek Watahy — \(displayName.trimmingCharacters(in: .whitespaces)).",
                        reputation: 3.0, isDemo: false, lat: loc.0, lng: loc.1, lastSeen: nowMs())
        db.upsertUser(user)
        ackBadge(uid: user.id, code: "ORZEL")
        enqueue(entity: "users", json: userJson(user))
        setSession(user)
        reloadAll()
        return .success(user)
    }

    func login(username: String, password: String) async -> Result<User, Error> {
        if let local = db.findUserByUsername(username.trimmingCharacters(in: .whitespaces).lowercased()) {
            if local.isDemo || local.passwordHash == password {
                setSession(local)
                return .success(local)
            }
            return .failure(NSError(domain: "wataha", code: 3, userInfo: [NSLocalizedDescriptionKey: "Błędne hasło."]))
        }
        if online {
            do {
                let (code, body) = try await comm.internet.request(method: "POST", path: "/api/auth/login",
                                                                   json: ["username": username.trimmingCharacters(in: .whitespaces), "password": password])
                if (200...299).contains(code), let data = body.data(using: .utf8),
                   let obj = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let u = obj["user"] as? [String: Any] {
                    let user = User(id: u["id"] as? String ?? uuid("u"),
                                    username: u["username"] as? String ?? username,
                                    displayName: u["displayName"] as? String ?? username,
                                    phone: u["phone"] as? String ?? "",
                                    bio: u["bio"] as? String ?? "",
                                    reputation: u["reputation"] as? Double ?? 3.0,
                                    badges: u["badges"] as? String ?? "",
                                    lastSeen: nowMs())
                    db.upsertUser(user)
                    setSession(user)
                    reloadAll()
                    return .success(user)
                }
            } catch {}
        }
        return .failure(NSError(domain: "wataha", code: 4, userInfo: [NSLocalizedDescriptionKey: "Nie znaleziono konta „\(username)”."]))
    }

    func setSession(_ u: User) {
        UserDefaults.standard.set(u.id, forKey: "session_uid")
        me = u
        feedback = db.feedbackFor(u.id)
    }

    func logout() {
        UserDefaults.standard.removeObject(forKey: "session_uid")
        me = nil
        feedback = []
    }

    func getUser(_ uid: String) -> User? { db.findUserById(uid) }
    func reviews(_ uid: String) -> [Badge] { db.badgesFor(uid) }

    func setForceOffline(_ v: Bool) { comm.setForceOffline(v) }

    // ---------- OGŁOSZENIA ----------
    func createPost(type: PostType, title: String, body: String, imagePath: String, lat: Double, lng: Double) throws -> Post {
        guard let me else { throw NSError(domain: "wataha", code: 5, userInfo: [NSLocalizedDescriptionKey: "Brak zalogowania"]) }
        let p = Post(id: uuid("p"), authorId: me.id, authorName: me.displayName, type: type.rawValue,
                     title: title, body: body, imagePath: imagePath, lat: lat, lng: lng,
                     createdAt: nowMs(), status: PostStatus.OPEN, pending: !online)
        db.upsertPost(p)
        reloadAll()
        let cnt = db.countMyPosts(me.id)
        if cnt == 1 { ackBadge(uid: me.id, code: "PIERWSZA_SZARZA") }
        if cnt == 5 { ackBadge(uid: me.id, code: "WILK") }
        if type == .OFFER { ackBadge(uid: me.id, code: "SOLIDARNOSC") }
        enqueue(entity: "posts", json: postJson(p))
        return p
    }

    func closePost(_ id: String) { db.setPostStatus(id, PostStatus.CLOSED); reloadAll() }
    func post(_ id: String) -> Post? { db.postById(id) }

    // ---------- WIADOMOŚCI ----------
    func messages(_ threadId: String) -> [Message] { db.threadMessages(threadId) }
    func thread(_ tid: String) -> ThreadRow? { db.threadById(tid) }

    func sendMessage(to: User, text: String) throws {
        guard let me else { throw NSError(domain: "wataha", code: 5, userInfo: [NSLocalizedDescriptionKey: "Brak zalogowania"]) }
        let threadId = "t_" + [me.id, to.id].sorted().joined(separator: "_")
        let m = Message(id: uuid("m"), threadId: threadId, fromId: me.id, toId: to.id, text: text,
                        createdAt: nowMs(), pending: !online)
        db.upsertMessage(m)
        db.upsertThread(ThreadRow(threadId: threadId, userId: to.id, userName: to.displayName, lastText: text, lastAt: m.createdAt, unread: 0))
        ackBadge(uid: me.id, code: "KOTWICA")
        if db.countMyMessages(me.id) == 10 { ackBadge(uid: me.id, code: "WETERAN") }
        reloadAll()
        enqueue(entity: "messages", json: messageJson(m))
    }

    /// DEMO: symulowana odpowiedź rozmówcy (+ powiadomienie).
    func simulateReply(threadId: String) {
        guard let me, let t = db.threadById(threadId) else { return }
        let responses = ["Przyjąłem. Daj znać, jak coś się zmieni.",
                         "Dzięki za info! Przekazuję dalej wataham.",
                         "OK, mogę pomóc — napisz godzinę.",
                         "Rozumiem. Trzymaj się, wataha czuwa! 🇵🇱"]
        let text = responses[Int(t.lastAt % Int64(responses.count))]
        let m = Message(id: uuid("m"), threadId: threadId, fromId: t.userId, toId: me.id, text: text,
                        createdAt: nowMs())
        db.upsertMessage(m)
        db.upsertThread(ThreadRow(threadId: t.threadId, userId: t.userId, userName: t.userName, lastText: text, lastAt: m.createdAt, unread: t.unread + 1))
        reloadAll()
        NotificationHelper.show(channel: .chat, id: 1001, title: "Wiadomość od \(t.userName)", body: text)
    }

    func markThreadRead(_ threadId: String) { db.markThreadRead(threadId); reloadAll() }

    // ---------- WYMIANY ----------
    func proposeExchange(post: Post, message: String) throws {
        guard let me else { throw NSError(domain: "wataha", code: 5, userInfo: [NSLocalizedDescriptionKey: "Brak zalogowania"]) }
        let e = Exchange(id: uuid("e"), postId: post.id, postTitle: post.title, proposerId: me.id,
                         proposerName: me.displayName, ownerId: post.authorId, message: message,
                         status: ExchangeStatus.PENDING, createdAt: nowMs(), pending: !online)
        db.upsertExchange(e)
        reloadAll()
        enqueue(entity: "exchanges", json: exchangeJson(e))
        NotificationHelper.show(channel: .exchange, id: 2001, title: "Propozycja wymiany wysłana", body: "„\(post.title)” — czekamy na decyzję właściciela.")
        let completed = db.allExchanges().filter { $0.proposerId == me.id && $0.status != ExchangeStatus.REJECTED }.count
        if completed >= 3 { ackBadge(uid: me.id, code: "GONIEC") }
    }

    func respondExchange(id: String, accept: Bool) {
        let status = accept ? ExchangeStatus.ACCEPTED : ExchangeStatus.REJECTED
        db.setExchangeStatus(id, status)
        if let e = db.allExchanges().first(where: { $0.id == id }) {
            if accept {
                db.setPostStatus(e.postId, PostStatus.CLOSED)
                NotificationHelper.show(channel: .exchange, id: 2002, title: "Wymiana przyjęta ✅", body: "„\(e.postTitle)” — uzgodnij szczegóły w wiadomościach.")
            } else {
                NotificationHelper.show(channel: .exchange, id: 2003, title: "Wymiana odrzucona", body: "„\(e.postTitle)” — propozycja nie została przyjęta.")
            }
        }
        reloadAll()
    }

    /// DEMO: symulowana odpowiedź właściciela ogłoszenia.
    func simulateIncomingAccept(_ id: String) {
        guard let e = db.allExchanges().first(where: { $0.id == id }) else { return }
        db.setExchangeStatus(id, ExchangeStatus.ACCEPTED)
        db.setPostStatus(e.postId, PostStatus.CLOSED)
        reloadAll()
        NotificationHelper.show(channel: .exchange, id: 2004, title: "Właściciel przyjął wymianę ✅", body: "„\(e.postTitle)” — umów odbiór w wiadomościach.")
    }

    // ---------- KRYZYS ----------
    @discardableResult
    func reportCrisis(type: String, text: String) -> CrisisSignal? {
        guard let me else { return nil }
        let loc = myLocation()
        let c = CrisisSignal(id: uuid("c"), userId: me.id, userName: me.displayName, type: type,
                             text: text, lat: loc.0, lng: loc.1, createdAt: nowMs(), pending: !online)
        db.upsertCrisis(c)
        ackBadge(uid: me.id, code: "STRAZNIK")
        reloadAll()
        enqueue(entity: "crisis_signals", json: crisisJson(c))
        NotificationHelper.show(channel: .crisis, id: 3001, title: "🚨 SYGNAŁ KRYZYSOWY WYSŁANY",
                                body: "Sygnał „\(type)” przesłany do sieci. W trybie offline trafi do kolejki i zostanie wysłany po odzyskaniu łączności.",
                                fullScreen: true)
        return c
    }

    // ---------- ODZNAKI ----------
    private func ackBadge(uid: String, code: String) {
        guard db.badgeCount(uid, code) == 0, let def = Badges.byCode(code) else { return }
        db.insertBadge(Badge(id: uuid("b"), userId: uid, code: def.code, name: def.name, desc: def.desc, emoji: def.emoji, earnedAt: nowMs()))
        if let u = db.findUserById(uid) {
            var parts = u.badges.split(separator: ",").map(String.init).filter { !$0.isEmpty }
            if !parts.contains(def.code) { parts.append(def.code) }
            db.upsertUser(U(user: u).withBadges(parts.joined(separator: ",")))
        }
        if let me, me.id == uid {
            NotificationHelper.show(channel: .local, id: 4001, title: "Nowa odznaka: \(def.emoji) \(def.name)", body: "\(def.desc) — gratulacje! 💪")
        }
        reloadAll()
    }

    // ---------- LOKALIZACJA ----------
    func myLocation() -> (Double, Double) {
        let lat = UserDefaults.standard.double(forKey: "my_lat")
        let lng = UserDefaults.standard.double(forKey: "my_lng")
        return (lat != 0 && lng != 0) ? (lat, lng) : (52.2297, 21.0122)
    }

    func setMyLocation(lat: Double, lng: Double) {
        UserDefaults.standard.set(lat, forKey: "my_lat")
        UserDefaults.standard.set(lng, forKey: "my_lng")
    }

    func distanceKm(lat: Double, lng: Double) -> Double {
        let (myLat, myLng) = myLocation()
        let r = 6371.0
        let dLat = (lat - myLat) * .pi / 180
        let dLng = (lng - myLng) * .pi / 180
        let a = sin(dLat / 2) * sin(dLat / 2) +
                cos(myLat * .pi / 180) * cos(lat * .pi / 180) * sin(dLng / 2) * sin(dLng / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }

    // ---------- POMYSŁY → ADMINISTRACJA ----------
    func submitFeedback(subject: String, text: String) {
        guard let me else { return }
        let f = FeedbackItem(id: uuid("f"), userId: me.id, userName: me.displayName, subject: subject,
                             text: text, createdAt: nowMs(), pending: !online)
        db.upsertFeedback(f)
        feedback = db.feedbackFor(me.id)
        enqueue(entity: "feedback", json: feedbackJson(f))
        NotificationHelper.show(channel: .local, id: 5001, title: "💡 Zgłoszenie wysłane do Administracji", body: "„\(subject)” — dziękujemy, wataha czyta każdy pomysł!")
    }

    // ---------- SYNCHRONIZACJA ----------
    private func enqueue(entity: String, json: [String: Any]) {
        guard let data = try? JSONSerialization.data(withJSONObject: json),
              let payload = String(data: data, encoding: .utf8) else { return }
        let id = db.insertPending(entity: entity, operation: "UPSERT", payload: payload)
        pendingCount = db.pendingCount()
        if online { Task { await pushRemote(id: id) } }
    }

    private func pushRemote(id: Int64) async {
        guard let row = db.pendingRows().first(where: { $0.id == id }) else { return }
        let route: String?
        switch row.entity {
        case "users": route = "/api/users"
        case "posts": route = "/api/posts"
        case "messages": route = "/api/messages"
        case "exchanges": route = "/api/exchanges"
        case "crisis_signals": route = "/api/crisis"
        case "badges": route = "/api/badges"
        case "nodes": route = "/api/nodes"
        case "markers": route = "/api/markers"
        case "feedback": route = "/api/feedback"
        default: route = nil
        }
        guard let route,
              let data = row.payload.data(using: .utf8),
              let inner = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else { return }
        do {
            let (code, _) = try await comm.internet.request(method: "POST", path: route, json: ["data": inner])
            if (200...299).contains(code) {
                db.deletePending(id)
                pendingCount = db.pendingCount()
            } else {
                db.bumpTries(id)
            }
        } catch {
            db.bumpTries(id)
        }
    }

    func syncNow() async -> Int {
        guard online else { return 0 }
        var ok = 0
        for row in db.pendingRows() {
            let before = db.pendingCount()
            await pushRemote(id: row.id)
            if db.pendingCount() < before { ok += 1 }
        }
        if ok > 0 {
            NotificationHelper.show(channel: .local, id: 4002, title: "Synchronizacja zakończona", body: "Wysłano \(ok) rekordów do backendu.")
        }
        return ok
    }

    // ---------- JSON (klucze identyczne jak Android) ----------
    func userJson(_ u: User) -> [String: Any] {
        ["id": u.id, "username": u.username, "displayName": u.displayName, "phone": u.phone,
         "bio": u.bio, "reputation": u.reputation, "badges": u.badges, "isDemo": u.isDemo,
         "lat": u.lat, "lng": u.lng]
    }
    func postJson(_ p: Post) -> [String: Any] {
        ["id": p.id, "authorId": p.authorId, "authorName": p.authorName, "type": p.type,
         "title": p.title, "body": p.body, "imagePath": p.imagePath, "lat": p.lat, "lng": p.lng,
         "createdAt": p.createdAt, "status": p.status]
    }
    func messageJson(_ m: Message) -> [String: Any] {
        ["id": m.id, "threadId": m.threadId, "fromId": m.fromId, "toId": m.toId,
         "text": m.text, "createdAt": m.createdAt]
    }
    func exchangeJson(_ e: Exchange) -> [String: Any] {
        ["id": e.id, "postId": e.postId, "postTitle": e.postTitle, "proposerId": e.proposerId,
         "proposerName": e.proposerName, "ownerId": e.ownerId, "message": e.message,
         "status": e.status, "createdAt": e.createdAt]
    }
    func crisisJson(_ c: CrisisSignal) -> [String: Any] {
        ["id": c.id, "userId": c.userId, "userName": c.userName, "type": c.type, "text": c.text,
         "lat": c.lat, "lng": c.lng, "createdAt": c.createdAt]
    }
    func feedbackJson(_ f: FeedbackItem) -> [String: Any] {
        ["id": f.id, "userId": f.userId, "userName": f.userName, "subject": f.subject,
         "text": f.text, "createdAt": f.createdAt]
    }
}

/// Pomocnik: kopia User z nową listą odznak.
struct U {
    let user: User
    init(user: User) { self.user = user }
    func withBadges(_ b: String) -> User {
        var u = user
        u.badges = b
        return u
    }
}
