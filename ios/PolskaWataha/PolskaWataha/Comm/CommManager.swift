import Foundation
import Combine
import Network

/// COMM SERVICE — centrum komunikacji (identyczne jak Android CommManager).
final class CommManager: ObservableObject {
    let mesh = MeshSim()

    let internet: InternetAdapter
    let bluetooth = BluetoothAdapter()
    let wifiDirect = WifiDirectAdapter()
    let lora = LoRaAdapter()
    var adapters: [any CommunicationAdapter] { [internet, bluetooth, wifiDirect, lora] }

    @Published var statuses: [TransportKind: TransportState] = [:]
    @Published var realOnline: Bool = false
    @Published var forceOffline: Bool = false
    @Published var testLog: [String] = []

    var apiBase: String {
        get { UserDefaults.standard.string(forKey: "api_base") ?? CommManager.DEFAULT_API }
        set {
            UserDefaults.standard.set(newValue, forKey: "api_base")
            refreshStatuses()
        }
    }

    static let DEFAULT_API = "https://api.polskawataha.pl"

    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "wataha.path")

    var online: Bool { realOnline && !forceOffline }

    init() {
        internet = InternetAdapter { [weak self] in self?.apiBase ?? CommManager.DEFAULT_API }
        statuses = [
            .INTERNET: .OFFLINE, .BLUETOOTH: .DEMO, .WIFI_DIRECT: .DEMO, .LORA: .NO_HARDWARE
        ]
        monitor.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                self?.realOnline = path.status == .satisfied
                self?.refreshStatuses()
            }
        }
        monitor.start(queue: queue)
    }

    func setForceOffline(_ v: Bool) {
        forceOffline = v
        UserDefaults.standard.set(v, forKey: "force_offline")
        refreshStatuses()
    }

    func refreshStatuses() {
        statuses[.INTERNET] = online ? .READY : .OFFLINE
    }

    func logTest(_ line: String) {
        testLog.append(line)
        if testLog.count > 80 { testLog.removeFirst(testLog.count - 80) }
    }

    func testTransport(_ kind: TransportKind) async {
        guard let adapter = adapters.first(where: { $0.kind == kind }) else { return }
        logTest("— Test: \(adapter.kind.label) —")
        let avail = await adapter.isAvailable()
        logTest("   dostępność warstwy: \(avail ? "TAK" : "NIE")")
        let res = await adapter.send(CommPayload(from: "app-test", to: "backend-or-mesh", type: "PING", body: "ping-wataha", timestamp: nowMs()))
        switch res {
        case .ok(let d): logTest("   wysyłka OK (\(d))")
        case .fail(let r): logTest("   wysyłka: \(r)")
        }
        logTest("   \(adapter.describe())")
    }

    func bleScanCount() async -> Int { await bluetooth.scanOnce() }

    /// Wysyłka sygnału przez InternetAdapter — test PING.
    func pingServer() async -> (Bool, String) {
        guard online else { return (false, "Offline — test niemożliwy (kolejka przechowa dane).") }
        let res = await internet.send(CommPayload(from: "app", to: "server", type: "PING", body: "ping", timestamp: nowMs()))
        switch res {
        case .ok(let d): return (true, "Internet: odpowiedź \(d)")
        case .fail(let r): return (false, r)
        }
    }
}
