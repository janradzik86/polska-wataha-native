import SwiftUI

// MARK: - WYMIANY (barter / pomoc lokalna)

struct ExchangeView: View {
    @EnvironmentObject var repo: Repo
    @State private var tab = 0   // 0 = dla mnie, 1 = moje propozycje

    private var incoming: [Exchange] {
        repo.exchanges.filter { $0.proposerId != repo.me?.id }
    }
    private var outgoing: [Exchange] {
        repo.exchanges.filter { $0.proposerId == repo.me?.id }
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 8) {
                FilterChip(text: "Propozycje do mnie", emoji: "📥", selected: tab == 0) { tab = 0 }
                FilterChip(text: "Moje propozycje", emoji: "📤", selected: tab == 1) { tab = 1 }
                Spacer()
            }
            .padding(.horizontal, 14).padding(.vertical, 10)
            .background(Color.white)

            ScrollView {
                LazyVStack(spacing: 10) {
                    let list = tab == 0 ? incoming : outgoing
                    if list.isEmpty {
                        EmptyState(emoji: "🤝", title: "Brak wymian",
                                   subtitle: "Zaproponuj wymianę pod ogłoszeniem na stronie głównej — lokalna wataha woli wymianę niż pieniądze.")
                    }
                    ForEach(list) { e in
                        ExchangeCard(exchange: e, isIncoming: tab == 0)
                            .padding(.horizontal, 14)
                    }
                    HStack {
                        Spacer()
                        Button {
                            if tab == 0 {
                                repo.simulateIncomingAccept(incoming.first?.id ?? "")
                            } else {
                                if let last = outgoing.first {
                                    repo.respondExchange(id: last.id, accept: true)
                                } else {
                                    repo.proposeExchange(post: Post(id: "demo", authorId: "u_warta", authorName: "Warta",
                                                                    type: PostType.GIVE.rawValue, title: "Demo wymiany",
                                                                    body: "", lat: 52.23, lng: 21.01, createdAt: nowMs()),
                                                          message: "Symulacja: propozycja testowa.")
                                }
                            }
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: "wand.and.stars")
                                Text("DEMO: symuluj zdarzenie")
                            }
                            .font(.system(size: 11.5, weight: .bold)).foregroundColor(WatahaColors.FlagRed)
                        }
                        .buttonStyle(PlainButtonStyle())
                        Spacer()
                    }
                    .padding(.vertical, 8)
                }
                .padding(.vertical, 10)
            }
        }
        .background(WatahaColors.LightGrey.opacity(0.4))
    }
}

struct ExchangeCard: View {
    @EnvironmentObject var repo: Repo
    let exchange: Exchange
    let isIncoming: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 8) {
                StatusPill(status: exchange.status)
                if exchange.pending { Text("⏳").font(.system(size: 12)) }
                Spacer()
                Text(timeAgo(exchange.createdAt)).font(.system(size: 10.5)).foregroundColor(WatahaColors.Grey)
            }
            Text("„\(exchange.postTitle)”")
                .font(.system(size: 14.5, weight: .bold)).foregroundColor(WatahaColors.Ink)
            Text(exchange.message)
                .font(.system(size: 12.5)).foregroundColor(WatahaColors.Grey)
            HStack {
                AvatarView(name: exchange.proposerName, size: 26)
                Text("\(exchange.proposerName) → \(ownerName)")
                    .font(.system(size: 11.5, weight: .semibold)).foregroundColor(WatahaColors.Ink)
                Spacer()
            }
            if isIncoming && exchange.status == ExchangeStatus.PENDING {
                HStack(spacing: 8) {
                    WatahaButton(title: "Przyjmij ✅", big: false) {
                        repo.respondExchange(id: exchange.id, accept: true)
                    }
                    WatahaButton(title: "Odrzuć", filled: false, big: false) {
                        repo.respondExchange(id: exchange.id, accept: false)
                    }
                }
            }
            if isIncoming && exchange.status != ExchangeStatus.PENDING {
                Button {
                    if let p = repo.post(exchange.postId) {
                        repo.simulateReply(threadId: "t_\([repo.me?.id ?? "", exchange.proposerId].sorted().joined(separator: "_"))")
                    }
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: "bubble.left.fill")
                        Text("Umów odbiór w wiadomościach")
                    }
                    .font(.system(size: 12, weight: .bold)).foregroundColor(WatahaColors.FlagRed)
                }
                .buttonStyle(PlainButtonStyle())
            }
        }
        .padding(13)
        .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(WatahaColors.Border, lineWidth: 1))
        .shadow(color: WatahaColors.Ink.opacity(0.04), radius: 4, y: 1)
    }
}

private extension ExchangeCard {
    var ownerName: String { repo.getUser(exchange.ownerId)?.displayName ?? "właściciel" }
}
