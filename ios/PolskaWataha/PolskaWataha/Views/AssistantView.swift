import SwiftUI
import PolskaWatahaCore

// MARK: - ASYSTENT WILK — offline, deterministyczny, głos w/wyjście

struct AssistantView: View {
    @EnvironmentObject var wilk: WilkService
    @EnvironmentObject var repo: Repo
    @State private var draft = ""
    @State private var showQuick = true

    var body: some View {
        VStack(spacing: 0) {
            // nagłówek WILK
            HStack(spacing: 10) {
                Image("wolves")
                    .resizable().scaledToFit().frame(width: 44, height: 34)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                VStack(alignment: .leading, spacing: 1) {
                    Text("WILK — asystent Watahy").font(.system(size: 14.5, weight: .heavy)).foregroundColor(WatahaColors.Ink)
                    HStack(spacing: 4) {
                        Circle().fill(WatahaColors.Green).frame(width: 6, height: 6)
                        Text("⚡ działa w 100% offline • bez internetu").font(.system(size: 10)).foregroundColor(WatahaColors.Grey)
                    }
                }
                Spacer()
                Button {
                    wilk.clearHistory()
                    showQuick = true
                } label: {
                    Image(systemName: "trash").font(.system(size: 13, weight: .bold)).foregroundColor(WatahaColors.Grey)
                        .padding(8).background(Circle().fill(WatahaColors.LightGrey))
                }
                .buttonStyle(PlainButtonStyle())
            }
            .padding(.horizontal, 14).padding(.vertical, 8)
            .background(Color.white)
            Divider()

            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(wilk.history) { m in
                            WilkBubble(m: m)
                        }
                        if showQuick {
                            SectionHeader(title: "Szybkie pytania")
                            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                                ForEach(WilkService.QUICK, id: \.self) { q in
                                    Button {
                                        draft = q
                                    } label: {
                                        Text(q)
                                            .font(.system(size: 11.5, weight: .medium))
                                            .foregroundColor(WatahaColors.Ink)
                                            .multilineTextAlignment(.leading)
                                            .frame(maxWidth: .infinity, alignment: .leading)
                                            .padding(10)
                                            .background(RoundedRectangle(cornerRadius: 10).fill(Color.white))
                                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(WatahaColors.Border, lineWidth: 1))
                                    }
                                    .buttonStyle(PlainButtonStyle())
                                }
                            }
                            .padding(.horizontal, 14)
                        }
                    }
                    .padding(.vertical, 10)
                }
                .onChange(of: wilk.history.count) { _ in
                    if let last = wilk.history.last {
                        withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                        showQuick = false
                    }
                }
                .onAppear {
                    if let last = wilk.history.last { proxy.scrollTo(last.id, anchor: .bottom) }
                }
            }

            // pasek wejścia + mikrofon
            HStack(alignment: .bottom, spacing: 8) {
                Button {
                    wilk.toggleListening { recognized in
                        if let recognized, !recognized.isEmpty {
                            draft = recognized
                            ask()
                        }
                    }
                } label: {
                    ZStack {
                        Circle().fill(wilk.listening ? WatahaColors.FlagRed : WatahaColors.LightRed)
                            .frame(width: 46, height: 46)
                        Image(systemName: wilk.listening ? "waveform" : "mic.fill")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(wilk.listening ? .white : WatahaColors.FlagRed)
                    }
                }
                .buttonStyle(PlainButtonStyle())

                TextField("Zapytaj WILK-a…", text: $draft, axis: .vertical)
                    .font(.system(size: 14))
                    .lineLimit(1...4)
                    .padding(.horizontal, 12).padding(.vertical, 10)
                    .background(RoundedRectangle(cornerRadius: 18).fill(WatahaColors.LightGrey))
                    .onSubmit { ask() }

                Button { ask() } label: {
                    Image(systemName: "arrow.up.circle.fill")
                        .font(.system(size: 30))
                        .foregroundColor(draft.isEmpty ? WatahaColors.Grey : WatahaColors.FlagRed)
                }
                .buttonStyle(PlainButtonStyle())
                .disabled(draft.isEmpty)
            }
            .padding(10)
            .background(Color.white)

            if wilk.speaking || wilk.listening {
                HStack(spacing: 8) {
                    ProgressView().tint(WatahaColors.FlagRed).scaleEffect(0.7)
                    Text(wilk.listening ? "Słucham… mów teraz" : "WILK mówi…")
                        .font(.system(size: 11.5, weight: .semibold))
                        .foregroundColor(WatahaColors.FlagRed)
                    Spacer()
                    Button("Stop") { wilk.stopSpeaking() }
                        .font(.system(size: 11.5, weight: .bold)).foregroundColor(WatahaColors.Grey)
                }
                .padding(.horizontal, 14).padding(.bottom, 6)
                .transition(.opacity)
            }
        }
        .background(WatahaColors.LightGrey.opacity(0.4))
        .alert("Mikrofon niedostępny", isPresented: $wilk.speechDenied) {
            Button("OK", role: .cancel) {}
        } message: {
            Text("Rozpoznawanie mowy wymaga zgody w Ustawieniach. Asystent działa też tekstowo — wpisz pytanie poniżej.")
        }
    }

    private func ask() {
        let t = draft.trimmingCharacters(in: .whitespaces)
        guard !t.isEmpty else { return }
        draft = ""
        wilk.ask(t)
        if let last = wilk.history.last {
            // scroll obsłuży onChange
            _ = last
        }
    }
}

struct WilkBubble: View {
    @EnvironmentObject var wilk: WilkService
    let m: WilkService.WilkMessage

    var body: some View {
        HStack {
            if m.role == "user" { Spacer(minLength: 50) }
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 4) {
                    if m.role == "wilk" {
                        Text("🐺").font(.system(size: 10))
                        Text("WILK").font(.system(size: 9.5, weight: .heavy)).foregroundColor(WatahaColors.FlagRed)
                        if let topic = m.topic {
                            Text("• \(topic)").font(.system(size: 9.5, weight: .semibold)).foregroundColor(WatahaColors.Grey)
                        }
                    }
                }
                Text(m.text)
                    .font(.system(size: 14))
                    .foregroundColor(m.role == "user" ? .white : WatahaColors.Ink)
                    .padding(.horizontal, 12).padding(.vertical, 9)
                    .background(RoundedRectangle(cornerRadius: 14)
                        .fill(m.role == "user" ? WatahaColors.FlagRed : Color.white))
                    .overlay(RoundedRectangle(cornerRadius: 14)
                        .stroke(m.role == "user" ? Color.clear : (m.crisis ? WatahaColors.FlagRed : WatahaColors.Border), lineWidth: 1))
                if m.role == "wilk" {
                    Button {
                        wilk.speak(m.text)
                    } label: {
                        HStack(spacing: 3) {
                            Image(systemName: "speaker.wave.2.fill").font(.system(size: 9))
                            Text("Posłuchaj").font(.system(size: 9.5, weight: .bold))
                        }
                        .foregroundColor(WatahaColors.FlagRed)
                        .padding(.horizontal, 8).padding(.vertical, 4)
                        .background(Capsule().fill(WatahaColors.LightRed))
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
            if m.role != "user" { Spacer(minLength: 50) }
        }
        .padding(.horizontal, 14)
        .id(m.id)
    }
}
