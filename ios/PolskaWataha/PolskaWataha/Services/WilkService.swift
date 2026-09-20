import Foundation
import AVFoundation
import Speech
import Combine
import PolskaWatahaCore

/// ASYSTENT WILK — w pełni offline, deterministyczny (bez modelu samouczącego się).
/// Głos: rozpoznawanie mowy (SFSpeechRecognizer) + synteza (AVSpeechSynthesizer).
/// Rdzeń = AssistantEngine z pakietu PolskaWatahaCore — przetestowany na Linuxie.
@MainActor
final class WilkService: NSObject, ObservableObject {
    @Published private(set) var history: [WilkMessage] = []
    @Published private(set) var listening = false
    @Published private(set) var speaking = false
    @Published var recognitionAvailable = true
    @Published var speechDenied = false

    struct WilkMessage: Codable, Identifiable, Hashable {
        let id: UUID
        let role: String      // "user" | "wilk"
        let text: String
        let crisis: Bool
        let topic: String?
        let at: Int64
    }

    private let engine = AssistantEngine.self
    private let synth = AVSpeechSynthesizer()
    private var recognizer: SFSpeechRecognizer?
    private var audioEngine = AVAudioEngine()
    private var request: SFSpeechAudioBufferRecognitionRequest?
    private var task: SFSpeechRecognitionTask?

    static let QUICK = [
        "Co powinienem spakować na 72 godziny?",
        "Jak złożyć ognisko?",
        "Jak uzdatnić wodę?",
        "Nie oddycha — co robić?",
        "Jak się schować przed burzą?",
        "Gdzie szukać pomocy w Warszawie?"
    ]

    override init() {
        super.init()
        recognizer = SFSpeechRecognizer(locale: Locale(identifier: "pl-PL"))
        recognitionAvailable = recognizer?.isAvailable ?? false
        loadHistory()
    }

    // ---------- historia ----------
    private static let histKey = "wilk_history"
    private func loadHistory() {
        guard let data = UserDefaults.standard.data(forKey: WilkService.histKey),
              let decoded = try? JSONDecoder().decode([WilkMessage].self, from: data) else {
            history = [WilkMessage(id: UUID(), role: "wilk",
                                   text: "Jestem WILK — offline'owy asystent Watahy. 🐺\nPytaj mnie o przetrwanie, pierwszą pomoc, schronienie, wodę, ogień, pogodę albo o to, co wziąć na 72 godziny. Mów śmiało — nie potrzebuję internetu.",
                                   crisis: false, topic: nil, at: nowMs())]
            return
        }
        history = decoded
    }
    private func persist() {
        if let data = try? JSONEncoder().encode(history) {
            UserDefaults.standard.set(data, forKey: WilkService.histKey)
        }
    }
    func clearHistory() {
        history = []
        persist()
    }

    // ---------- odpowiedź ----------
    @discardableResult
    func ask(_ text: String) -> AssistantEngine.Reply {
        let reply = engine.answer(text)
        history.append(WilkMessage(id: UUID(), role: "user", text: text, crisis: false, topic: nil, at: nowMs()))
        history.append(WilkMessage(id: UUID(), role: "wilk", text: reply.text, crisis: reply.crisis, topic: reply.topic, at: nowMs()))
        if history.count > 200 { history.removeFirst(history.count - 200) }
        persist()
        if reply.crisis {
            NotificationHelper.show(channel: .crisis, id: 9001, title: "🚨 WILK wykrył sytuację zagrażającą życiu",
                                    body: "Odpowiedź asystenta: \(String(reply.text.prefix(120)))…", fullScreen: true)
        }
        speak(reply.text)
        return reply
    }

    // ---------- mowa w / wyjście ----------
    func speak(_ text: String) {
        synth.stopSpeaking(at: .immediate)
        let u = AVSpeechUtterance(string: text)
        u.voice = AVSpeechSynthesisVoice(language: "pl-PL")
        u.rate = 0.5
        synth.speak(u)
        speaking = true
        // przybliżony czas czytania (~14 znaków/s); flaga gaśnie, gdy synteza kończy
        let seconds = Double(text.count) / 14.0
        DispatchQueue.main.asyncAfter(deadline: .now() + max(seconds, 1.5)) { [weak self] in
            self?.speaking = false
        }
    }
    func stopSpeaking() {
        synth.stopSpeaking(at: .immediate)
        speaking = false
    }

    func toggleListening(completion: @escaping (String?) -> Void) {
        if listening {
            stopListening()
            completion(nil)
            return
        }
        guard SFSpeechRecognizer.authorizationStatus() != .denied else {
            speechDenied = true
            completion(nil)
            return
        }
        SFSpeechRecognizer.requestAuthorization { [weak self] status in
            DispatchQueue.main.async {
                guard let self else { return }
                if status != .authorized {
                    self.speechDenied = true
                    completion(nil)
                    return
                }
                self.startListening(completion: completion)
            }
        }
    }

    private func startListening(completion: @escaping (String?) -> Void) {
        guard let recognizer else { speechDenied = true; completion(nil); return }
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.record, mode: .measurement, options: .duckOthers)
            try session.setActive(true)
        } catch {
            completion(nil); return
        }

        let node = audioEngine.inputNode
        let fmt = node.outputFormat(forBus: 0)
        let rq = SFSpeechAudioBufferRecognitionRequest()
        rq.shouldReportPartialResults = true
        request = rq
        listening = true

        task = recognizer.recognitionTask(with: rq) { [weak self] result, error in
            DispatchQueue.main.async {
                guard let self else { return }
                if let result {
                    let text = result.bestTranscription.formattedString
                    if !text.isEmpty { completion(text) }
                    if result.isFinal {
                        self.stopListening()
                        completion(text)
                    }
                } else if error != nil {
                    self.stopListening()
                    completion(nil)
                }
            }
        }

        node.installTap(onBus: 0, bufferSize: 1024, format: fmt) { [weak self] buffer, _ in
            self?.request?.append(buffer)
        }
        audioEngine.prepare()
        try? audioEngine.start()
    }

    private func stopListening() {
        audioEngine.stop()
        audioEngine.inputNode.removeTap(onBus: 0)
        task?.cancel()
        task = nil
        request = nil
        listening = false
        try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    }
}


