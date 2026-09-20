import SwiftUI

// MARK: - LOGOWANIE / REJESTRACJA (domyślnie normalne konto — jak w V0.2)

struct AuthView: View {
    @EnvironmentObject var repo: Repo
    @State private var tab = 0            // 0 = logowanie, 1 = rejestracja
    @State private var username = ""
    @State private var password = ""
    @State private var displayName = ""
    @State private var phone = ""
    @State private var busy = false
    @State private var error: String?

    var body: some View {
        ZStack {
            LinearGradient(colors: [Color.white, WatahaColors.LightRed],
                           startPoint: .top, endPoint: .bottom).ignoresSafeArea()

            ScrollView {
                VStack(spacing: 16) {
                    Image("eagle")
                        .resizable().scaledToFit()
                        .frame(width: 110, height: 110)
                        .clipShape(Circle())
                        .overlay(Circle().stroke(WatahaColors.FlagRed, lineWidth: 3))
                        .shadow(color: WatahaColors.FlagRed.opacity(0.3), radius: 14, y: 5)

                    Text("POLSKA WATAHA!")
                        .font(.system(size: 28, weight: .black, design: .rounded))
                        .foregroundColor(WatahaColors.Ink)
                    Text("CzarneWilkiPrawdy • kotwica 🦅")
                        .font(.system(size: 12.5, weight: .bold))
                        .foregroundColor(WatahaColors.FlagRed)

                    HStack(spacing: 0) {
                        tabButton("Logowanie", selected: tab == 0) { tab = 0 }
                        tabButton("Rejestracja", selected: tab == 1) { tab = 1 }
                    }
                    .padding(4)
                    .background(RoundedRectangle(cornerRadius: 13).fill(Color.white))
                    .overlay(RoundedRectangle(cornerRadius: 13).stroke(WatahaColors.Border, lineWidth: 1))

                    VStack(spacing: 10) {
                        if tab == 1 {
                            field("Imię / pseudonim", text: $displayName)
                            field("Telefon (opcjonalnie)", text: $phone)
                        }
                        field("Nazwa użytkownika", text: $username)
                        SecureField("Hasło (min. 4 znaki)", text: $password)
                            .font(.system(size: 14))
                            .padding(12)
                            .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
                        if let error {
                            Text(error).font(.system(size: 12, weight: .semibold))
                                .foregroundColor(WatahaColors.FlagRed)
                        }
                        WatahaButton(title: busy ? "Przetwarzanie…" : (tab == 0 ? "Zaloguj się" : "Załóż konto w Watahy"), big: true) {
                            submit()
                        }
                        .disabled(busy)

                        HStack(spacing: 6) {
                            Text("𝕏").foregroundColor(WatahaColors.Grey)
                            Text("Konto demo: cwilk / haslo123 — lokalna wataha, pełne dane.")
                                .font(.system(size: 11)).foregroundColor(WatahaColors.Grey)
                        }
                        .padding(.horizontal, 20)

                        VStack(alignment: .leading, spacing: 6) {
                            Text("Dlaczego warto dołączyć?")
                                .font(.system(size: 12, weight: .heavy)).foregroundColor(WatahaColors.Ink)
                            FeatureRow("📦", "Wymiana darów i usług z sąsiadami — bez pieniędzy")
                            FeatureRow("🐺", "WILK — asystent offline: kryzys, przetrwanie, pierwsza pomoc")
                            FeatureRow("📡", "Sieć mesh A–D — działa, gdy zniknie internet")
                        }
                        .padding(12)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(RoundedRectangle(cornerRadius: 14).fill(Color.white.opacity(0.7)))
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(WatahaColors.Border, lineWidth: 1))
                    }
                    .padding(.horizontal, 20)
                }
                .padding(.vertical, 24)
            }
        }
    }

    private func tabButton(_ t: String, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(t)
                .font(.system(size: 13.5, weight: .bold))
                .foregroundColor(selected ? .white : WatahaColors.Ink)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 9)
                .background(RoundedRectangle(cornerRadius: 10).fill(selected ? WatahaColors.FlagRed : Color.clear))
        }
        .buttonStyle(PlainButtonStyle())
    }

    private func field(_ label: String, text: Binding<String>) -> some View {
        TextField(label, text: text)
            .font(.system(size: 14))
            .autocorrectionDisabled()
            .textInputAutocapitalization(.never)
            .padding(12)
            .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
    }

    private func submit() {
        error = nil
        let u = username.trimmingCharacters(in: .whitespaces)
        let p = password
        guard !u.isEmpty, !p.isEmpty else { error = "Podaj nazwę użytkownika i hasło."; return }
        busy = true
        Task {
            if tab == 0 {
                let res = await repo.login(username: u, password: p)
                if case .failure(let e) = res { error = e.localizedDescription }
            } else {
                let res = repo.register(displayName: displayName.isEmpty ? u : displayName,
                                        username: u, phone: phone, password: p)
                if case .failure(let e) = res { error = e.localizedDescription }
            }
            busy = false
        }
    }
}

struct FeatureRow: View {
    let emoji: String
    let text: String
    init(_ emoji: String, _ text: String) { self.emoji = emoji; self.text = text }
    var body: some View {
        HStack(spacing: 8) {
            Text(emoji).font(.system(size: 14))
            Text(text).font(.system(size: 11.5)).foregroundColor(WatahaColors.Ink.opacity(0.85))
        }
    }
}
