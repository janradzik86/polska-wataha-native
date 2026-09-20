import SwiftUI
import PolskaWatahaCore

// MARK: - Wejście aplikacji

@main
struct PolskaWatahaApp: App {
    @StateObject private var comm: CommManager
    @StateObject private var repo: Repo
    @StateObject private var wilk = WilkService()

    init() {
        let comm = CommManager()
        _comm = StateObject(wrappedValue: comm)
        _repo = StateObject(wrappedValue: Repo(db: Database(path: Self.dbPath()), comm: comm))
    }

    static func dbPath() -> String {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        return dir.appendingPathComponent("wataha.db").path
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(comm)
                .environmentObject(repo)
                .environmentObject(wilk)
                .preferredColorScheme(.light)
                .onAppear { NotificationHelper.requestAuth() }
        }
    }
}

// MARK: - Nawigacja główna (gate: logowanie → aplikacja)

struct RootView: View {
    @EnvironmentObject var repo: Repo
    @AppStorage("crisis_mode") private var crisisMode = false

    var body: some View {
        ZStack {
            if !repo.ready {
                SplashView()
            } else if repo.me == nil {
                AuthView()
            } else {
                MainView()
            }
            if crisisMode {
                VStack {
                    HStack(spacing: 8) {
                        Image(systemName: "exclamationmark.triangle.fill")
                        Text("TRYB KRYZYSOWY — WATAHA CZUWA")
                            .font(.system(size: 12, weight: .heavy))
                        Spacer()
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 14).padding(.vertical, 8)
                    .background(WatahaColors.DarkRed.edgesIgnoringSafeArea(.top))
                    .padding(.top, 0)
                    Spacer()
                }
                .transition(.move(edge: .top))
                .zIndex(50)
            }
        }
        .animation(.easeInOut(duration: 0.25), value: crisisMode)
    }
}

struct SplashView: View {
    @State private var pulse = false
    var body: some View {
        ZStack {
            LinearGradient(colors: [Color.white, WatahaColors.LightRed],
                           startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            VStack(spacing: 14) {
                Image("eagle")
                    .resizable().scaledToFit()
                    .frame(width: 120, height: 120)
                    .clipShape(Circle())
                    .shadow(color: WatahaColors.FlagRed.opacity(0.4), radius: 18, y: 6)
                    .scaleEffect(pulse ? 1.06 : 0.94)
                    .animation(.easeInOut(duration: 1.1).repeatForever(autoreverses: true), value: pulse)
                Text("POLSKA WATAHA!")
                    .font(.system(size: 30, weight: .black, design: .rounded))
                    .foregroundColor(WatahaColors.Ink)
                Text("CzarneWilkiPrawdy")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(WatahaColors.FlagRed)
                ProgressView().tint(WatahaColors.FlagRed).padding(.top, 10)
            }
        }
        .onAppear { pulse = true }
        .task {
            await repo.initAsync()
        }
    }
}

// MARK: - Ekran główny z szufladą (odpowiednik NavigationDrawer z V0.2)

enum MainSection: String, CaseIterable, Identifiable {
    case home, wilk, survival, exchange, chat, crisis, map, mesh, profile, admin, settings
    var id: String { rawValue }
    var title: String {
        switch self {
        case .home: return "Strona główna"
        case .wilk: return "WILK — asystent offline"
        case .survival: return "Poradnik przetrwania"
        case .exchange: return "Wymiany"
        case .chat: return "Wiadomości"
        case .crisis: return "Tryb kryzysowy"
        case .map: return "Mapa dobroci"
        case .mesh: return "MESH LAB"
        case .profile: return "Moje odznaki"
        case .admin: return "Pomysły → Administracja"
        case .settings: return "Ustawienia"
        }
    }
    var emoji: String {
        switch self {
        case .home: return "🏠"
        case .wilk: return "🐺"
        case .survival: return "🎒"
        case .exchange: return "🤝"
        case .chat: return "💬"
        case .crisis: return "🚨"
        case .map: return "🗺️"
        case .mesh: return "📡"
        case .profile: return "🦅"
        case .admin: return "💡"
        case .settings: return "⚙️"
        }
    }
}

struct MainView: View {
    @EnvironmentObject var repo: Repo
    @EnvironmentObject var comm: CommManager
    @State private var section: MainSection = .home
    @State private var drawerOpen = false

    var body: some View {
        ZStack(alignment: .leading) {
            VStack(spacing: 0) {
                WatahaTopBar(title: section.title, subtitle: "Wataha • Warszawa",
                             trailing: AnyView(onlineBadge), onMenu: { withAnimation { drawerOpen = true } })
                Divider()
                content
            }
            .zIndex(1)

            if drawerOpen {
                Color.black.opacity(0.35)
                    .ignoresSafeArea()
                    .zIndex(2)
                    .onTapGesture { withAnimation { drawerOpen = false } }
                    .transition(.opacity)
                DrawerView(selected: $section, open: $drawerOpen)
                    .transition(.move(edge: .leading))
                    .zIndex(3)
            }
        }
        .animation(.spring(response: 0.32, dampingFraction: 0.86), value: drawerOpen)
    }

    private var onlineBadge: some View {
        HStack(spacing: 5) {
            Circle().fill(repo.online ? WatahaColors.Green : WatahaColors.Grey).frame(width: 8, height: 8)
            Text(repo.online ? "ONLINE" : "OFFLINE")
                .font(.system(size: 10, weight: .heavy))
                .foregroundColor(repo.online ? WatahaColors.Green : WatahaColors.Grey)
            if repo.pendingCount > 0 {
                Text("\(repo.pendingCount)")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.white)
                    .frame(width: 18, height: 18)
                    .background(Circle().fill(WatahaColors.FlagRed))
            }
        }
        .padding(.horizontal, 9).padding(.vertical, 6)
        .background(Capsule().fill(WatahaColors.LightGrey))
    }

    @ViewBuilder private var content: some View {
        switch section {
        case .home: HomeView()
        case .wilk: AssistantView()
        case .survival: SurvivalView()
        case .exchange: ExchangeView()
        case .chat: ChatsView()
        case .crisis: CrisisView()
        case .map: WatahaMapView()
        case .mesh: MeshLabView()
        case .profile: ProfileView()
        case .admin: AdminFeedbackView()
        case .settings: SettingsView()
        }
    }
}
