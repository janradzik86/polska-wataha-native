import SwiftUI
import UIKit
import PhotosUI

// MARK: - Strona główna (styl OLX): ogłoszenia lokalne — oddaję / potrzebuję / pomogę

struct HomeView: View {
    @EnvironmentObject var repo: Repo
    @State private var query = ""
    @State private var filter: String? = nil     // nil = Wszystkie
    @State private var showCreate = false

    private var filtered: [Post] {
        var out = repo.posts.filter { $0.status == PostStatus.OPEN }
        if let filter { out = out.filter { $0.type == filter } }
        if !query.isEmpty {
            let q = query.lowercased()
            out = out.filter { $0.title.lowercased().contains(q) || $0.body.lowercased().contains(q) }
        }
        return out.sorted { $0.createdAt > $1.createdAt }
    }

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                ScrollView {
                    VStack(spacing: 10) {
                        hero
                        searchBar
                        chips

                        if filtered.isEmpty {
                            EmptyState(emoji: "📭", title: "Brak ogłoszeń",
                                       subtitle: "W tej chwili nic tu nie ma. Dodaj pierwsze ogłoszenie — lokalna wataha pomaga lokalnie.")
                        } else {
                            ForEach(filtered) { p in
                                PostCard(post: p)
                            }
                            HStack {
                                Spacer()
                                Button {
                                    Task { _ = await repo.syncNow() }
                                } label: {
                                    HStack(spacing: 4) {
                                        Image(systemName: "arrow.triangle.2.circlepath")
                                        Text("Odśwież")
                                    }
                                    .font(.system(size: 12, weight: .semibold))
                                    .foregroundColor(WatahaColors.Grey)
                                }
                                Spacer()
                            }
                            .padding(.vertical, 6)
                        }
                    }
                    .padding(.bottom, 90)
                }
                .refreshable { _ = await repo.syncNow() }

                Button { showCreate = true } label: {
                    Image(systemName: "plus")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 58, height: 58)
                        .background(Circle().fill(WatahaColors.FlagRed))
                        .shadow(color: WatahaColors.FlagRed.opacity(0.45), radius: 10, y: 5)
                }
                .buttonStyle(PlainButtonStyle())
                .padding(.trailing, 20).padding(.bottom, 22)
            }
            .background(WatahaColors.LightGrey.opacity(0.4))
            .sheet(isPresented: $showCreate) { CreatePostView() }
        }
    }

    private var hero: some View {
        HStack(spacing: 12) {
            Image("wolves")
                .resizable().scaledToFit()
                .frame(width: 92, height: 64)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            VStack(alignment: .leading, spacing: 4) {
                Text("Pomagaj lokalnie")
                    .font(.system(size: 17, weight: .heavy))
                    .foregroundColor(WatahaColors.Ink)
                Text("Oddaj rzecz • zgłoś potrzebę • zaoferuj pomoc — sąsiedzi z Twojej okolicy.")
                    .font(.system(size: 12))
                    .foregroundColor(WatahaColors.Grey)
                    .lineLimit(2)
            }
            Spacer()
        }
        .padding(12)
        .background(RoundedRectangle(cornerRadius: 16)
            .fill(LinearGradient(colors: [WatahaColors.LightRed, Color.white],
                                 startPoint: .topLeading, endPoint: .bottomTrailing)))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(WatahaColors.Border, lineWidth: 1))
        .padding(.horizontal, 14)
        .padding(.top, 10)
    }

    private var searchBar: some View {
        HStack(spacing: 8) {
            Image(systemName: "magnifyingglass").foregroundColor(WatahaColors.Grey)
            TextField("Czego szukasz w okolicy?", text: $query)
                .font(.system(size: 14))
                .autocorrectionDisabled()
            if !query.isEmpty {
                Button { query = "" } label: {
                    Image(systemName: "xmark.circle.fill").foregroundColor(WatahaColors.Grey)
                }.buttonStyle(PlainButtonStyle())
            }
        }
        .padding(.horizontal, 12).padding(.vertical, 10)
        .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
        .padding(.horizontal, 14)
    }

    private var chips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                FilterChip(text: "Wszystkie", emoji: "🌐", selected: filter == nil) { filter = nil }
                FilterChip(text: "Oddaję", emoji: "📦", selected: filter == PostType.GIVE.rawValue) { filter = PostType.GIVE.rawValue }
                FilterChip(text: "Potrzebuję", emoji: "🆘", selected: filter == PostType.NEED.rawValue) { filter = PostType.NEED.rawValue }
                FilterChip(text: "Mogę pomóc", emoji: "🤝", selected: filter == PostType.OFFER.rawValue) { filter = PostType.OFFER.rawValue }
            }
            .padding(.horizontal, 14)
        }
    }
}

// MARK: - Dodaj ogłoszenie

struct CreatePostView: View {
    @EnvironmentObject var repo: Repo
    @Environment(\.dismiss) private var dismiss

    @State private var type: PostType = .GIVE
    @State private var title = ""
    @State private var bodyText = ""
    @State private var photoItem: PhotosPickerItem?
    @State private var photoData: Data?
    @State private var showCamera = false
    @State private var useMyLocation = true
    @State private var error: String?
    @State private var publishing = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    SectionHeader(title: "Rodzaj ogłoszenia")
                    HStack(spacing: 8) {
                        ForEach(PostType.allCases, id: \.self) { t in
                            Button { type = t } label: {
                                HStack(spacing: 5) {
                                    Text(t.emoji)
                                    Text(t.label).font(.system(size: 12.5, weight: .bold))
                                }
                                .foregroundColor(type == t ? .white : WatahaColors.Ink)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 10)
                                .background(RoundedRectangle(cornerRadius: 10)
                                    .fill(type == t ? typeColor(t.rawValue) : Color.white))
                                .overlay(RoundedRectangle(cornerRadius: 10)
                                    .stroke(type == t ? Color.clear : WatahaColors.Border, lineWidth: 1))
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .padding(.horizontal, 16)

                    SectionHeader(title: "Zdjęcie (opcjonalnie)")
                    HStack(spacing: 10) {
                        if let photoData, let img = UIImage(data: photoData) {
                            Image(uiImage: img).resizable().scaledToFill()
                                .frame(width: 86, height: 86).clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        PhotosPicker(selection: $photoItem, matching: .images) {
                            HStack(spacing: 6) {
                                Image(systemName: "photo")
                                Text("Galeria")
                            }
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(WatahaColors.Ink)
                            .padding(.horizontal, 14).padding(.vertical, 10)
                            .background(RoundedRectangle(cornerRadius: 10).fill(WatahaColors.LightGrey))
                        }
                        Button {
                            showCamera = true
                        } label: {
                            HStack(spacing: 6) {
                                Image(systemName: "camera.fill")
                                Text("Aparat")
                            }
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(WatahaColors.Ink)
                            .padding(.horizontal, 14).padding(.vertical, 10)
                            .background(RoundedRectangle(cornerRadius: 10).fill(WatahaColors.LightGrey))
                        }
                        .buttonStyle(PlainButtonStyle())
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .fullScreenCover(isPresented: $showCamera) {
                        CameraPicker(image: $photoData).ignoresSafeArea()
                    }
                    .onChange(of: photoItem) { _ in
                        Task {
                            if let data = try? await photoItem?.loadTransferable(type: Data.self) {
                                photoData = data
                            }
                        }
                    }

                    SectionHeader(title: "Tytuł")
                    TextField("np. Powerbank 20 000 mAh do oddania", text: $title)
                        .font(.system(size: 14))
                        .padding(12)
                        .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
                        .padding(.horizontal, 16)

                    SectionHeader(title: "Opis")
                    TextEditor(text: $bodyText)
                        .font(.system(size: 14))
                        .frame(minHeight: 110)
                        .padding(8)
                        .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
                        .padding(.horizontal, 16)

                    SectionHeader(title: "Lokalizacja")
                    Toggle(isOn: $useMyLocation) {
                        Text("Użyj mojej lokalizacji").font(.system(size: 13, weight: .medium))
                    }
                    .tint(WatahaColors.FlagRed)
                    .padding(.horizontal, 16)
                    Text("Ogłoszenie będzie widoczne dla watahy w promieniu kilku km.")
                        .font(.system(size: 11)).foregroundColor(WatahaColors.Grey)
                        .padding(.horizontal, 16)

                    if let error {
                        Text(error).font(.system(size: 12, weight: .semibold))
                            .foregroundColor(WatahaColors.FlagRed).padding(.horizontal, 16)
                    }

                    WatahaButton(title: publishing ? "Publikowanie…" : "Opublikuj ogłoszenie", big: true) {
                        publish()
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .disabled(publishing)

                    Text(repo.online ? "🔵 Publikacja trafi od razu do sieci. Bez internetu — do kolejki offline i wyśle się automatycznie."
                                      : "🟠 Brak łączności: ogłoszenie zapisane lokalnie i trafi do kolejki sync.")
                        .font(.system(size: 11))
                        .foregroundColor(repo.online ? WatahaColors.Green : WatahaColors.Amber)
                        .padding(.horizontal, 16)
                }
                .padding(.bottom, 24)
            }
            .background(WatahaColors.LightGrey.opacity(0.4))
            .navigationTitle("Nowe ogłoszenie")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Anuluj") { dismiss() }.foregroundColor(WatahaColors.FlagRed)
                }
            }
        }
    }

    private func publish() {
        let t = title.trimmingCharacters(in: .whitespaces)
        guard t.count >= 4 else { error = "Podaj tytuł (min. 4 znaki)."; return }
        guard bodyText.trimmingCharacters(in: .whitespaces).count >= 10 else { error = "Opis powinien mieć min. 10 znaków."; return }
        var imagePath = ""
        if let photoData, let img = UIImage(data: photoData),
           let png = img.jpegData(compressionQuality: 0.8) {
            imagePath = PostImages.save(png: png, id: "tmp_\(nowMs())")
        }
        var lat = repo.myLocation().0, lng = repo.myLocation().1
        if useMyLocation, let loc = LocationHelper.shared.last {
            lat = loc.lat; lng = loc.lng
            repo.setMyLocation(lat: lat, lng: lng)
        }
        publishing = true
        _ = try? repo.createPost(type: type, title: t, body: bodyText, imagePath: imagePath, lat: lat, lng: lng)
        publishing = false
        dismiss()
    }
}

// MARK: - Szczegóły ogłoszenia

struct PostDetailView: View {
    @EnvironmentObject var repo: Repo
    let postId: String
    @State private var proposal = ""
    @State private var showPropose = false
    @State private var showAuthor = false
    @State private var showChat = false

    var body: some View {
        Group {
            if let post = repo.post(postId) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 14) {
                        PostThumb(post: post, size: UIScreen.main.bounds.width - 32)
                            .frame(maxWidth: .infinity)

                        VStack(alignment: .leading, spacing: 10) {
                            HStack {
                                Text(PostType(rawValue: post.type)?.label ?? post.type)
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 8).padding(.vertical, 4)
                                    .background(Capsule().fill(typeColor(post.type)))
                                StatusPill(status: post.status)
                                Spacer()
                                Text(timeAgo(post.createdAt)).font(.system(size: 11)).foregroundColor(WatahaColors.Grey)
                            }
                            Text(post.title).font(.system(size: 21, weight: .black)).foregroundColor(WatahaColors.Ink)
                            Text(post.body).font(.system(size: 14.5)).foregroundColor(WatahaColors.Ink.opacity(0.85))
                                .padding(.vertical, 2)

                            Divider()

                            Button { showAuthor = true } label: {
                                HStack(spacing: 10) {
                                    AvatarView(name: post.authorName, size: 40)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(post.authorName).font(.system(size: 14, weight: .bold)).foregroundColor(WatahaColors.Ink)
                                        Text("📍 \(distanceText(km: repo.distanceKm(lat: post.lat, lng: post.lng))) od Ciebie")
                                            .font(.system(size: 11)).foregroundColor(WatahaColors.Grey)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right").foregroundColor(WatahaColors.Grey).font(.system(size: 12))
                                }
                            }
                            .buttonStyle(PlainButtonStyle())

                            HStack(spacing: 10) {
                                if let author = repo.getUser(post.authorId), author.id != repo.me?.id {
                                    Button { showChat = true } label: {
                                        HStack { Image(systemName: "bubble.left.fill"); Text("Napisz") }
                                            .font(.system(size: 13, weight: .bold))
                                            .foregroundColor(WatahaColors.Ink)
                                            .frame(maxWidth: .infinity)
                                            .padding(.vertical, 11)
                                            .background(RoundedRectangle(cornerRadius: 12).fill(WatahaColors.LightGrey))
                                    }.buttonStyle(PlainButtonStyle())
                                }
                                if post.status == PostStatus.OPEN {
                                    WatahaButton(title: post.type == PostType.NEED.rawValue ? "Chcę pomóc 🤝" : "Zaproponuj wymianę 🤝") {
                                        showPropose = true
                                    }
                                    .frame(maxWidth: .infinity)
                                }
                            }

                            if post.authorId == repo.me?.id {
                                WatahaButton(title: "Zamknij ogłoszenie", filled: false) {
                                    repo.closePost(post.id)
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.bottom, 24)
                    }
                }
            } else {
                EmptyState(emoji: "🕳️", title: "Ogłoszenie niedostępne", subtitle: "Zostało usunięte lub zamknięte.")
            }
        }
        .navigationTitle("Ogłoszenie")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showPropose) { proposeSheet(post: repo.post(postId)) }
        .sheet(item: Binding(get: { showAuthor ? repo.getUser(repo.post(postId)?.authorId ?? "") : nil },
                             set: { showAuthor = $0 != nil })) { author in
            ProfileSheet(user: author)
        }
        .sheet(isPresented: $showChat) {
            if let post = repo.post(postId), let author = repo.getUser(post.authorId) {
                ChatView(peerName: author.displayName, peerId: author.id,
                         threadId: "t_\([repo.me!.id, author.id].sorted().joined(separator: "_"))")
            }
        }
    }

    @ViewBuilder
    private func proposeSheet(post: Post?) -> some View {
        if let post {
            NavigationStack {
                VStack(alignment: .leading, spacing: 12) {
                    Text("Propozycja dla „\(post.title)”")
                        .font(.system(size: 15, weight: .bold)).foregroundColor(WatahaColors.Ink)
                    Text("Napisz, co możesz zaoferować w zamian lub w jakiej formie pomożesz.")
                        .font(.system(size: 12)).foregroundColor(WatahaColors.Grey)
                    TextEditor(text: $proposal)
                        .font(.system(size: 14))
                        .frame(minHeight: 120)
                        .padding(8)
                        .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
                    WatahaButton(title: "Wyślij propozycję") {
                        try? repo.proposeExchange(post: post, message: proposal.isEmpty ? "Chcę pomóc — proszę o kontakt." : proposal)
                        showPropose = false
                        proposal = ""
                    }
                }
                .padding(16)
                .navigationTitle("Wymiana")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .topBarLeading) {
                        Button("Anuluj") { showPropose = false }.foregroundColor(WatahaColors.FlagRed)
                    }
                }
            }
        } else {
            EmptyView()
        }
    }
}

// MARK: - Karta profilu w arkuszu

struct ProfileSheet: View {
    @EnvironmentObject var repo: Repo
    let user: User
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 14) {
                    AvatarView(name: user.displayName, size: 84)
                    Text(user.displayName).font(.system(size: 20, weight: .heavy)).foregroundColor(WatahaColors.Ink)
                    HStack(spacing: 3) {
                        Text("★").foregroundColor(WatahaColors.Amber)
                        Text(String(format: "%.1f", user.reputation)).font(.system(size: 13, weight: .bold)).foregroundColor(WatahaColors.Ink)
                        Text("• \(repo.reviews(user.id).count) odznak").font(.system(size: 13)).foregroundColor(WatahaColors.Grey)
                    }
                    if !user.bio.isEmpty {
                        Text(user.bio).font(.system(size: 13)).foregroundColor(WatahaColors.Grey)
                            .multilineTextAlignment(.center).padding(.horizontal, 24)
                    }
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 96))], spacing: 10) {
                        ForEach(repo.reviews(user.id)) { b in
                            VStack(spacing: 4) {
                                Text(b.emoji).font(.system(size: 26))
                                Text(b.name).font(.system(size: 11, weight: .bold)).foregroundColor(WatahaColors.Ink)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 6)
                }
                .padding(.top, 20)
            }
            .navigationTitle("Profil")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

// MARK: - Aparat (natywny odpowiednik camera intent z Android)

struct CameraPicker: UIViewControllerRepresentable {
    @Binding var image: Data?
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let c = UIImagePickerController()
        c.sourceType = .camera
        c.cameraCaptureMode = .photo
        c.delegate = context.coordinator
        return c
    }
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    func makeCoordinator() -> Coordinator { Coordinator(self) }

    final class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: CameraPicker
        init(_ p: CameraPicker) { parent = p }
        func imagePickerController(_ picker: UIImagePickerController,
                                   didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let img = info[.originalImage] as? UIImage {
                parent.image = img.jpegData(compressionQuality: 0.8)
            }
            parent.dismiss()
        }
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.dismiss()
        }
    }
}
