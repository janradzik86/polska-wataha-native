import SwiftUI

/// Szuflada boczna (lewa) — zamykana przyciskiem ✕, identycznie jak Android V0.2.
struct DrawerView: View {
    @EnvironmentObject var repo: Repo
    @Binding var selected: MainSection
    @Binding var open: Bool
    @State private var confirmLogout = false

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 10) {
                        Image("eagle")
                            .resizable().scaledToFit()
                            .frame(width: 52, height: 52)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(WatahaColors.FlagRed, lineWidth: 2))
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Polska Wataha!")
                                .font(.system(size: 19, weight: .black))
                                .foregroundColor(WatahaColors.Ink)
                            Text("CzarneWilkiPrawdy")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(WatahaColors.FlagRed)
                        }
                    }
                    if let me = repo.me {
                        HStack(spacing: 10) {
                            AvatarView(name: me.displayName, size: 38)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(me.displayName).font(.system(size: 14, weight: .bold)).foregroundColor(WatahaColors.Ink)
                                HStack(spacing: 3) {
                                    Text("★").foregroundColor(WatahaColors.Amber).font(.system(size: 11))
                                    Text(String(format: "%.1f", me.reputation)).font(.system(size: 11, weight: .bold)).foregroundColor(WatahaColors.Ink)
                                    Text("• \(repo.reviews(me.id).count) odznak").font(.system(size: 11)).foregroundColor(WatahaColors.Grey)
                                }
                            }
                        }
                        .padding(8)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(RoundedRectangle(cornerRadius: 12).fill(WatahaColors.LightGrey))
                    }
                }
                Spacer()
                Button { withAnimation { open = false } } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(WatahaColors.Ink)
                        .frame(width: 34, height: 34)
                        .background(Circle().fill(WatahaColors.LightGrey))
                }
                .buttonStyle(PlainButtonStyle())
            }
            .padding(16)

            ScrollView {
                VStack(spacing: 2) {
                    ForEach(MainSection.allCases) { s in
                        DrawerRow(item: s, isSelected: selected == s) {
                            selected = s
                            withAnimation { open = false }
                        }
                    }
                }
                .padding(.horizontal, 10)
            }

            Spacer()

            VStack(spacing: 10) {
                HStack {
                    Image(systemName: "externaldrive.badge.clock")
                        .foregroundColor(WatahaColors.Grey)
                    Text(repo.pendingCount > 0 ? "Kolejka offline: \(repo.pendingCount) zaległych operacji" : "Wszystko zsynchronizowane")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(repo.pendingCount > 0 ? WatahaColors.Amber : WatahaColors.Grey)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 16)

                Button {
                    confirmLogout = true
                } label: {
                    HStack {
                        Image(systemName: "rectangle.portrait.and.arrow.right")
                        Text("Wyloguj się")
                            .font(.system(size: 14, weight: .bold))
                    }
                    .foregroundColor(WatahaColors.FlagRed)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 11)
                    .background(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.FlagRed, lineWidth: 1.2))
                }
                .buttonStyle(PlainButtonStyle())
                .padding(.horizontal, 16)

                Text("Polska Wataha! • iOS 0.2.0 • kotwica 🦅")
                    .font(.system(size: 10))
                    .foregroundColor(WatahaColors.Grey)
                    .padding(.bottom, 12)
            }
        }
        .background(Color.white)
        .frame(width: 300)
        .alert("Wylogować się?", isPresented: $confirmLogout) {
            Button("Anuluj", role: .cancel) {}
            Button("Wyloguj", role: .destructive) {
                repo.logout()
                withAnimation { open = false }
            }
        } message: {
            Text("Dane pozostaną na urządzeniu (offline).")
        }
    }
}

struct DrawerRow: View {
    let item: MainSection
    let isSelected: Bool
    let action: () -> Void
    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                Text(item.emoji).font(.system(size: 17))
                Text(item.title).font(.system(size: 14.5, weight: isSelected ? .bold : .medium))
                    .foregroundColor(isSelected ? .white : WatahaColors.Ink)
                Spacer()
                if item == .chat && unreadCount > 0 {
                    Text("\(unreadCount)")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 20, height: 20)
                        .background(Circle().fill(WatahaColors.FlagRed))
                }
            }
            .padding(.horizontal, 12).padding(.vertical, 11)
            .background(RoundedRectangle(cornerRadius: 12).fill(isSelected ? WatahaColors.FlagRed : Color.clear))
        }
        .buttonStyle(PlainButtonStyle())
        .padding(.horizontal, 4)
    }
    @EnvironmentObject var repo: Repo
    private var unreadCount: Int { repo.threads.reduce(0) { $0 + $1.unread } }
}
