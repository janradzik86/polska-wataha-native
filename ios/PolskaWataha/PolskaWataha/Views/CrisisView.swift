import SwiftUI

// MARK: - TRYB KRYZYSOWY 🚨 (port ekranu kryzysowego z V0.2)

struct CrisisView: View {
    @EnvironmentObject var repo: Repo
    @AppStorage("crisis_mode") private var crisisMode = false
    @State private var type: String = CrisisTypes.SOS
    @State private var text = ""
    @State private var confirmSOS = false
    @State private var sendWithLocation = true

    private var signals: [CrisisSignal] { repo.crisis }

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                // nagłówek kryzysowy
                HStack(spacing: 12) {
                    Text("🚨").font(.system(size: 40))
                    VStack(alignment: .leading, spacing: 3) {
                        Text("TRYB KRYZYSOWY").font(.system(size: 17, weight: .black)).foregroundColor(.white)
                        Text("Sygnały rozchodzą się po sieci watahy (mesh + Internet).")
                            .font(.system(size: 11.5)).foregroundColor(.white.opacity(0.85))
                    }
                    Spacer()
                    Toggle("", isOn: $crisisMode)
                        .labelsHidden()
                        .tint(.white)
                        .onChange(of: crisisMode) { v in
                            NotificationHelper.show(channel: .crisis, id: 8001,
                                                    title: v ? "🟥 TRYB KRYZYSOWY WŁĄCZONY" : "Tryb kryzysowy wyłączony",
                                                    body: v ? "Nasłuch 24/7. Powiadomienia priorytetowe aktywne." : "Wracasz do normalnego trybu.",
                                                    fullScreen: v)
                        }
                }
                .padding(16)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(RoundedRectangle(cornerRadius: 18).fill(
                    LinearGradient(colors: [WatahaColors.FlagRed, WatahaColors.DarkRed],
                                   startPoint: .topLeading, endPoint: .bottomTrailing)))
                .shadow(color: WatahaColors.FlagRed.opacity(0.35), radius: 10, y: 4)
                .padding(.horizontal, 14).padding(.top, 10)

                // wybór rodzaju sygnału
                HStack(spacing: 8) {
                    crisisChip(CrisisTypes.SOS, "SOS", "🆘")
                    crisisChip(CrisisTypes.LOCATION, "Lokalizacja", "📍")
                    crisisChip(CrisisTypes.BROADCAST, "Komunikat", "📢")
                }
                .padding(.horizontal, 14)

                if type != CrisisTypes.BROADCAST {
                    HStack(spacing: 6) {
                        Image(systemName: "location.fill").font(.system(size: 11)).foregroundColor(WatahaColors.Grey)
                        Toggle("Dołącz moją lokalizację", isOn: $sendWithLocation)
                            .font(.system(size: 12.5, weight: .medium))
                            .tint(WatahaColors.FlagRed)
                    }
                    .padding(.horizontal, 16)
                }

                if type != CrisisTypes.LOCATION {
                    TextEditor(text: $text)
                        .font(.system(size: 14))
                        .frame(minHeight: 84)
                        .padding(8)
                        .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
                        .padding(.horizontal, 16)
                }

                Button {
                    confirmSOS = true
                } label: {
                    HStack(spacing: 10) {
                        Text("🚨").font(.system(size: 26))
                        Text(type == CrisisTypes.SOS ? "WYŚLIJ SYGNAŁ SOS" : "WYŚLIJ SYGNAŁ")
                            .font(.system(size: 16, weight: .black))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 17)
                    .background(RoundedRectangle(cornerRadius: 16).fill(WatahaColors.DarkRed))
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(style: StrokeStyle(lineWidth: 2, dash: [7]))
                            .foregroundColor(.white.opacity(0.7))
                    )
                    .scaleEffect(confirmSOS ? 0.98 : 1)
                }
                .buttonStyle(PlainButtonStyle())
                .padding(.horizontal, 16)
                .padding(.top, 2)

                // status sieci
                meshStatusCard

                SectionHeader(title: "Aktywne sygnały (\(signals.count))", emoji: "📡")
                ForEach(signals) { s in
                    CrisisCard(signal: s)
                        .padding(.horizontal, 14)
                }
                if signals.isEmpty {
                    Text("Brak aktywnych sygnałów — wataha czuwa. 🐺")
                        .font(.system(size: 12.5)).foregroundColor(WatahaColors.Grey)
                        .padding(.vertical, 20)
                }
            }
            .padding(.bottom, 20)
        }
        .background(crisisMode ? WatahaColors.DarkRed.opacity(0.08) : WatahaColors.LightGrey.opacity(0.4))
        .alert("Wysłać sygnał „\(type)" + (type == CrisisTypes.SOS ? " — SOS" : "") + "”?", isPresented: $confirmSOS) {
            Button("Anuluj", role: .cancel) {}
            Button("TAK, wyślij 🚨", role: .destructive) { send() }
        } message: {
            Text("Sygnał zostanie rozgłoszony w sieci watahy. W trybie offline trafi do kolejki i wyśle się automatycznie.")
        }
    }

    private func crisisChip(_ t: String, _ label: String, _ emoji: String) -> some View {
        Button {
            type = t
        } label: {
            HStack(spacing: 5) {
                Text(emoji)
                Text(label).font(.system(size: 12.5, weight: .bold))
            }
            .foregroundColor(type == t ? .white : WatahaColors.Ink)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 11)
            .background(RoundedRectangle(cornerRadius: 12)
                .fill(type == t ? WatahaColors.FlagRed : Color.white))
            .overlay(RoundedRectangle(cornerRadius: 12)
                .stroke(type == t ? Color.clear : WatahaColors.Border, lineWidth: 1))
        }
        .buttonStyle(PlainButtonStyle())
    }

    private var meshStatusCard: some View {
        let online = repo.nodes.filter { $0.status == NodeStatus.ONLINE }.count
        return VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("📡 SIEC WATAHY — MESH").font(.system(size: 12, weight: .heavy)).foregroundColor(WatahaColors.Ink)
                Spacer()
                Text("\(online)/\(repo.nodes.count) węzłów ONLINE")
                    .font(.system(size: 10.5, weight: .bold))
                    .foregroundColor(online > 0 ? WatahaColors.Green : WatahaColors.Grey)
            }
            HStack(spacing: 8) {
                ForEach(repo.nodes.filter { $0.type == NodeType.MESH }) { n in
                    VStack(spacing: 3) {
                        Circle()
                            .fill(n.status == NodeStatus.ONLINE ? WatahaColors.Green :
                                  n.status == NodeStatus.DEGRADED ? WatahaColors.Amber : WatahaColors.Grey)
                            .frame(width: 10, height: 10)
                        Text(String(n.name.suffix(1))).font(.system(size: 10, weight: .bold)).foregroundColor(WatahaColors.Ink)
                        Text("\(n.battery)%").font(.system(size: 8.5)).foregroundColor(WatahaColors.Grey)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            Text(repo.online ? "🟢 Internet: sygnały trafiają też do bramy backendu (kolejka: \(repo.pendingCount))."
                             : "🟠 Offline: sygnały buforowane lokalnie, wyślą się po odzyskaniu łączności (kolejka: \(repo.pendingCount)).")
                .font(.system(size: 10.5))
                .foregroundColor(repo.online ? WatahaColors.Green : WatahaColors.Amber)
        }
        .padding(13)
        .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(WatahaColors.Border, lineWidth: 1))
        .padding(.horizontal, 14)
    }

    private func send() {
        var msg = text.trimmingCharacters(in: .whitespaces)
        if msg.isEmpty {
            switch type {
            case CrisisTypes.SOS: msg = "SOS — potrzebuję natychmiastowej pomocy."
            case CrisisTypes.LOCATION: msg = "Moja lokalizacja została udostępniona watahom."
            default: msg = "Komunikat od \(repo.me?.displayName ?? "watahy") — przekażcie dalej."
            }
        }
        if sendWithLocation && type != CrisisTypes.BROADCAST {
            LocationHelper.shared.requestOnce()
        }
        repo.reportCrisis(type: type, text: msg)
        text = ""
    }
}

struct CrisisCard: View {
    let signal: CrisisSignal
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 8) {
                Text(signal.type == CrisisTypes.SOS ? "🆘" : signal.type == CrisisTypes.LOCATION ? "📍" : "📢")
                Text(signal.type == CrisisTypes.SOS ? "SOS" : signal.type == CrisisTypes.LOCATION ? "Lokacja" : "Komunikat")
                    .font(.system(size: 11, weight: .heavy))
                    .foregroundColor(signal.type == CrisisTypes.SOS ? .white : WatahaColors.Ink)
                    .padding(.horizontal, 9).padding(.vertical, 4)
                    .background(Capsule().fill(signal.type == CrisisTypes.SOS ? WatahaColors.FlagRed : WatahaColors.LightGrey))
                Text(signal.userName).font(.system(size: 11.5, weight: .bold)).foregroundColor(WatahaColors.Ink)
                Spacer()
                if signal.pending { Text("⏳").font(.system(size: 11)) }
            }
            if !signal.text.isEmpty {
                Text(signal.text).font(.system(size: 13.5)).foregroundColor(WatahaColors.Ink.opacity(0.85))
            }
            Text("\(timeAgo(signal.createdAt)) • 📍 \(String(format: "%.3f, %.3f", signal.lat, signal.lng))")
                .font(.system(size: 10)).foregroundColor(WatahaColors.Grey)
        }
        .padding(12)
        .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(
            signal.type == CrisisTypes.SOS ? WatahaColors.FlagRed : WatahaColors.Border, lineWidth: 1))
    }
}
