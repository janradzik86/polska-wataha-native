package pl.wataha.app.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Głos asystenta WILK — synteza mowy (TTS) w 100% offline (głosy systemowe PL).
 * Kolejność: wbudowany syntezator Androida → „vocalizer” (systemowe głosy PL).
 */
object AssistantVoice {

    private var tts: TextToSpeech? = null
    private var ready = false

    /** Automatyczne odczytywanie odpowiedzi asystenta. */
    @Volatile
    var speakingEnabled = true

    fun ensure(context: Context) {
        if (tts != null) return
        ready = false
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = tts?.setLanguage(Locale("pl", "PL"))
                ready = res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    fun speak(text: String) {
        if (!speakingEnabled || !ready) return
        val clean = text
            .replace(Regex("[🩹💧🔥🏠🎒📡🌊⛈️🧠🔌🔁📦🚨🇵🇱🤝⛪🚒🏛📼]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        try {
            tts?.stop()
            tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "wataha-voice")
        } catch (_: Exception) {
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready = false
    }
}
