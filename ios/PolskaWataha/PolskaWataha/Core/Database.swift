import Foundation
import SQLite3

/// Baza danych — SQLite (systemowe), offline-first. Odpowiednik Room z Android.
final class Database {
    private var db: OpaquePointer?

    init(path: String) {
        if sqlite3_open(path, &db) != SQLITE_OK {
            print("WATAHA DB: nie udało się otworzyć (\(path))")
            db = nil
            return
        }
        migrate()
    }

    deinit { sqlite3_close(db) }

    private func exec(_ sql: String) {
        guard let db else { return }
        sqlite3_exec(db, sql, nil, nil, nil)
    }

    private func clearError(_ stmt: OpaquePointer?) {
        guard let db, let stmt else { return }
        print("WATAHA DB err: \(String(cString: sqlite3_errmsg(db))) @ \(sqlite3_sql(stmt).map { String(cString: $0) } ?? "?")")
        sqlite3_finalize(stmt)
    }

    private func migrate() {
        exec("""
        CREATE TABLE IF NOT EXISTS users(id TEXT PRIMARY KEY, username TEXT, displayName TEXT, phone TEXT, passwordHash TEXT, bio TEXT, reputation REAL, badges TEXT, isDemo INTEGER, lat REAL, lng REAL, lastSeen INTEGER);
        CREATE TABLE IF NOT EXISTS posts(id TEXT PRIMARY KEY, authorId TEXT, authorName TEXT, type TEXT, title TEXT, body TEXT, imagePath TEXT, lat REAL, lng REAL, createdAt INTEGER, status TEXT, pending INTEGER);
        CREATE TABLE IF NOT EXISTS messages(id TEXT PRIMARY KEY, threadId TEXT, fromId TEXT, toId TEXT, text TEXT, createdAt INTEGER, read INTEGER, pending INTEGER);
        CREATE TABLE IF NOT EXISTS threads(threadId TEXT PRIMARY KEY, userId TEXT, userName TEXT, lastText TEXT, lastAt INTEGER, unread INTEGER);
        CREATE TABLE IF NOT EXISTS exchanges(id TEXT PRIMARY KEY, postId TEXT, postTitle TEXT, proposerId TEXT, proposerName TEXT, ownerId TEXT, message TEXT, status TEXT, createdAt INTEGER, pending INTEGER);
        CREATE TABLE IF NOT EXISTS crisis_signals(id TEXT PRIMARY KEY, userId TEXT, userName TEXT, type TEXT, text TEXT, lat REAL, lng REAL, createdAt INTEGER, pending INTEGER);
        CREATE TABLE IF NOT EXISTS badges(id TEXT PRIMARY KEY, userId TEXT, code TEXT, name TEXT, desc TEXT, emoji TEXT, earnedAt INTEGER);
        CREATE TABLE IF NOT EXISTS nodes(id TEXT PRIMARY KEY, name TEXT, type TEXT, status TEXT, lat REAL, lng REAL, battery INTEGER, lastSeen INTEGER);
        CREATE TABLE IF NOT EXISTS markers(id TEXT PRIMARY KEY, title TEXT, subtitle TEXT, kind TEXT, emoji TEXT, lat REAL, lng REAL);
        CREATE TABLE IF NOT EXISTS feedback(id TEXT PRIMARY KEY, userId TEXT, userName TEXT, subject TEXT, text TEXT, createdAt INTEGER, pending INTEGER);
        CREATE TABLE IF NOT EXISTS pending_sync(id INTEGER PRIMARY KEY AUTOINCREMENT, entity TEXT, operation TEXT, payload TEXT, createdAt INTEGER, tries INTEGER DEFAULT 0);
        """)
    }

    // ---------- generyczne ----------
    private func run(_ sql: String, params: [Any?] = []) {
        guard let db else { return }
        var stmt: OpaquePointer?
        guard sqlite3_prepare_v2(db, sql, -1, &stmt, nil) == SQLITE_OK, let stmt else {
            clearError(stmt); return
        }
        bind(sqlite3_bind_parameter_count(stmt), params, stmt)
        sqlite3_step(stmt)
        sqlite3_finalize(stmt)
    }

    private func query(_ sql: String, params: [Any?] = []) -> [[String: Any?]] {
        guard let db else { return [] }
        var stmt: OpaquePointer?
        guard sqlite3_prepare_v2(db, sql, -1, &stmt, nil) == SQLITE_OK, let stmt else {
            clearError(stmt); return []
        }
        bind(sqlite3_bind_parameter_count(stmt), params, stmt)
        var rows: [[String: Any?]] = []
        while sqlite3_step(stmt) == SQLITE_ROW {
            var row: [String: Any?] = [:]
            for i in 0..<sqlite3_column_count(stmt) {
                let name = String(cString: sqlite3_column_name(stmt, i))
                switch sqlite3_column_type(stmt, i) {
                case SQLITE_INTEGER: row[name] = sqlite3_column_int64(stmt, i)
                case SQLITE_FLOAT: row[name] = sqlite3_column_double(stmt, i)
                case SQLITE_TEXT: row[name] = String(cString: sqlite3_column_text(stmt, i))
                case SQLITE_NULL: row[name] = nil
                default: row[name] = Data(bytes: sqlite3_column_blob(stmt, i), count: Int(sqlite3_column_bytes(stmt, i)))
                }
            }
            rows.append(row)
        }
        sqlite3_finalize(stmt)
        return rows
    }

    private func bind(_ count: Int32, _ params: [Any?], _ stmt: OpaquePointer) {
        for i in 0..<Int(count) {
            let v = i < params.count ? params[i] : nil
            switch v {
            case nil: sqlite3_bind_null(stmt, Int32(i + 1))
            case let s as String: sqlite3_bind_text(stmt, Int32(i + 1), s, -1, SQLITE_TRANSIENT)
            case let n as Int: sqlite3_bind_int64(stmt, Int32(i + 1), Int64(n))
            case let n as Int64: sqlite3_bind_int64(stmt, Int32(i + 1), n)
            case let d as Double: sqlite3_bind_double(stmt, Int32(i + 1), d)
            default: sqlite3_bind_null(stmt, Int32(i + 1))
            }
        }
    }

    // ---------- DAO (odpowiedniki Android AppDao) ----------
    func upsertUser(_ u: User) {
        run("INSERT OR REPLACE INTO users(id,username,displayName,phone,passwordHash,bio,reputation,badges,isDemo,lat,lng,lastSeen) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
            params: [u.id, u.username, u.displayName, u.phone, u.passwordHash, u.bio, u.reputation, u.badges, u.isDemo ? 1 : 0, u.lat, u.lng, u.lastSeen])
    }
    func findUserByUsername(_ username: String) -> User? {
        query("SELECT * FROM users WHERE username = ? LIMIT 1", params: [username.lowercased()]).first.flatMap(userFrom)
    }
    func findUserById(_ id: String) -> User? {
        query("SELECT * FROM users WHERE id = ? LIMIT 1", params: [id]).first.flatMap(userFrom)
    }
    func allUsers() -> [User] { query("SELECT * FROM users ORDER BY reputation DESC").compactMap(userFrom) }
    func countUsers() -> Int { (query("SELECT COUNT(*) c FROM users").first?["c"] as? Int64) ?? Int64(0) }
    func patchUserReputation(id: String, rep: Double, now: Int64) {
        run("UPDATE users SET reputation = ?, lastSeen = ? WHERE id = ?", params: [rep, now, id])
    }
    private func userFrom(_ r: [String: Any?]) -> User? {
        guard let id = r["id"] as? String, let username = r["username"] as? String else { return nil }
        return User(id: id, username: username, displayName: r["displayName"] as? String ?? "",
                    phone: r["phone"] as? String ?? "", passwordHash: r["passwordHash"] as? String ?? "",
                    bio: r["bio"] as? String ?? "", reputation: r["reputation"] as? Double ?? 3.0,
                    badges: r["badges"] as? String ?? "", isDemo: (r["isDemo"] as? Int64 ?? 0) == 1,
                    lat: r["lat"] as? Double ?? 52.2297, lng: r["lng"] as? Double ?? 21.0122,
                    lastSeen: r["lastSeen"] as? Int64 ?? 0)
    }

    func upsertPost(_ p: Post) {
        run("INSERT OR REPLACE INTO posts(id,authorId,authorName,type,title,body,imagePath,lat,lng,createdAt,status,pending) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
            params: [p.id, p.authorId, p.authorName, p.type, p.title, p.body, p.imagePath, p.lat, p.lng, p.createdAt, p.status, p.pending ? 1 : 0])
    }
    func allPosts(status: String = PostStatus.OPEN) -> [Post] {
        query("SELECT * FROM posts WHERE status = '\(status)' ORDER BY createdAt DESC").compactMap(postFrom)
    }
    func postById(_ id: String) -> Post? { query("SELECT * FROM posts WHERE id = ? LIMIT 1", params: [id]).first.flatMap(postFrom) }
    func setPostStatus(_ id: String, _ status: String) { run("UPDATE posts SET status = ? WHERE id = ?", params: [status, id]) }
    func countMyPosts(_ uid: String) -> Int { (query("SELECT COUNT(*) c FROM posts WHERE authorId = ?", params: [uid]).first?["c"] as? Int64) ?? 0 }
    private func postFrom(_ r: [String: Any?]) -> Post? {
        guard let id = r["id"] as? String, let title = r["title"] as? String else { return nil }
        return Post(id: id, authorId: r["authorId"] as? String ?? "", authorName: r["authorName"] as? String ?? "",
                    type: r["type"] as? String ?? "GIVE", title: title, body: r["body"] as? String ?? "",
                    imagePath: r["imagePath"] as? String ?? "", lat: r["lat"] as? Double ?? 52.23,
                    lng: r["lng"] as? Double ?? 21.01, createdAt: r["createdAt"] as? Int64 ?? 0,
                    status: r["status"] as? String ?? PostStatus.OPEN, pending: (r["pending"] as? Int64 ?? 0) == 1)
    }

    func upsertMessage(_ m: Message) {
        run("INSERT OR REPLACE INTO messages(id,threadId,fromId,toId,text,createdAt,read,pending) VALUES(?,?,?,?,?,?,?,?)",
            params: [m.id, m.threadId, m.fromId, m.toId, m.text, m.createdAt, m.read ? 1 : 0, m.pending ? 1 : 0])
    }
    func threadMessages(_ threadId: String) -> [Message] {
        query("SELECT * FROM messages WHERE threadId = ? ORDER BY createdAt ASC", params: [threadId]).compactMap(messageFrom)
    }
    func countMyMessages(_ uid: String) -> Int { (query("SELECT COUNT(*) c FROM messages WHERE fromId = ?", params: [uid]).first?["c"] as? Int64) ?? 0 }
    func markThreadRead(_ threadId: String) {
        run("UPDATE messages SET read = 1 WHERE threadId = ?", params: [threadId])
        run("UPDATE threads SET unread = 0 WHERE threadId = ?", params: [threadId])
    }
    private func messageFrom(_ r: [String: Any?]) -> Message? {
        guard let id = r["id"] as? String else { return nil }
        return Message(id: id, threadId: r["threadId"] as? String ?? "", fromId: r["fromId"] as? String ?? "",
                       toId: r["toId"] as? String ?? "", text: r["text"] as? String ?? "",
                       createdAt: r["createdAt"] as? Int64 ?? 0, read: (r["read"] as? Int64 ?? 0) == 1,
                       pending: (r["pending"] as? Int64 ?? 0) == 1)
    }

    func upsertThread(_ t: ThreadRow) {
        run("INSERT OR REPLACE INTO threads(threadId,userId,userName,lastText,lastAt,unread) VALUES(?,?,?,?,?,?)",
            params: [t.threadId, t.userId, t.userName, t.lastText, t.lastAt, t.unread])
    }
    func allThreads() -> [ThreadRow] {
        query("SELECT * FROM threads ORDER BY lastAt DESC").compactMap { r in
            guard let tid = r["threadId"] as? String else { return nil }
            return ThreadRow(threadId: tid, userId: r["userId"] as? String ?? "", userName: r["userName"] as? String ?? "",
                          lastText: r["lastText"] as? String ?? "", lastAt: r["lastAt"] as? Int64 ?? 0,
                          unread: (r["unread"] as? Int64 ?? 0).flatMap { Int($0) } ?? 0)
        }
    }
    func threadById(_ tid: String) -> ThreadRow? { allThreads().first { $0.threadId == tid } }

    func upsertExchange(_ e: Exchange) {
        run("INSERT OR REPLACE INTO exchanges(id,postId,postTitle,proposerId,proposerName,ownerId,message,status,createdAt,pending) VALUES(?,?,?,?,?,?,?,?,?,?)",
            params: [e.id, e.postId, e.postTitle, e.proposerId, e.proposerName, e.ownerId, e.message, e.status, e.createdAt, e.pending ? 1 : 0])
    }
    func allExchanges() -> [Exchange] {
        query("SELECT * FROM exchanges ORDER BY createdAt DESC").compactMap { r in
            guard let id = r["id"] as? String else { return nil }
            return Exchange(id: id, postId: r["postId"] as? String ?? "", postTitle: r["postTitle"] as? String ?? "",
                            proposerId: r["proposerId"] as? String ?? "", proposerName: r["proposerName"] as? String ?? "",
                            ownerId: r["ownerId"] as? String ?? "", message: r["message"] as? String ?? "",
                            status: r["status"] as? String ?? ExchangeStatus.PENDING,
                            createdAt: r["createdAt"] as? Int64 ?? 0, pending: (r["pending"] as? Int64 ?? 0) == 1)
        }
    }
    func setExchangeStatus(_ id: String, _ status: String) { run("UPDATE exchanges SET status = ? WHERE id = ?", params: [status, id]) }

    func upsertCrisis(_ c: CrisisSignal) {
        run("INSERT OR REPLACE INTO crisis_signals(id,userId,userName,type,text,lat,lng,createdAt,pending) VALUES(?,?,?,?,?,?,?,?,?)",
            params: [c.id, c.userId, c.userName, c.type, c.text, c.lat, c.lng, c.createdAt, c.pending ? 1 : 0])
    }
    func allCrisis(limit: Int = 50) -> [CrisisSignal] {
        query("SELECT * FROM crisis_signals ORDER BY createdAt DESC LIMIT \(limit)").compactMap { r in
            guard let id = r["id"] as? String else { return nil }
            return CrisisSignal(id: id, userId: r["userId"] as? String ?? "", userName: r["userName"] as? String ?? "",
                                type: r["type"] as? String ?? "", text: r["text"] as? String ?? "",
                                lat: r["lat"] as? Double ?? 0, lng: r["lng"] as? Double ?? 0,
                                createdAt: r["createdAt"] as? Int64 ?? 0, pending: (r["pending"] as? Int64 ?? 0) == 1)
        }
    }

    func insertBadge(_ b: Badge) {
        run("INSERT OR IGNORE INTO badges(id,userId,code,name,desc,emoji,earnedAt) VALUES(?,?,?,?,?,?,?)",
            params: [b.id, b.userId, b.code, b.name, b.desc, b.emoji, b.earnedAt])
    }
    func badgesFor(_ uid: String) -> [Badge] {
        query("SELECT * FROM badges WHERE userId = ? ORDER BY earnedAt ASC", params: [uid]).compactMap { r in
            guard let id = r["id"] as? String else { return nil }
            return Badge(id: id, userId: r["userId"] as? String ?? "", code: r["code"] as? String ?? "",
                         name: r["name"] as? String ?? "", desc: r["desc"] as? String ?? "",
                         emoji: r["emoji"] as? String ?? "", earnedAt: r["earnedAt"] as? Int64 ?? 0)
        }
    }
    func badgeCount(_ uid: String, _ code: String) -> Int {
        (query("SELECT COUNT(*) c FROM badges WHERE userId = ? AND code = ?", params: [uid, code]).first?["c"] as? Int64).flatMap { Int($0) } ?? 0
    }

    func upsertNode(_ n: NetworkNode) {
        run("INSERT OR REPLACE INTO nodes(id,name,type,status,lat,lng,battery,lastSeen) VALUES(?,?,?,?,?,?,?,?)",
            params: [n.id, n.name, n.type, n.status, n.lat, n.lng, n.battery, n.lastSeen])
    }
    func allNodes() -> [NetworkNode] {
        query("SELECT * FROM nodes ORDER BY name").compactMap { r in
            guard let id = r["id"] as? String else { return nil }
            return NetworkNode(id: id, name: r["name"] as? String ?? "", type: r["type"] as? String ?? "",
                               status: r["status"] as? String ?? "", lat: r["lat"] as? Double ?? 0,
                               lng: r["lng"] as? Double ?? 0, battery: (r["battery"] as? Int64).flatMap { Int($0) } ?? 100,
                               lastSeen: r["lastSeen"] as? Int64 ?? 0)
        }
    }

    func upsertMarker(_ m: GeoMarker) {
        run("INSERT OR REPLACE INTO markers(id,title,subtitle,kind,emoji,lat,lng) VALUES(?,?,?,?,?,?,?)",
            params: [m.id, m.title, m.subtitle, m.kind, m.emoji, m.lat, m.lng])
    }
    func allMarkers() -> [GeoMarker] {
        query("SELECT * FROM markers").compactMap { r in
            guard let id = r["id"] as? String else { return nil }
            return GeoMarker(id: id, title: r["title"] as? String ?? "", subtitle: r["subtitle"] as? String ?? "",
                             kind: r["kind"] as? String ?? "", emoji: r["emoji"] as? String ?? "",
                             lat: r["lat"] as? Double ?? 0, lng: r["lng"] as? Double ?? 0)
        }
    }

    func upsertFeedback(_ f: FeedbackItem) {
        run("INSERT OR REPLACE INTO feedback(id,userId,userName,subject,text,createdAt,pending) VALUES(?,?,?,?,?,?,?)",
            params: [f.id, f.userId, f.userName, f.subject, f.text, f.createdAt, f.pending ? 1 : 0])
    }
    func feedbackFor(_ uid: String) -> [FeedbackItem] {
        query("SELECT * FROM feedback WHERE userId = ? ORDER BY createdAt DESC", params: [uid]).compactMap { r in
            guard let id = r["id"] as? String else { return nil }
            return FeedbackItem(id: id, userId: r["userId"] as? String ?? "", userName: r["userName"] as? String ?? "",
                                subject: r["subject"] as? String ?? "", text: r["text"] as? String ?? "",
                                createdAt: r["createdAt"] as? Int64 ?? 0, pending: (r["pending"] as? Int64 ?? 0) == 1)
        }
    }

    func insertPending(entity: String, operation: String, payload: String) -> Int64 {
        run("INSERT INTO pending_sync(entity,operation,payload,createdAt,tries) VALUES(?,?,?,?,0)",
            params: [entity, operation, payload, nowMs()])
        return (query("SELECT MAX(id) m FROM pending_sync").first?["m"] as? Int64) ?? 0
    }
    func pendingRows() -> [PendingRow] {
        query("SELECT * FROM pending_sync ORDER BY createdAt ASC").compactMap { r in
            guard let id = r["id"] as? Int64, let e = r["entity"] as? String else { return nil }
            return PendingRow(id: id, entity: e, operation: r["operation"] as? String ?? "",
                              payload: r["payload"] as? String ?? "", createdAt: r["createdAt"] as? Int64 ?? 0,
                              tries: (r["tries"] as? Int64).flatMap { Int($0) } ?? 0)
        }
    }
    func pendingCount() -> Int { (query("SELECT COUNT(*) c FROM pending_sync").first?["c"] as? Int64).flatMap { Int($0) } ?? 0 }
    func bumpTries(_ id: Int64) { run("UPDATE pending_sync SET tries = tries + 1 WHERE id = ?", params: [id]) }
    func deletePending(_ id: Int64) { run("DELETE FROM pending_sync WHERE id = ?", params: [id]) }

    // Domyślna lokalizacja (Warszawa) w SharedPreferences → UserDefaults w Repo.
}

private let SQLITE_TRANSIENT = unsafeBitCast(-1, to: sqlite3_destructor_type.self)
