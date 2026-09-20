import Foundation
import Combine
import CoreBluetooth

// ============ WARSTWY KOMUNIKACJI — identyczne jak Android ============
// APPLICATION → COMM SERVICE (Repo/CommManager) → COMM ADAPTER → INTERNET / BLE / P2P / LoRa

enum TransportKind: String, CaseIterable {
    case INTERNET, BLUETOOTH, WIFI_DIRECT, LORA
    var label: String {
        switch self {
        case .INTERNET: return "Internet"
        case .BLUETOOTH: return "Bluetooth LE"
        case .WIFI_DIRECT: return "P2P (Multipeer)"
        case .LORA: return "LoRa 2,4 GHz"
        }
    }
    var emoji: String {
        switch self {
        case .INTERNET: return "🌐"
        case .BLUETOOTH: return "🔵"
        case .WIFI_DIRECT: return "📶"
        case .LORA: return "📻"
        }
    }
}

enum TransportState: Equatable {
    case READY, SCANNING, DEMO, OFFLINE, NO_HARDWARE
    var label: String {
        switch self {
        case .READY: return "Gotowy"
        case .SCANNING: return "Skanowanie…"
        case .DEMO: return "Tryb testowy"
        case .OFFLINE: return "Brak połączenia"
        case .NO_HARDWARE: return "Brak sprzętu — warstwa gotowa"
        }
    }
}

struct CommPayload {
    let from: String; let to: String; let type: String; let body: String; let timestamp: Int64
}

enum CommResult {
    case ok(String)
    case fail(String)
}

protocol CommunicationAdapter {
    var kind: TransportKind { get }
    func isAvailable() async -> Bool
    func send(_ payload: CommPayload) async -> CommResult
    func describe() -> String
}

// ============ INTERNET ADAPTER — jedyny w pełni działający w V0.2 ============
struct InternetAdapter: CommunicationAdapter {
    let kind = TransportKind.INTERNET
    let baseUrlProvider: () -> String

    func serverUrl() -> String { baseUrlProvider().trimmingCharacters(in: .whitespaces) }

    /// Zapytanie REST — rzuca przy braku sieci.
    func request(method: String, path: String, json: [String: Any]? = nil) async throws -> (Int, String) {
        guard let url = URL(string: serverUrl() + path) else { throw URLError(.badURL) }
        var req = URLRequest(url: url)
        req.httpMethod = method
        req.timeoutInterval = 8
        if let json {
            req.setValue("application/json", forHTTPHeaderField: "Content-Type")
            req.httpBody = try JSONSerialization.data(withJSONObject: json)
        }
        let (data, resp) = try await URLSession.shared.data(for: req)
        let code = (resp as? HTTPURLResponse)?.statusCode ?? 0
        return (code, String(data: data, encoding: .utf8) ?? "")
    }

    func check() async -> (Bool, String) {
        do {
            let (code, _) = try await request(method: "GET", path: "/api/health")
            return code == 200 ? (true, "OK — backend odpowiada (\(serverUrl()))") : (false, "Backend: HTTP \(code)")
        } catch {
            return (false, "Brak połączenia z backendem (\(type(of: error)))")
        }
    }

    func isAvailable() async -> Bool { await check().0 }

    func send(_ payload: CommPayload) async -> CommResult {
        do {
            let json: [String: Any] = [
                "from": payload.from, "to": payload.to, "type": payload.type,
                "body": payload.body, "timestamp": payload.timestamp
            ]
            let (code, body) = try await request(method: "POST", path: "/api/comm", json: json)
            return (200...299).contains(code) ? .ok("HTTP \(code)") : .fail("HTTP \(code): \(body.prefix(120))")
        } catch {
            return .fail("Brak internetu: \(error.localizedDescription)")
        }
    }

    func describe() -> String { "REST do backendu (\(serverUrl())). Offline-first: brak odpowiedzi = kolejka synchronizacji." }
}

// ============ BLUETOOTH ADAPTER — realny skan BLE (CoreBluetooth), transport V0.3 ============
final class BluetoothAdapter: NSObject, CommunicationAdapter, CBCentralManagerDelegate, ObservableObject {
    let kind = TransportKind.BLUETOOTH
    @Published var scanResult: String?
    @Published var discovered: Int = 0
    private var central: CBCentralManager!
    private var scanActive = false
    private var scanCont: CheckedContinuation<Int, Never>?

    override init() {
        super.init()
        central = CBCentralManager(delegate: self, queue: .main)
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {}

    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,
                        advertisementData: [String: Any], rssi RSSI: NSNumber) {
        discovered += 1
    }

    /// Realny skan BLE — 4 sekundy. Zwraca liczbę wykrytych urządzeń (-1 = brak możliwości).
    func scanOnce() async -> Int {
        guard central.state == .poweredOn else { return -1 }
        discovered = 0
        scanActive = true
        await withCheckedContinuation { cont in
            scanCont = cont
            central.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
            DispatchQueue.main.asyncAfter(deadline: .now() + 4) { [weak self] in
                guard let self else { return }
                self.central.stopScan()
                self.scanActive = false
                self.scanCont?.resume(returning: self.discovered)
                self.scanCont = nil
            }
        }
    }

    func isAvailable() async -> Bool { central.state == .poweredOn }
    func send(_ payload: CommPayload) async -> CommResult {
        .fail("Nadawanie ramek BLE będzie dostępne w V0.3 (wymaga pary urządzeń z modułem BLE mesh).")
    }
    func describe() -> String { "Skaner BLE — wykrywa urządzenia w pobliżu. Transport ramek mesh: etap V0.3." }
}

// ============ P2P ADAPTER (iOS nie ma Wi-Fi Direct — odpowiednik: MultipeerConnectivity) ============
struct WifiDirectAdapter: CommunicationAdapter {
    let kind = TransportKind.WIFI_DIRECT
    func isAvailable() async -> Bool { false }
    func send(_ payload: CommPayload) async -> CommResult {
        .fail("P2P w przygotowaniu (V0.3). iOS: odpowiednik Wi-Fi Direct = MultipeerConnectivity (grupa urządzeń mesh).")
    }
    func describe() -> String { "Warstwa gotowa: grupy P2P (MultipeerConnectivity), kanał danych, mesh urządzenie–urządzenie. Testy: V0.3." }
}

// ============ LORA ADAPTER — kontrakt pod przyszły moduł (V0.4), nie udajemy sprzętu ============
struct LoRaAdapter: CommunicationAdapter {
    let kind = TransportKind.LORA
    struct LoraFrame { let fromNode: String; let toNode: String; let hopCount: Int; let payload: String }
    func buildFrame(from: String, to: String, hops: Int, payload: String) -> LoraFrame {
        LoraFrame(fromNode: from, toNode: to, hopCount: hops, payload: payload)
    }
    func isAvailable() async -> Bool { false }
    func send(_ payload: CommPayload) async -> CommResult {
        .fail("Moduł LoRa nie został wykryty (brak sprzętu w tym telefonie). Kontrakt protokołu gotowy — integracja w V0.4.")
    }
    func describe() -> String { "Protokół LoRa (ramki, hop-count, store-and-forward) gotowy. Wymaga modułu E22/SX1262 — etap V0.4." }
}
