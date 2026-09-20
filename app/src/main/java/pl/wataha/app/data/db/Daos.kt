package pl.wataha.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.wataha.app.data.model.BadgeEntity
import pl.wataha.app.data.model.CrisisSignalEntity
import pl.wataha.app.data.model.ExchangeEntity
import pl.wataha.app.data.model.FeedbackEntity
import pl.wataha.app.data.model.GeoMarkerEntity
import pl.wataha.app.data.model.MessageEntity
import pl.wataha.app.data.model.NetworkNodeEntity
import pl.wataha.app.data.model.PendingSyncEntity
import pl.wataha.app.data.model.PostEntity
import pl.wataha.app.data.model.ThreadEntity
import pl.wataha.app.data.model.UserEntity

@Dao
interface AppDao {

    // ---- Użytkownicy ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(u: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findUserById(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY reputation DESC")
    fun usersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun userFlow(id: String): Flow<UserEntity?>

    @Query("UPDATE users SET reputation = :rep, lastSeen = :now WHERE id = :id")
    suspend fun updateReputation(id: String, rep: Double, now: Long)

    // ---- Ogłoszenia ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPost(p: PostEntity)

    @Query("SELECT * FROM posts WHERE status = 'OPEN' ORDER BY createdAt DESC")
    fun postsFlow(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun postById(id: String): PostEntity?

    @Query("SELECT * FROM posts WHERE authorId = :uid ORDER BY createdAt DESC")
    fun myPostsFlow(uid: String): Flow<List<PostEntity>>

    @Query("UPDATE posts SET status = :status WHERE id = :id")
    suspend fun setPostStatus(id: String, status: String)

    // ---- Wiadomości ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(m: MessageEntity)

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY createdAt ASC")
    fun messagesFlow(threadId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM threads ORDER BY lastAt DESC")
    fun threadsFlow(): Flow<List<ThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThread(t: ThreadEntity)

    @Query("SELECT * FROM threads WHERE threadId = :threadId LIMIT 1")
    suspend fun threadById(threadId: String): ThreadEntity?

    @Query("UPDATE messages SET read = 1 WHERE threadId = :threadId")
    suspend fun markThreadRead(threadId: String)

    @Query("UPDATE threads SET unread = 0 WHERE threadId = :threadId")
    suspend fun resetUnread(threadId: String)

    // ---- Wymiany ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExchange(e: ExchangeEntity)

    @Query("SELECT * FROM exchanges ORDER BY createdAt DESC")
    fun exchangesFlow(): Flow<List<ExchangeEntity>>

    @Query("UPDATE exchanges SET status = :status WHERE id = :id")
    suspend fun setExchangeStatus(id: String, status: String)

    // ---- Kryzys ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCrisis(c: CrisisSignalEntity)

    @Query("SELECT * FROM crisis_signals ORDER BY createdAt DESC LIMIT 50")
    fun crisisFlow(): Flow<List<CrisisSignalEntity>>

    // ---- Odznaki ----
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(b: BadgeEntity)

    @Query("SELECT * FROM badges WHERE userId = :uid ORDER BY earnedAt ASC")
    fun badgesFlow(uid: String): Flow<List<BadgeEntity>>

    @Query("SELECT COUNT(*) FROM badges WHERE userId = :uid AND code = :code")
    suspend fun badgeCount(uid: String, code: String): Int

    // ---- Węzły sieci ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNode(n: NetworkNodeEntity)

    @Query("SELECT * FROM nodes ORDER BY name")
    fun nodesFlow(): Flow<List<NetworkNodeEntity>>

    @Query("SELECT * FROM nodes WHERE id = :id LIMIT 1")
    suspend fun nodeById(id: String): NetworkNodeEntity?

    @Query("UPDATE nodes SET status = :status, lastSeen = :now WHERE id = :id")
    suspend fun setNodeStatus(id: String, status: String, now: Long)

    // ---- Markery mapy ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMarker(m: GeoMarkerEntity)

    @Query("SELECT * FROM markers")
    fun markersFlow(): Flow<List<GeoMarkerEntity>>

    // ---- Kolejka synchronizacji ----
    @Insert
    suspend fun insertPending(p: PendingSyncEntity): Long

    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    fun pendingFlow(): Flow<List<PendingSyncEntity>>

    @Query("SELECT COUNT(*) FROM pending_sync")
    fun pendingCountFlow(): Flow<Int>

    @Query("UPDATE pending_sync SET tries = tries + 1 WHERE id = :id")
    suspend fun bumpTries(id: Long)

    @Delete
    suspend fun deletePending(p: PendingSyncEntity)

    // ---- Pomysły / zgłoszenia do administracji ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeedback(f: FeedbackEntity)

    @Query("SELECT * FROM feedback WHERE userId = :uid ORDER BY createdAt DESC")
    fun myFeedbackFlow(uid: String): Flow<List<FeedbackEntity>>

    // ---- Liczniki ----
    @Query("SELECT COUNT(*) FROM users")
    suspend fun userCount(): Int

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun postCount(): Int

    @Query("SELECT COUNT(*) FROM nodes")
    suspend fun nodeCount(): Int

    @Query("SELECT COUNT(*) FROM posts WHERE authorId = :uid")
    suspend fun myPostCount(uid: String): Int

    @Query("SELECT COUNT(*) FROM messages WHERE fromId = :uid")
    suspend fun myMessageCount(uid: String): Int
}
