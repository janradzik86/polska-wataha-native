import SwiftUI

// MARK: - MOJE ODZNAKI / PROFIL

struct ProfileView: View {
    @EnvironmentObject var repo: Repo
    @State private var confirmLogout = false

    private var myBadges: [Badge] { repo.me.map { repo.reviews($0.id) } ?? [] }
    private var myPosts: [Post] { repo.posts.filter { $0.authorId == repo.me?.id } }

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                if let me = repo.me {
                    // karta użytkownika
                    VStack(spacing: 10) {
                        AvatarView(name: me.displayName, size: 92)
                        Text(me.displayName).font(.system(size: 21, weight: .black)).foregroundColor(WatahaColors.Ink)
                        Text("@\(me.username)").font(.system(size: 12.5)).foregroundColor(WatahaColors.Grey)
                        HStack(spacing: 10) {
                            statBox(value: String(format: "%.1f", me.reputation), label: "reputacja", emoji: "★")
                            statBox(value: "\(myPosts.count)", label: "ogłoszenia", emoji: "📦")
                            statBox(value: "\(myBadges.count)", label: "odznaki", emoji: "🦅")
                        }
                        if !me.bio.isEmpty {
                            Text(me.bio).font(.system(size: 12.5)).foregroundColor(WatahaColors.Grey)
                                .multilineTextAlignment(.center).padding(.horizontal, 24)
                        }
                    }
                    .padding(.vertical, 16)
                    .frame(maxWidth: .infinity)
                    .background(RoundedRectangle(cornerRadius: 18).fill(Color.white))
                    .overlay(RoundedRectangle(cornerRadius: 18).stroke(WatahaColors.Border, lineWidth: 1))
                    .padding(.horizontal, 14).padding(.top, 10)

                    // odznaki
                    SectionHeader(title: "Moje odznaki", emoji: "🏅")
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 100))], spacing: 8) {
                        ForEach(myBadges) { b in
                            VStack(spacing: 5) {
                                Text(b.emoji).font(.system(size: 30))
                                Text(b.name).font(.system(size: 11.5, weight: .bold)).foregroundColor(WatahaColors.Ink)
                                Text(b.desc).font(.system(size: 9)).foregroundColor(WatahaColors.Grey)
                                    .multilineTextAlignment(.center)
                                Text(timeAgo(b.earnedAt)).font(.system(size: 8.5)).foregroundColor(WatahaColors.Grey)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
                            .overlay(RoundedRectangle(cornerRadius: 14).stroke(WatahaColors.Border, lineWidth: 1))
                        }
                    }
                    .padding(.horizontal, 14)

                    // moje ogłoszenia
                    SectionHeader(title: "Moje ogłoszenia (\(myPosts.count))", emoji: "📌")
                    if myPosts.isEmpty {
                        Text("Nie masz jeszcze ogłoszeń. Dodaj pierwsze na stronie głównej!")
                            .font(.system(size: 12)).foregroundColor(WatahaColors.Grey)
                            .padding(.horizontal, 20).padding(.vertical, 8)
                    } else {
                        ForEach(myPosts) { p in
                            PostCard(post: p)
                        }
                    }

                    WatahaButton(title: "Wyloguj się", filled: false) {
                        confirmLogout = true
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)

                    Text("Konto lokalne • dane na urządzeniu (offline-first) • wersja 0.2.0")
                        .font(.system(size: 10)).foregroundColor(WatahaColors.Grey)
                }
            }
            .padding(.bottom, 24)
        }
        .background(WatahaColors.LightGrey.opacity(0.4))
        .alert("Wylogować się?", isPresented: $confirmLogout) {
            Button("Anuluj", role: .cancel) {}
            Button("Wyloguj", role: .destructive) { repo.logout() }
        }
    }

    private func statBox(value: String, label: String, emoji: String) -> some View {
        VStack(spacing: 3) {
            Text("\(emoji) \(value)").font(.system(size: 15, weight: .heavy)).foregroundColor(WatahaColors.Ink)
            Text(label).font(.system(size: 10)).foregroundColor(WatahaColors.Grey)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(RoundedRectangle(cornerRadius: 12).fill(WatahaColors.LightGrey))
    }
}
