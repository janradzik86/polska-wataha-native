package pl.wataha.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PostType(val label: String, val emoji: String) {
    GIVE("Oddaję rzecz", "📦"),
    NEED("Potrzebuję pomocy", "🆘"),
    OFFER("Mogę pomóc", "🤝")
}

object PostStatus {
    const val OPEN = "OPEN"
    const val CLOSED = "CLOSED"
    const val DONE = "DONE"
}

object Types {
    const val CRISIS_SOS = "SOS"
    const val CRISIS_LOCATION = "LOKALIZACJA"
    const val CRISIS_BROADCAST = "KOMUNIKAT"
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val phone: String = "",
    val passwordHash: String = "",
    val bio: String = "",
    val reputation: Double = 3.0,
    val badges: String = "",
    val isDemo: Boolean = false,
    val lat: Double = 52.2297,
    val lng: Double = 21.0122,
    val lastSeen: Long = 0L
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val type: String,          // PostType.name
    val title: String,
    val body: String,
    val imagePath: String = "",
    val lat: Double,
    val lng: Double,
    val createdAt: Long,
    val status: String = PostStatus.OPEN,
    val pending: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val fromId: String,
    val toId: String,
    val text: String,
    val createdAt: Long,
    val read: Boolean = false,
    val pending: Boolean = false
)

@Entity(tableName = "threads")
data class ThreadEntity(
    @PrimaryKey val threadId: String,
    val userId: String,
    val userName: String,
    val lastText: String = "",
    val lastAt: Long = 0L,
    val unread: Int = 0
)

object ExchangeStatus {
    const val PENDING = "PENDING"
    const val ACCEPTED = "PRZYJĘTA"
    const val REJECTED = "ODRZUCONA"
    const val DONE = "ZREALIZOWANA"
}

@Entity(tableName = "exchanges")
data class ExchangeEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val postTitle: String,
    val proposerId: String,
    val proposerName: String,
    val ownerId: String,
    val message: String,
    val status: String = ExchangeStatus.PENDING,
    val createdAt: Long,
    val pending: Boolean = false
)

@Entity(tableName = "crisis_signals")
data class CrisisSignalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val type: String,     // Types.*
    val text: String = "",
    val lat: Double,
    val lng: Double,
    val createdAt: Long,
    val pending: Boolean = false
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val code: String,
    val name: String,
    val desc: String,
    val emoji: String,
    val earnedAt: Long
)

enum class NodeType(val label: String) {
    MESH("Symulator mesh"),
    LORA("Węzeł LoRa"),
    GATEWAY("Brama internetowa")
}

enum class NodeStatus(val label: String) {
    ONLINE("Online"),
    DEGRADED("Osłabiony"),
    OFFLINE("Awaria")
}

@Entity(tableName = "nodes")
data class NetworkNodeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,       // NodeType.name
    val status: String,     // NodeStatus.name
    val lat: Double,
    val lng: Double,
    val battery: Int = 100,
    val lastSeen: Long = 0L
)

enum class MarkerKind(val label: String, val color: Long) {
    POINT_POMOCY("Punkt pomocy", 0xFFD4213D),
    SZTAB("Sztab / hub", 0xFF1F2A44),
    SIEC("Węzeł sieci", 0xFF2E7D32)
}

@Entity(tableName = "markers")
data class GeoMarkerEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val kind: String,       // MarkerKind.name
    val emoji: String,
    val lat: Double,
    val lng: Double
)

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entity: String,
    val operation: String,   // UPSERT / DELETE
    val payload: String,     // JSON
    val createdAt: Long,
    val tries: Int = 0
)

@Entity(tableName = "feedback")
data class FeedbackEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val subject: String,
    val text: String,
    val createdAt: Long,
    val pending: Boolean = false
)

/** Struktura punktu na mapie (gotowa do serializacji) */
data class MapPoint(
    val id: String,
    val kind: String,   // POST / CRISIS / MARKER / NODE / SELF
    val type: String = "",
    val title: String,
    val emoji: String,
    val lat: Double,
    val lng: Double
)
