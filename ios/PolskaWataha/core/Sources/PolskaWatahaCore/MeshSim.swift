import Foundation

/// MESH LAB / SYMULATOR — test architektury bez fizycznych węzłów.
/// Topologia:  A ── B ── C ── D   (+ zapasowa krawędź A ── C)
/// Węzły można wyłączać (symulacja awarii) — routing przełącza się na trasy zapasowe,
/// a pakiety zablokowane przez awarię trafiają do bufora store-and-forward.
/// Czysta logika (bez SwiftUI) — może być testowana na każdym backendzie.
public final class MeshSim {

    public struct SimNode {
        public let id: String
        public let name: String
        public var online: Bool
        public var battery: Int
        public let neighbors: [String]
        public init(_ id: String, _ name: String, online: Bool, battery: Int, neighbors: [String]) {
            self.id = id; self.name = name; self.online = online; self.battery = battery; self.neighbors = neighbors
        }
    }

    public struct Buffered { public let node: String; public let target: String; public let payload: String }

    public static let NODE_LABEL = "NODE"

    public private(set) var nodes: [String: SimNode] = [
        "A": SimNode("A", "NODE A", online: true, battery: 87, neighbors: ["B", "C"]),
        "B": SimNode("B", "NODE B", online: true, battery: 64, neighbors: ["A", "C"]),
        "C": SimNode("C", "NODE C", online: true, battery: 91, neighbors: ["B", "D", "A"]),
        "D": SimNode("D", "NODE D", online: true, battery: 45, neighbors: ["C"])
    ]

    private var buffers: [String: [String]] = [:]

    public private(set) var events: [String] = [
        "MESH LAB zainicjalizowany. Topologia: A—B—C—D (główna) + A—C (zapasowa, dłuższa).",
        "Wyślij pakiet testowy A→D lub wyłącz NODE B i zaobserwuj zmianę trasy."
    ]

    public init() {}

    public func log(_ line: String) {
        events.append(line)
        if events.count > 120 { events.removeFirst(events.count - 120) }
    }

    public func resetLog() { events = ["MESH LAB — log wyczyszczony."] }

    public func bufferedCount() -> Int { buffers.values.reduce(0) { $0 + $1.count } }

    public func bufferedEntries() -> [Buffered] {
        buffers.flatMap { (node, list) in list.map { Buffered(node: node, target: $0, payload: "pakiet \(node)→\($0)") } }
            .filter { route(from: $0.node, to: $0.target).isEmpty }
    }

    public func setFailed(_ id: String, _ failed: Bool) {
        guard var n = nodes[id] else { return }
        n.online = !failed
        nodes[id] = n
        if failed { log("⚠️ \(MeshSim.NODE_LABEL)/\(id) — SYMULOWANA AWARIA. Węzeł nie odpowiada.") }
        else { log("✅ \(MeshSim.NODE_LABEL)/\(id) — wznowienie pracy (bateria \(n.battery)%).") }
        log("🛰️ Nowa trasa A→D: \(pathLabel(route(from: "A", to: "D")))")
    }

    private func edge(_ a: String, _ b: String) -> Bool { nodes[a]?.neighbors.contains(b) ?? false }

    /// Wagi krawędzi: trasa główna A→B→C→D (1), zapasowa A→C jest „droższa” (3 — słabsza radiówka).
    private func weight(_ a: String, _ b: String) -> Int {
        (a == "A" && b == "C") || (a == "C" && b == "A") ? 3 : 1
    }

    /// Dijkstra — trasa omija węzły wyłączone. Zwraca listę węzłów trasy lub pustą listę.
    public func route(from: String, to: String, ignoreFailed: Bool = true) -> [String] {
        var d: [String: Int] = [:]
        var prev: [String: String] = [:]
        var visited = Set<String>()
        var queue = Set(nodes.keys)
        for k in nodes.keys { d[k] = Int.max }
        d[from] = 0
        while !queue.isEmpty {
            guard let u = queue.min(by: { (d[$0] ?? Int.max) < (d[$1] ?? Int.max) }) else { break }
            queue.remove(u)
            if u == to { break }
            visited.insert(u)
            for v in nodes[u]?.neighbors ?? [] {
                if visited.contains(v) { continue }
                if ignoreFailed && (nodes[v]?.online == false) { continue }
                let alt = (d[u] ?? Int.max) + weight(u, v)
                if alt < (d[v] ?? Int.max) { d[v] = alt; prev[v] = u }
            }
        }
        if (d[to] ?? Int.max) == Int.max { return [] }
        var path = [to]
        var cur = to
        while cur != from {
            guard let p = prev[cur] else { return [] }
            path.insert(p, at: 0)
            cur = p
        }
        return path
    }

    public func pathLabel(_ path: [String]) -> String {
        path.isEmpty ? "— brak trasy —" : path.joined(separator: " → ")
    }

    /// Wysyłka pakietu (log przejść między węzłami). `onHop` wywoływane po każdym hopie.
    public func sendPacket(from: String, to: String, onHop: ((String, String, String) -> Void)? = nil) {
        let path = route(from: from, to: to)
        if path.isEmpty {
            buffers[from, default: []].append(to)
            log("📦 Pakiet \(from)→\(to): brak trasy (awaria pośredniego węzła). STORE-AND-FORWARD — pakiet przechowywany w \(MeshSim.NODE_LABEL)/\(from).")
            return
        }
        log("🛰️ Pakiet \(from)→\(to): trasa \(pathLabel(path)) (\(path.count - 1) hop\(path.count > 2 ? "s" : "")).")
        for i in 0..<(path.count - 1) {
            let u = path[i], v = path[i + 1]
            let rssi = -(60 + Int.random(in: 0..<25))
            let line = "   ↦ \(MeshSim.NODE_LABEL)/\(u) → \(MeshSim.NODE_LABEL)/\(v): ramka przekazana (RSSI \(rssi) dBm, \(nodes[v]?.battery ?? 0)% baterii)."
            log(line)
            onHop?(u, v, line)
            Thread.sleep(forTimeInterval: 0.45)
        }
        log("✅ Doręczono: \(from) → \(to) (potwierdzenie odbioru z \(MeshSim.NODE_LABEL)/\(to)).")
    }

    public func flushBuffers(_ from: String, _ to: String) {
        guard var list = buffers[from], let idx = list.firstIndex(of: to) else { return }
        if route(from: from, to: to).isEmpty {
            log("🧊 Pakiet \(from)→\(to) nadal bez trasy — pozostaje w buforze (store-and-forward).")
            return
        }
        list.remove(at: idx)
        buffers[from] = list
        log("📤 Store-and-forward: pakiet \(from)→\(to) doręczony po wznowieniu pracy węzłów.")
    }

    public func signalQuality(_ a: String, _ b: String) -> String {
        if !edge(a, b) { return "—" }
        let da = Double(a.utf8.first ?? 65) - Double(b.utf8.first ?? 65)
        let db = Double(a.utf8.last ?? 65) - Double(b.utf8.last ?? 65)
        let dist = (da * da + db * db).squareRoot()
        return "\(Int(95 - dist * 9))%"
    }
}
