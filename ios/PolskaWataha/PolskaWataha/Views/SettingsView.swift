import SwiftUI

// MARK: - USTAWIENIA (port ekranu ustawień z Android V0.2)

struct SettingsView: View {
    @EnvironmentObject var repo: Repo
    @EnvironmentObject var comm: CommManager
    @State private var apiText = ""
    @State private var testResult = ""
    @State private var bleCount = -1
    @State private var pinging = false

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                // łączność
                SectionHeader(title: "ŁĄCZNOŚĆ", emoji: "📡")
                settingsCard {
                    Toggle(isOn: Binding(get: { repo.forceOffline },
                                         set: { repo.setForceOffline($0) })) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Tryb offline").font(.system(size: 13.5, weight: .bold)).foregroundColor(WatahaColors.Ink)
                            Text("Wymusza kolejkowanie wszystkich operacji — jak w V0.2.")
                                .font(.system(size: 10.5)).foregroundColor(WatahaColors.Grey)
                        }
                    }
                    .tint(WatahaColors.FlagRed)

                    Divider()

                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Serwer API").font(.system(size: 13.5, weight: .bold)).foregroundColor(WatahaColors.Ink)
                            TextField("https://api.polskawataha.pl", text: $apiText)
                                .font(.system(size: 11.5)).autocorrectionDisabled()
                                .textInputAutocapitalization(.never)
                                .keyboardType(.URL)
                                .onSubmit { saveApi() }
                        }
                        Button("Zapisz") { saveApi() }
                            .font(.system(size: 12, weight: .bold)).foregroundColor(WatahaColors.FlagRed)
                    }

                    Divider()

                    ForEach(TransportKind.allCases, id: \.self) { kind in
                        HStack(spacing: 8) {
                            Circle().fill(stateColor(comm.statuses[kind] ?? .OFFLINE)).frame(width: 9, height: 9)
                            Text(kind.rawValue).font(.system(size: 12.5, weight: .bold)).foregroundColor(WatahaColors.Ink)
                            Spacer()
                            Text(comm.statusText(kind)).font(.system(size: 10.5)).foregroundColor(WatahaColors.Grey)
                        }
                        .padding(.vertical, 2)
                    }
                }

                // testy
                SectionHeader(title: "TESTY WARSTWY KOMMUNIKACJI", emoji: "🧪")
                settingsCard {
                    HStack(spacing: 8) {
                        Button { ping() } label: {
                            Label(pinging ? "Ping…" : "Ping serwera", systemImage: "antenna.radiowaves.left.and.right")
                                .font(.system(size: 12.5, weight: .bold)).foregroundColor(WatahaColors.FlagRed)
                        }
                        .buttonStyle(PlainButtonStyle())
                        .disabled(pinging)
                        Button { bleScan() } label: {
                            Label(bleCount >= 0 ? "BLE: \(bleCount)" : "Skan BLE", systemImage: "dot.radiowaves.left.and.right")
                                .font(.system(size: 12.5, weight: .bold)).foregroundColor(WatahaColors.Ink)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                    Button {
                        Task {
                            await comm.testTransport(.INTERNET)
                            testResult = comm.testLog.last.map { "→ \($0)" } ?? ""
                        }
                    } label: {
                        Label("Test transportu (Internet)", systemImage: "paperplane")
                            .font(.system(size: 12.5, weight: .bold)).foregroundColor(WatahaColors.Ink)
                    }
                    .buttonStyle(PlainButtonStyle())
                    if !testResult.isEmpty {
                        Text(testResult).font(.system(size: 11)).foregroundColor(WatahaColors.Grey)
                    }
                }

                // demo
                SectionHeader(title: "DEMO — SYMULACJE (jak w V0.2)", emoji: "🎭")
                settingsCard {
                    demoRow("Wiadomość zwrotna od rozmówcy") {
                        if let t = repo.threads.first { repo.simulateReply(threadId: t.threadId) }
                    }
                    demoRow("Właściciel przyjmuje wymianę") {
                        if let e = repo.exchanges.first { repo.simulateIncomingAccept(e.id) }
                    }
                    demoRow("Wyślij testowe zgłoszenie") {
                        repo.submitFeedback(subject: "Inne", text: "Test kanału zgłoszeń — pozdrawiam Watahę!")
                    }
                }

                // o aplikacji
                SectionHeader(title: "O APLIKACJI", emoji: "ℹ️")
                settingsCard {
                    HStack {
                        Text("Wersja").font(.system(size: 12.5)).foregroundColor(WatahaColors.Ink)
                        Spacer()
                        Text("Polska Wataha! • iOS 0.2.0").font(.system(size: 12, weight: .bold)).foregroundColor(WatahaColors.Grey)
                    }
                    Divider()
                    Text("CzarneWilkiPrawdy • kotwica 🦅\nOffline-first: asystent WILK, poradnik, mesh A–D i kolejka synchronizacji działają bez internetu.")
                        .font(.system(size: 11.5)).foregroundColor(WatahaColors.Grey)
                    Divider()
                    Text("Roadmapa: V0.1 ratowanie → V0.2 płaszczyzna (tej aplikacji) → V0.3 transport Wi-Fi Direct / MultipeerConnectivity → V0.4 LoRa → V1.0 narodowa wataha.")
                        .font(.system(size: 11)).foregroundColor(WatahaColors.Grey)
                }
            }
            .padding(.bottom, 20)
        }
        .background(WatahaColors.LightGrey.opacity(0.4))
        .onAppear {
            if apiText.isEmpty { apiText = comm.apiBase }
        }
    }

    private func saveApi() {
        let t = apiText.trimmingCharacters(in: .whitespaces)
        comm.apiBase = t.isEmpty ? CommManager.DEFAULT_API : t
        apiText = comm.apiBase
    }

    private func ping() {
        pinging = true
        Task {
            let (ok, msg) = await comm.pingServer()
            testResult = ok ? "✅ \(msg)" : "🟠 \(msg)"
            pinging = false
        }
    }

    private func bleScan() {
        Task {
            bleCount = await comm.bleScanCount()
            testResult = bleCount >= 0 ? "✅ Wykryto \(bleCount) urządzeń BLE" : "🟠 Bluetooth wyłączony lub brak zgody"
        }
    }

    private func demoRow(_ title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack {
                Text(title).font(.system(size: 12.5, weight: .medium)).foregroundColor(WatahaColors.Ink)
                Spacer()
                Image(systemName: "chevron.right").font(.system(size: 11)).foregroundColor(WatahaColors.Grey)
            }
            .padding(.vertical, 4)
        }
        .buttonStyle(PlainButtonStyle())
    }

    private func settingsCard<C: View>(@ViewBuilder content: () -> C) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            content()
        }
        .padding(14)
        .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(WatahaColors.Border, lineWidth: 1))
        .padding(.horizontal, 14)
    }
}

extension CommManager {
    func statusText(_ k: TransportKind) -> String {
        switch statuses[k] ?? .OFFLINE {
        case .READY: return "gotowy"
        case .OFFLINE: return "offline"
        case .DEMO: return "symulacja"
        case .NO_HARDWARE: return "brak sprzętu"
        }
    }
}
