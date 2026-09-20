import SwiftUI
import PolskaWatahaCore

// MARK: - MESH LAB (symulator sieci A–D — silnik MeshSim testowany na Linuxie)

struct MeshLabView: View {
    @EnvironmentObject var repo: Repo
    @EnvironmentObject var comm: CommManager
    @State private var from = "A"
    @State private var to = "D"
    @State private var bFailed = false
    @State private var logLines: [String] = []

    private var mesh: MeshSim { comm.mesh }
    private var nodes: [String] { ["A", "B", "C", "D"] }

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                introCard

                // węzły
                HStack(spacing: 10) {
                    ForEach(nodes, id: \.self) { id in
                        nodeView(comm.node(id))
                    }
                }
                .padding(.horizontal, 16)

                // trasa
                routeCard

                // przełącznik awarii B
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Awaria węzła B").font(.system(size: 13, weight: .bold)).foregroundColor(WatahaColors.Ink)
                        Text("Failover: trasa przełącza się na zapasową A→C→D (koszt 3).")
                            .font(.system(size: 10.5)).foregroundColor(WatahaColors.Grey)
                    }
                    Spacer()
                    Toggle("", isOn: $bFailed)
                        .labelsHidden().tint(bFailed ? WatahaColors.Amber : WatahaColors.Green)
                        .onChange(of: bFailed) { v in
                            mesh.setFailed("B", v)
                            pushLog(v ? "NODE B — AWARIA. Trasy przez B wyłączone." : "NODE B — wraca do pracy. Bufor wysyłany.")
                        }
                }
                .padding(13)
                .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
                .overlay(RoundedRectangle(cornerRadius: 14).stroke(WatahaColors.Border, lineWidth: 1))
                .padding(.horizontal, 16)

                // log
                logCard
            }
            .padding(.bottom, 20)
        }
        .background(WatahaColors.LightGrey.opacity(0.4))
        .onAppear {
            logLines = mesh.events.suffix(30).reversed().map { $0 }
        }
    }

    private var introCard: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text("📡 POLSKON — WARSZTAT W PARADYGMIE A–D").font(.system(size: 11.5, weight: .heavy)).foregroundColor(WatahaColors.Ink)
                Spacer()
                Text("LAB").font(.system(size: 10, weight: .black)).foregroundColor(.white)
                    .padding(.horizontal, 8).padding(.vertical, 3).background(Capsule().fill(WatahaColors.FlagRed))
            }
            Text("Silnik routingu (Dijkstra, trasy zapasowe, buffer store-and-forward) — identyczny jak w Android V0.2 i pokryty testami jednostkowymi.")
                .font(.system(size: 11.5)).foregroundColor(WatahaColors.Grey)
            HStack(spacing: 6) {
                ForEach(TransportKind.allCases, id: \.self) { kind in
                    Circle().fill(stateColor(comm.statuses[kind] ?? .DEMO)).frame(width: 8, height: 8)
                    Text(kind.rawValue).font(.system(size: 9, weight: .bold)).foregroundColor(WatahaColors.Grey)
                }
            }
        }
        .padding(13)
        .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(WatahaColors.Border, lineWidth: 1))
        .padding(.horizontal, 16)
        .padding(.top, 10)
    }

    private func nodeView(_ n: MeshSim.SimNode) -> some View {
        VStack(spacing: 5) {
            ZStack {
                Circle().fill(n.online ? WatahaColors.LightRed : WatahaColors.LightGrey)
                    .frame(width: 44, height: 44)
                Text(n.id).font(.system(size: 17, weight: .black))
                    .foregroundColor(n.online ? WatahaColors.FlagRed : WatahaColors.Grey)
            }
            Text("\(n.battery)%").font(.system(size: 9.5, weight: .bold))
                .foregroundColor(n.battery > 60 ? WatahaColors.Green : n.battery > 30 ? WatahaColors.Amber : WatahaColors.FlagRed)
            Text(n.online ? "ONLINE" : "AWARIA").font(.system(size: 8, weight: .heavy))
                .foregroundColor(n.online ? WatahaColors.Green : WatahaColors.FlagRed)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(n.online ? WatahaColors.Border : WatahaColors.FlagRed, lineWidth: 1))
    }

    private var routeCard: some View {
        let route = mesh.route(from: from, to: to)
        let usable = !route.isEmpty
        return VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text("TRASA").font(.system(size: 11, weight: .heavy)).foregroundColor(WatahaColors.Ink)
                Spacer()
                if mesh.bufferedCount() > 0 {
                    Text("⚡ \(mesh.bufferedCount()) pakietów w buforze")
                        .font(.system(size: 9.5, weight: .bold)).foregroundColor(WatahaColors.Amber)
                }
            }
            HStack(spacing: 8) {
                picker("Z", $from)
                Image(systemName: "arrow.right").foregroundColor(WatahaColors.Grey).font(.system(size: 12))
                picker("DO", $to)
                Spacer()
                Button {
                    sendPacket()
                } label: {
                    Image(systemName: "paperplane.fill")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 42, height: 42)
                        .background(Circle().fill(WatahaColors.FlagRed))
                }
                .buttonStyle(PlainButtonStyle())
                .disabled(!usable && mesh.bufferedCount() == 0)
            }

            // ścieżka
            if usable {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 6) {
                        ForEach(Array(route.enumerated()), id: \.offset) { i, n in
                            Text("NODE \(n)")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 9).padding(.vertical, 5)
                                .background(Capsule().fill(n == from ? WatahaColors.Green : WatahaColors.FlagRed))
                            if i < route.count - 1 { Text("→").foregroundColor(WatahaColors.Grey).font(.system(size: 10)) }
                        }
                    }
                }
                Text("Koszt: \(route.count - 1) hopów • \(bFailed ? "trasa zapasowa (B uszkodzony)" : "trasa główna")")
                    .font(.system(size: 10)).foregroundColor(WatahaColors.Grey)
            } else {
                HStack(spacing: 6) {
                    Text("⚠️").font(.system(size: 14))
                    Text("Brak trasy \(from)→\(to). Pakiet zostanie zbuforowany (store-and-forward) i wysłany po odzyskaniu węzłów.")
                        .font(.system(size: 10.5)).foregroundColor(WatahaColors.Amber)
                }
                Button {
                    mesh.flushBuffers(from, to)
                    refreshLog()
                } label: {
                    Text("Wyślij bufor teraz").font(.system(size: 11.5, weight: .bold)).foregroundColor(WatahaColors.FlagRed)
                }
                .buttonStyle(PlainButtonStyle())
                .disabled(mesh.bufferedCount() == 0)
            }
        }
        .padding(13)
        .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(WatahaColors.Border, lineWidth: 1))
        .padding(.horizontal, 16)
    }

    private func picker(_ label: String, _ binding: Binding<String>) -> some View {
        Menu {
            ForEach(nodes, id: \.self) { n in
                Button("NODE \(n)") { binding.wrappedValue = n }
            }
        } label: {
            VStack(spacing: 1) {
                Text(label).font(.system(size: 8, weight: .bold)).foregroundColor(WatahaColors.Grey)
                Text(binding.wrappedValue).font(.system(size: 15, weight: .black)).foregroundColor(WatahaColors.Ink)
            }
            .frame(width: 56)
            .padding(.vertical, 6)
            .background(RoundedRectangle(cornerRadius: 10).fill(WatahaColors.LightGrey))
        }
    }

    private var logCard: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text("LOG WYDARZEŃ").font(.system(size: 11, weight: .heavy)).foregroundColor(WatahaColors.Ink)
                Spacer()
                Button {
                    mesh.resetLog()
                    refreshLog()
                } label: {
                    Text("Wyczyść").font(.system(size: 11, weight: .bold)).foregroundColor(WatahaColors.Grey)
                }
                .buttonStyle(PlainButtonStyle())
            }
            ForEach(logLines, id: \.self) { l in
                Text(l)
                    .font(.system(size: 10.5, design: .monospaced))
                    .foregroundColor(WatahaColors.Ink.opacity(0.8))
            }
            if logLines.isEmpty {
                Text("Brak zdarzeń. Wyślij pakiet testowy powyżej.").font(.system(size: 10.5)).foregroundColor(WatahaColors.Grey)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(13)
        .background(RoundedRectangle(cornerRadius: 14).fill(WatahaColors.LightGrey.opacity(0.6)))
        .padding(.horizontal, 16)
    }

    private func sendPacket() {
        mesh.sendPacket(from: from, to: to) { hop, node, text in
            pushLog("\(hop) \(node) → \(text)")
        }
        refreshLog()
    }

    private func pushLog(_ line: String) {
        logLines.insert(line, at: 0)
        if logLines.count > 40 { logLines.removeLast(logLines.count - 40) }
    }

    private func refreshLog() {
        logLines = mesh.events.suffix(30).reversed().map { $0 }
    }
}

extension CommManager {
    func adapterKind(_ a: any CommunicationAdapter) -> TransportKind {
        if a is InternetAdapter { return .INTERNET }
        if a is BluetoothAdapter { return .BLUETOOTH }
        if a is WifiDirectAdapter { return .WIFI_DIRECT }
        return .LORA
    }
    func node(_ id: String) -> MeshSim.SimNode {
        mesh.nodes[id] ?? MeshSim.SimNode(id, "NODE \(id)", online: false, battery: 0, neighbors: [])
    }
}
