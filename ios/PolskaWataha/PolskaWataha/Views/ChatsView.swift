import SwiftUI
import UIKit

// MARK: - Wiadomości (lista wątków)

struct ChatsView: View {
    @EnvironmentObject var repo: Repo
    @State private var newChat = false

    private var sorted: [ThreadRow] { repo.threads.sorted { $0.lastAt > $1.lastAt } }

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                ScrollView {
                    LazyVStack(spacing: 8) {
                        if sorted.isEmpty {
                            EmptyState(emoji: "💬", title: "Brak wiadomości",
                                       subtitle: "Wejdź w ogłoszenie i napisz do autora — wataha trzyma łączność lokalnie i offline.")
                        }
                        ForEach(sorted) { t in
                            NavigationLink(destination: ChatView(peerName: t.userName, peerId: t.userId, threadId: t.threadId)) {
                                HStack(spacing: 12) {
                                    AvatarView(name: t.userName, size: 46)
                                    VStack(alignment: .leading, spacing: 3) {
                                        Text(t.userName)
                                            .font(.system(size: 14.5, weight: .bold))
                                            .foregroundColor(WatahaColors.Ink)
                                        Text(t.lastText)
                                            .font(.system(size: 12.5))
                                            .foregroundColor(WatahaColors.Grey)
                                            .lineLimit(1)
                                    }
                                    Spacer()
                                    VStack(alignment: .trailing, spacing: 4) {
                                        Text(timeAgo(t.lastAt)).font(.system(size: 10.5)).foregroundColor(WatahaColors.Grey)
                                        if t.unread > 0 {
                                            Text("\(t.unread)")
                                                .font(.system(size: 11, weight: .bold))
                                                .foregroundColor(.white)
                                                .frame(width: 20, height: 20)
                                                .background(Circle().fill(WatahaColors.FlagRed))
                                        }
                                    }
                                }
                                .padding(12)
                                .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
                                .overlay(RoundedRectangle(cornerRadius: 14).stroke(WatahaColors.Border, lineWidth: 1))
                                .padding(.horizontal, 14)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .padding(.vertical, 10)
                }

                Button { newChat = true } label: {
                    Image(systemName: "square.and.pencil")
                        .font(.system(size: 19, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 54, height: 54)
                        .background(Circle().fill(WatahaColors.FlagRed))
                        .shadow(color: WatahaColors.FlagRed.opacity(0.45), radius: 10, y: 5)
                }
                .buttonStyle(PlainButtonStyle())
                .padding(.trailing, 20).padding(.bottom, 22)
            }
            .background(WatahaColors.LightGrey.opacity(0.4))
            .sheet(isPresented: $newChat) { NewChatSheet() }
        }
    }
}

// MARK: - Nowa rozmowa

struct NewChatSheet: View {
    @EnvironmentObject var repo: Repo
    @Environment(\.dismiss) private var dismiss
    @State private var text = ""
    @State private var picked: User?

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVStack(spacing: 8) {
                    SectionHeader(title: "Wybierz członka watahy")
                    ForEach(repo.users.filter { $0.id != repo.me?.id }) { u in
                        Button {
                            picked = u
                            text = ""
                        } label: {
                            HStack(spacing: 10) {
                                AvatarView(name: u.displayName, size: 38)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(u.displayName).font(.system(size: 13.5, weight: .bold)).foregroundColor(WatahaColors.Ink)
                                    HStack(spacing: 4) {
                                        Text("★").foregroundColor(WatahaColors.Amber).font(.system(size: 10))
                                        Text(String(format: "%.1f", u.reputation)).font(.system(size: 10.5)).foregroundColor(WatahaColors.Grey)
                                    }
                                }
                                Spacer()
                                if picked?.id == u.id {
                                    Image(systemName: "checkmark.circle.fill").foregroundColor(WatahaColors.FlagRed)
                                }
                            }
                            .padding(10)
                            .background(RoundedRectangle(cornerRadius: 12).fill(picked?.id == u.id ? WatahaColors.LightRed : Color.white))
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
                            .padding(.horizontal, 14)
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                    if let picked {
                        SectionHeader(title: "Pierwsza wiadomość")
                        TextEditor(text: $text)
                            .font(.system(size: 14))
                            .frame(minHeight: 80)
                            .padding(8)
                            .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
                            .padding(.horizontal, 16)
                        WatahaButton(title: "Wyślij") {
                            if !text.trimmingCharacters(in: .whitespaces).isEmpty {
                                try? repo.sendMessage(to: picked, text: text)
                                dismiss()
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }
                .padding(.bottom, 20)
            }
            .navigationTitle("Nowa rozmowa")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Anuluj") { dismiss() }.foregroundColor(WatahaColors.FlagRed)
                }
            }
        }
    }
}

// MARK: - Rozmowa 1:1

struct ChatView: View {
    @EnvironmentObject var repo: Repo
    @Environment(\.dismiss) private var dismiss
    let peerName: String
    let peerId: String
    let threadId: String
    @State private var text = ""
    @State private var showDictation = false
    @EnvironmentObject var wilk: WilkService

    private var msgs: [Message] { repo.messages(threadId) }

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 10) {
                AvatarView(name: peerName, size: 34)
                Text(peerName).font(.system(size: 15, weight: .bold)).foregroundColor(WatahaColors.Ink)
                Text("• \(repo.online ? "dostępny" : "offline / mesh")")
                    .font(.system(size: 11)).foregroundColor(WatahaColors.Grey)
                Spacer()
                Button {
                    repo.simulateReply(threadId: threadId)
                } label: {
                    Image(systemName: "arrow.clockwise").font(.system(size: 13, weight: .bold))
                        .foregroundColor(WatahaColors.FlagRed)
                        .padding(8).background(Circle().fill(WatahaColors.LightRed))
                }
                .buttonStyle(PlainButtonStyle())
                .accessibilityLabel("DEMO: symuluj odpowiedź rozmówcy")
            }
            .padding(.horizontal, 14).padding(.vertical, 8)
            .background(Color.white)
            Divider()

            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 6) {
                        ForEach(msgs) { m in
                            HStack {
                                if m.fromId == repo.me?.id { Spacer(minLength: 40) }
                                VStack(alignment: .leading, spacing: 3) {
                                    Text(m.text)
                                        .font(.system(size: 14))
                                        .foregroundColor(m.fromId == repo.me?.id ? .white : WatahaColors.Ink)
                                        .padding(.horizontal, 12).padding(.vertical, 8)
                                        .background(RoundedRectangle(cornerRadius: 14)
                                            .fill(m.fromId == repo.me?.id ? WatahaColors.FlagRed : WatahaColors.LightGrey))
                                    HStack(spacing: 4) {
                                        Text(timeAgo(m.createdAt)).font(.system(size: 9.5)).foregroundColor(WatahaColors.Grey)
                                        if m.pending { Text("⏳").font(.system(size: 9.5)) }
                                    }
                                }
                                if m.fromId != repo.me?.id { Spacer(minLength: 40) }
                            }
                            .id(m.id)
                            .padding(.horizontal, 12)
                        }
                    }
                    .padding(.vertical, 10)
                }
                .onChange(of: msgs.count) { _ in
                    if let last = msgs.last { proxy.scrollTo(last.id, anchor: .bottom) }
                }
                .onAppear {
                    repo.markThreadRead(threadId)
                    if let last = msgs.last { proxy.scrollTo(last.id, anchor: .bottom) }
                }
            }

            HStack(spacing: 8) {
                Button {
                    wilk.toggleListening { recognized in
                        if let recognized, !recognized.isEmpty {
                            text = recognized
                        }
                    }
                } label: {
                    Image(systemName: wilk.listening ? "waveform.circle.fill" : "mic.circle.fill")
                        .font(.system(size: 26))
                        .foregroundColor(wilk.listening ? WatahaColors.FlagRed : WatahaColors.Ink)
                }
                .buttonStyle(PlainButtonStyle())
                TextField("Wiadomość…", text: $text, axis: .vertical)
                    .font(.system(size: 14))
                    .lineLimit(1...4)
                    .padding(.horizontal, 12).padding(.vertical, 9)
                    .background(RoundedRectangle(cornerRadius: 18).fill(WatahaColors.LightGrey))
                Button {
                    let t = text.trimmingCharacters(in: .whitespaces)
                    guard !t.isEmpty else { return }
                    if let me = repo.me, let peer = repo.getUser(peerId) {
                        try? repo.sendMessage(to: peer, text: t)
                    }
                    text = ""
                } label: {
                    Image(systemName: "paperplane.fill")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 38, height: 38)
                        .background(Circle().fill(WatahaColors.FlagRed))
                }
                .buttonStyle(PlainButtonStyle())
            }
            .padding(10)
            .background(Color.white)
        }
        .navigationTitle(peerName)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { repo.markThreadRead(threadId) }
        .alert("Mikrofon niedostępny", isPresented: $wilk.speechDenied) {
            Button("OK", role: .cancel) {}
        } message: {
            Text("Włącz dostęp do mikrofonu i rozpoznawania mowy w Ustawieniach, aby dyktować wiadomości głosem.")
        }
    }
}
