import SwiftUI

// MARK: - Wspólne komponenty (styl OLX × biało-czerwony, Kotwica, CzarneWilkiPrawdy)

struct WatahaButton: View {
    let title: String
    var filled = true
    var big = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: big ? 18 : 15, weight: .bold))
                .foregroundColor(filled ? .white : WatahaColors.FlagRed)
                .frame(maxWidth: .infinity)
                .padding(.vertical, big ? 16 : 11)
                .background(RoundedRectangle(cornerRadius: big ? 16 : 12)
                    .fill(filled ? WatahaColors.FlagRed : Color.white))
                .overlay(RoundedRectangle(cornerRadius: big ? 16 : 12)
                    .stroke(WatahaColors.FlagRed, lineWidth: filled ? 0 : 1.5))
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct SectionHeader: View {
    let title: String
    var emoji: String? = nil
    var body: some View {
        HStack(spacing: 6) {
            if let emoji { Text(emoji).font(.system(size: 15)) }
            Text(title)
                .font(.system(size: 13, weight: .bold))
                .foregroundColor(WatahaColors.Ink)
                .textCase(nil)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.top, 14)
        .padding(.bottom, 4)
    }
}

struct EmptyState: View {
    let emoji: String
    let title: String
    let subtitle: String
    var body: some View {
        VStack(spacing: 10) {
            Text(emoji).font(.system(size: 46))
            Text(title).font(.system(size: 17, weight: .bold)).foregroundColor(WatahaColors.Ink)
            Text(subtitle).font(.system(size: 13)).foregroundColor(WatahaColors.Grey)
                .multilineTextAlignment(.center).padding(.horizontal, 32)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 60)
    }
}

struct AvatarView: View {
    let name: String
    let size: CGFloat
    var initials: String {
        let parts = name.split(separator: " ")
        let a = parts.first.map { String($0.prefix(1)) } ?? "?"
        let b = parts.count > 1 ? String(parts[1].prefix(1)) : ""
        return (a + b).uppercased()
    }
    var body: some View {
        ZStack {
            Circle().fill(LinearGradient(colors: [WatahaColors.FlagRed, WatahaColors.DarkRed],
                                         startPoint: .topLeading, endPoint: .bottomTrailing))
            Text(initials.uppercased())
                .font(.system(size: size * 0.4, weight: .bold))
                .foregroundColor(.white)
        }
        .frame(width: size, height: size)
    }
}

struct StatusPill: View {
    let status: String
    var body: some View {
        let (bg, fg) = pillColors(status)
        Text(label)
            .font(.system(size: 10, weight: .bold))
            .foregroundColor(fg)
            .padding(.horizontal, 8).padding(.vertical, 3)
            .background(Capsule().fill(bg))
    }
    private var label: String {
        switch status {
        case PostStatus.OPEN: return "Aktywne"
        case PostStatus.CLOSED: return "Zamknięte"
        case ExchangeStatus.PENDING: return "Oczekuje"
        case ExchangeStatus.ACCEPTED: return "Przyjęta"
        case ExchangeStatus.REJECTED: return "Odrzucona"
        default: return status
        }
    }
    private func pillColors(_ s: String) -> (Color, Color) {
        switch s {
        case ExchangeStatus.ACCEPTED: return (Color.green.opacity(0.15), WatahaColors.Green)
        case ExchangeStatus.REJECTED: return (WatahaColors.LightRed, WatahaColors.FlagRed)
        case PostStatus.CLOSED: return (WatahaColors.LightGrey, WatahaColors.Grey)
        default: return (WatahaColors.LightRed, WatahaColors.FlagRed)
        }
    }
}

struct FilterChip: View {
    let text: String
    let emoji: String
    let selected: Bool
    let action: () -> Void
    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Text(emoji).font(.system(size: 13))
                Text(text).font(.system(size: 13, weight: selected ? .bold : .medium))
            }
            .foregroundColor(selected ? .white : WatahaColors.Ink)
            .padding(.horizontal, 12).padding(.vertical, 7)
            .background(Capsule().fill(selected ? WatahaColors.FlagRed : Color.white))
            .overlay(Capsule().stroke(selected ? Color.clear : WatahaColors.Border, lineWidth: 1))
        }
        .buttonStyle(PlainButtonStyle())
    }
}

// MARK: - Pasek górny z przyciskiem menu (odpowiednik AppBar + hamburgera z V0.2)

struct WatahaTopBar: View {
    @EnvironmentObject var repo: Repo
    let title: String
    var subtitle: String? = nil
    var trailing: AnyView? = nil
    let onMenu: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onMenu) {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(WatahaColors.Ink)
                    .frame(width: 40, height: 40)
                    .background(RoundedRectangle(cornerRadius: 12).fill(WatahaColors.LightGrey))
            }
            .buttonStyle(PlainButtonStyle())
            VStack(alignment: .leading, spacing: 1) {
                Text(title).font(.system(size: 19, weight: .heavy)).foregroundColor(WatahaColors.Ink)
                if let subtitle {
                    HStack(spacing: 5) {
                        Circle().fill(repo.online ? WatahaColors.Green : WatahaColors.Grey).frame(width: 7, height: 7)
                        Text(repo.online ? subtitle : "\(subtitle) • TRYB OFFLINE")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(repo.online ? WatahaColors.Green : WatahaColors.Grey)
                    }
                }
            }
            Spacer()
            if let trailing { trailing }
        }
        .padding(.horizontal, 14).padding(.vertical, 8)
        .background(Color.white)
    }
}

// MARK: - Karta ogłoszenia w stylu OLX

struct PostCard: View {
    @EnvironmentObject var repo: Repo
    let post: Post

    var body: some View {
        NavigationLink(destination: PostDetailView(postId: post.id)) {
            VStack(alignment: .leading, spacing: 0) {
                HStack(alignment: .top, spacing: 12) {
                    PostThumb(post: post, size: 86)
                    VStack(alignment: .leading, spacing: 5) {
                        HStack {
                            Text(PostType(rawValue: post.type)?.label ?? post.type)
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 7).padding(.vertical, 3)
                                .background(Capsule().fill(typeColor(post.type)))
                            Spacer()
                            StatusPill(status: post.status)
                            if post.pending {
                                Text("⏳").font(.system(size: 12))
                            }
                        }
                        Text(post.title)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(WatahaColors.Ink)
                            .lineLimit(2)
                        Text(post.body)
                            .font(.system(size: 12.5))
                            .foregroundColor(WatahaColors.Grey)
                            .lineLimit(2)
                    }
                }
                HStack(spacing: 8) {
                    Text("👤 \(post.authorName)")
                    Text("•")
                    Text(timeAgo(post.createdAt))
                    Text("•")
                    Text(distanceText(km: repo.distanceKm(lat: post.lat, lng: post.lng)))
                    Spacer()
                }
                .font(.system(size: 11, weight: .medium))
                .foregroundColor(WatahaColors.Grey)
                .padding(.top, 8)
            }
            .padding(12)
            .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
            .shadow(color: WatahaColors.Ink.opacity(0.06), radius: 6, y: 2)
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(WatahaColors.Border, lineWidth: 1))
        }
        .buttonStyle(PlainButtonStyle())
        .padding(.horizontal, 14)
        .padding(.bottom, 10)
    }
}

func typeColor(_ t: String) -> Color {
    switch t {
    case PostType.GIVE.rawValue: return WatahaColors.FlagRed
    case PostType.NEED.rawValue: return WatahaColors.Amber
    case PostType.OFFER.rawValue: return WatahaColors.Green
    default: return WatahaColors.Grey
    }
}

struct PostThumb: View {
    let post: Post
    let size: CGFloat
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 12)
                .fill(LinearGradient(colors: [WatahaColors.LightRed, Color.white],
                                     startPoint: .topLeading, endPoint: .bottomTrailing))
            if let img = PostImages.load(posts: [post], id: post.id) {
                Image(uiImage: img).resizable().scaledToFill()
                    .frame(width: size, height: size)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            } else {
                Text(PostType(rawValue: post.type)?.emoji ?? "📦")
                    .font(.system(size: size * 0.42))
            }
        }
        .frame(width: size, height: size)
    }
}

/// Zapisywane zdjęcia ogłoszeń (Documents — offline).
enum PostImages {
    static func dir() -> URL {
        let u = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("post_photos", isDirectory: true)
        try? FileManager.default.createDirectory(at: u, withIntermediateDirectories: true)
        return u
    }
    @discardableResult
    static func save(png: Data, id: String) -> String {
        let url = dir().appendingPathComponent("\(id).png")
        try? png.write(to: url)
        return url.path
    }
    static func load(posts: [Post], id: String) -> UIImage? {
        guard let post = posts.first(where: { $0.id == id }), !post.imagePath.isEmpty else { return nil }
        let url = URL(fileURLWithPath: post.imagePath)
        guard FileManager.default.fileExists(atPath: url.path) else { return nil }
        return UIImage(contentsOfFile: url.path)
    }
}
