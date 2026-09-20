import SwiftUI

// MARK: - „Masz pomysł? Napisz!” — zgłoszenia do Administracji

struct AdminFeedbackView: View {
    @EnvironmentObject var repo: Repo
    @State private var subject = "Pomysł na funkcję"
    @State private var text = ""
    @State private var sent = false

    private let subjects = ["Pomysł na funkcję", "Zgłoszenie błędu", "Bezpieczeństwo", "Inne"]

    private var items: [FeedbackItem] { repo.feedback.sorted { $0.createdAt > $1.createdAt } }

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                HStack(spacing: 12) {
                    Text("💡").font(.system(size: 36))
                    VStack(alignment: .leading, spacing: 3) {
                        Text("MASZ POMYSŁ? NAPISZ!")
                            .font(.system(size: 16, weight: .black)).foregroundColor(WatahaColors.Ink)
                        Text("Administracja Watahy czyta każde zgłoszenie — funkcje z roadmapy powstają z pomysłów takich jak Twój.")
                            .font(.system(size: 11.5)).foregroundColor(WatahaColors.Grey)
                    }
                    Spacer()
                }
                .padding(14)
                .background(RoundedRectangle(cornerRadius: 16).fill(WatahaColors.LightRed))
                .padding(.horizontal, 14).padding(.top, 10)

                VStack(alignment: .leading, spacing: 10) {
                    Text("Kategoria").font(.system(size: 12, weight: .bold)).foregroundColor(WatahaColors.Ink)
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(subjects, id: \.self) { s in
                                FilterChip(text: s, emoji: s == "Pomysł na funkcję" ? "💡" : s == "Zgłoszenie błędu" ? "🐞" : s == "Bezpieczeństwo" ? "🛡️" : "✉️",
                                           selected: subject == s) { subject = s }
                            }
                        }
                    }
                    Text("Co masz na myśli?").font(.system(size: 12, weight: .bold)).foregroundColor(WatahaColors.Ink)
                    TextEditor(text: $text)
                        .font(.system(size: 14))
                        .frame(minHeight: 110)
                        .padding(8)
                        .background(RoundedRectangle(cornerRadius: 12).fill(Color.white))
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(WatahaColors.Border, lineWidth: 1))
                    WatahaButton(title: sent ? "Wysłano! 💪" : "Wyślij do Administracji") {
                        guard text.trimmingCharacters(in: .whitespaces).count >= 10 else { return }
                        repo.submitFeedback(subject: subject, text: text.trimmingCharacters(in: .whitespaces))
                        text = ""
                        sent = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) { sent = false }
                    }
                }
                .padding(14)
                .background(RoundedRectangle(cornerRadius: 16).fill(Color.white))
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(WatahaColors.Border, lineWidth: 1))
                .padding(.horizontal, 14)

                SectionHeader(title: "Twoje zgłoszenia (\(items.count))", emoji: "🗂️")
                ForEach(items) { f in
                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            Text(f.subject)
                                .font(.system(size: 12, weight: .heavy))
                                .foregroundColor(WatahaColors.Ink)
                                .padding(.horizontal, 8).padding(.vertical, 3)
                                .background(Capsule().fill(WatahaColors.LightGrey))
                            Spacer()
                            Text(f.pending ? "⏳ w kolejce offline" : "✅ wysłane")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(f.pending ? WatahaColors.Amber : WatahaColors.Green)
                        }
                        Text(f.text).font(.system(size: 13)).foregroundColor(WatahaColors.Ink.opacity(0.85))
                        Text(timeAgo(f.createdAt)).font(.system(size: 10)).foregroundColor(WatahaColors.Grey)
                    }
                    .padding(12)
                    .background(RoundedRectangle(cornerRadius: 14).fill(Color.white))
                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(WatahaColors.Border, lineWidth: 1))
                    .padding(.horizontal, 14)
                }

                if repo.pendingCount > 0 {
                    Button {
                        Task { _ = await repo.syncNow() }
                    } label: {
                        HStack(spacing: 6) {
                            Image(systemName: "arrow.triangle.2.circlepath")
                            Text("Synchronizuj kolejke (\(repo.pendingCount))")
                        }
                        .font(.system(size: 12, weight: .bold)).foregroundColor(WatahaColors.FlagRed)
                    }
                    .buttonStyle(PlainButtonStyle())
                    .padding(.vertical, 6)
                }
            }
            .padding(.bottom, 20)
        }
        .background(WatahaColors.LightGrey.opacity(0.4))
    }
}
