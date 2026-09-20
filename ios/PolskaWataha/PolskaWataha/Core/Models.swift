import Foundation

// ============ MODEL — identyczny jak Android ============

enum PostType: String, CaseIterable, Codable {
    case GIVE, NEED, OFFER
    var label: String {
        switch self { case .GIVE: return "Oddaję rzecz"; case .NEED: return "Potrzebuję pomocy"; case .OFFER: return "Mogę pomóc" }
    }
    var emoji: String {
        switch self { case .GIVE: return "📦"; case .NEED: return "🆘"; case .OFFER: return "🤝" }
    }
}

enum PostStatus { static let OPEN = "OPEN"; static let CLOSED = "CLOSED"; static let DONE = "DONE" }
enum ExchangeStatus { static let PENDING = "PENDING"; static let ACCEPTED = "PRZYJĘTA"; static let REJECTED = "ODRZUCONA" }
enum CrisisTypes { static let SOS = "SOS"; static let LOCATION = "LOKALIZACJA"; static let BROADCAST = "KOMUNIKAT" }
enum NodeType { static let MESH = "MESH"; static let LORA = "LORA"; static let GATEWAY = "GATEWAY" }
enum NodeStatus { static let ONLINE = "ONLINE"; static let DEGRADED = "DEGRADED"; static let OFFLINE = "OFFLINE" }
enum MarkerKind { case POINT_POMOCY, SZTAB, SIEC }

struct User: Codable, Identifiable, Hashable {
    var id: String; var username: String; var displayName: String; var phone: String = ""
    var passwordHash: String = ""; var bio: String = ""; var reputation: Double = 3.0
    var badges: String = ""; var isDemo: Bool = false
    var lat: Double = 52.2297; var lng: Double = 21.0122; var lastSeen: Int64 = 0
}

struct Post: Codable, Identifiable, Hashable {
    var id: String; var authorId: String; var authorName: String; var type: String
    var title: String; var body: String; var imagePath: String = ""
    var lat: Double; var lng: Double; var createdAt: Int64
    var status: String = PostStatus.OPEN; var pending: Bool = false
}

struct Message: Codable, Identifiable, Hashable {
    var id: String; var threadId: String; var fromId: String; var toId: String
    var text: String; var createdAt: Int64; var read: Bool = false; var pending: Bool = false
}

struct ThreadRow: Codable, Identifiable, Hashable {
    var id: String { threadId }
    var threadId: String; var userId: String; var userName: String
    var lastText: String = ""; var lastAt: Int64 = 0; var unread: Int = 0
}

struct Exchange: Codable, Identifiable, Hashable {
    var id: String; var postId: String; var postTitle: String
    var proposerId: String; var proposerName: String; var ownerId: String
    var message: String; var status: String = ExchangeStatus.PENDING
    var createdAt: Int64; var pending: Bool = false
}

struct CrisisSignal: Codable, Identifiable, Hashable {
    var id: String; var userId: String; var userName: String; var type: String
    var text: String = ""; var lat: Double; var lng: Double; var createdAt: Int64; var pending: Bool = false
}

struct Badge: Codable, Identifiable, Hashable {
    var id: String; var userId: String; var code: String; var name: String
    var desc: String; var emoji: String; var earnedAt: Int64
}

struct NetworkNode: Codable, Identifiable, Hashable {
    var id: String; var name: String; var type: String; var status: String
    var lat: Double; var lng: Double; var battery: Int = 100; var lastSeen: Int64 = 0
}

struct GeoMarker: Codable, Identifiable, Hashable {
    var id: String; var title: String; var subtitle: String; var kind: String
    var emoji: String; var lat: Double; var lng: Double
}

struct FeedbackItem: Codable, Identifiable, Hashable {
    var id: String; var userId: String; var userName: String; var subject: String
    var text: String; var createdAt: Int64; var pending: Bool = false
}

struct PendingRow: Identifiable {
    var id: Int64; var entity: String; var operation: String; var payload: String; var createdAt: Int64; var tries: Int
}

/// Katalog odznak — identyczny jak Android.
struct BadgeDef { let code: String; let name: String; let desc: String; let emoji: String }
enum Badges {
    static let ALL: [BadgeDef] = [
        BadgeDef(code: "ORZEL", name: "Orzeł", desc: "Założenie konta", emoji: "🦅"),
        BadgeDef(code: "KOTWICA", name: "Kotwica", desc: "Pierwsza wysłana wiadomość", emoji: "⚓"),
        BadgeDef(code: "PIERWSZA_SZARZA", name: "Pierwsza Szarża", desc: "Pierwsze ogłoszenie", emoji: "🐺"),
        BadgeDef(code: "SOLIDARNOSC", name: "Solidarność", desc: "Pierwsza oferta pomocy", emoji: "🤝"),
        BadgeDef(code: "STRAZNIK", name: "Strażnik", desc: "Użycie Trybu Kryzysowego", emoji: "🚨"),
        BadgeDef(code: "WILK", name: "Wilk", desc: "5 opublikowanych ogłoszeń", emoji: "🐺"),
        BadgeDef(code: "WETERAN", name: "Weteran", desc: "10 wysłanych wiadomości", emoji: "🎖️"),
        BadgeDef(code: "GONIEC", name: "Goniec", desc: "3 przeprowadzone wymiany", emoji: "📯")
    ]
    static func byCode(_ code: String) -> BadgeDef? { ALL.first { $0.code == code } }
}

struct MapPoint: Identifiable {
    let id: String; let kind: String; let type: String; let title: String
    let subtitle: String; let emoji: String; let lat: Double; let lng: Double
    let color: ColorRGB; let authorId: String
}
struct ColorRGB { let r: Double; let g: Double; let b: Double }

func nowMs() -> Int64 { Int64(Date().timeIntervalSince1970 * 1000) }
func uuid(_ p: String) -> String { p + "_" + UUID().uuidString.replacingOccurrences(of: "-", with: "").prefix(10) }
