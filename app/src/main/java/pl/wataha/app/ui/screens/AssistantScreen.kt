package pl.wataha.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.wataha.app.R
import pl.wataha.app.ai.AssistantEngine
import pl.wataha.app.ai.AssistantVoice
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.GlassCard
import pl.wataha.app.ui.components.GradientButton
import pl.wataha.app.ui.components.NeoChip
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.theme.WatahaColors

private data class ChatMsg(
    val text: String,
    val fromUser: Boolean,
    val crisis: Boolean = false
)

/**
 * ASYSTENT WILK — lokalny, w 100% offline:
 * → mikrofon (rozpoznawanie mowy urządzenia, głos PL) → silnik wiedzy → TTS (odpowiada głosem).
 * Deterministyczna baza wiedzy survivalowej; bez „samonaprawiającego się” modelu — działa zawsze.
 */
@Composable
fun AssistantScreen(repo: Repository, onBack: () -> Unit, onCrisis: () -> Unit) {
    val context = LocalContext.current
    val me by repo.me.collectAsState()
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMsg(
                    "Cześć, ${me?.displayName ?: "Wilku"}! 🐺 Jestem WILK — lokalny asystent watahy, specjalista od sytuacji kryzysowych i przetrwania. Działam w 100% offline: mów do mnie (🎤) albo pisz. Zapytaj np. „jak oczyścić wodę?” albo „co spakować na 72 godziny?”",
                    fromUser = false
                )
            )
        )
    }
    var input by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var micError by remember { mutableStateOf<String?>(null) }
    var voiceOut by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val recognizer = remember {
        runCatching { SpeechRecognizer.createSpeechRecognizer(context) }.getOrNull()
    }

    fun answerAndSpeak(text: String) {
        val reply = AssistantEngine.answer(text)
        messages = messages + ChatMsg(text, fromUser = true) + ChatMsg(reply.text, fromUser = false, crisis = reply.crisis)
        if (voiceOut && !reply.crisis) AssistantVoice.speak(reply.text)
        if (reply.crisis) AssistantVoice.speak("Wykryto sytuację kryzysową. Otwórz tryb kryzysowy.")
    }

    fun listen() {
        micError = null
        val rec = recognizer
        if (rec == null) {
            micError = "Brak modułu rozpoznawania mowy w tym urządzeniu — napisz tekst."
            return
        }
        try {
            rec.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { listening = true }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { listening = false }
                override fun onError(error: Int) {
                    listening = false
                    micError = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Nie zrozumiałem — spróbuj jeszcze raz."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Cisza… powiedz coś (albo napisz)."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Rozpoznawanie zajęte — chwilka…"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Brak uprawnienia mikrofonu."
                        else -> "Rozpoznawanie niedostępne (brak pakietu głosu PL?) — wpisz tekst."
                    }
                }

                override fun onResults(results: Bundle?) {
                    listening = false
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!text.isNullOrBlank()) answerAndSpeak(text)
                    else micError = "Nie zrozumiałem — spróbuj jeszcze raz."
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pl-PL")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            rec.startListening(intent)
        } catch (e: Exception) {
            listening = false
            micError = "Rozpoznawanie mowy niedostępne — wpisz tekst."
        }
    }

    val micPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) listen() else micError = "Bez mikrofonu nie usłyszę — pisz tekst."
    }

    DisposableEffect(Unit) {
        AssistantVoice.ensure(context)
        onDispose {
            AssistantVoice.stop()
            runCatching { recognizer?.destroy() }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Asystent WILK", "Lokalny specjalista kryzysowy • działa offline", onBack = onBack)

        // hero: czarny wilk (fotorealistyczny)
        Box(Modifier.fillMaxWidth().height(150.dp)) {
            Image(
                painter = painterResource(R.drawable.wolf_ai),
                contentDescription = "Asystent WILK — czarny wilk",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, WatahaColors.White.copy(alpha = 0.95f))
                    )
                )
            )
            Row(
                Modifier.align(Alignment.BottomStart).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🧠", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("WILK — Watahowy Inteligentny Lokalny Kompas", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WatahaColors.Ink)
                    Text("Odpowiada głosem i tekstem • 100% offline • bez modelu w chmurze",
                        fontSize = 10.sp, color = WatahaColors.Grey)
                }
                Spacer(Modifier.weight(1f))
                GradientButton(
                    text = if (voiceOut) "GŁOS: ON" else "GŁOS: OFF",
                    icon = if (voiceOut) "🔊" else "🔇",
                    colors = if (voiceOut) listOf(WatahaColors.Ink, Color(0xFF0F172A))
                    else listOf(WatahaColors.Grey, WatahaColors.Grey),
                    height = 36.dp, fontSize = 11
                ) { voiceOut = !voiceOut; AssistantVoice.speakingEnabled = voiceOut }
            }
        }

        // czat
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages, key = { it.hashCode() }) { m ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.fromUser) Arrangement.End else Arrangement.Start) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (m.fromUser) WatahaColors.Ink else WatahaColors.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (m.fromUser) 2.dp else 4.dp),
                        shape = RoundedCornerShape(if (m.fromUser) 16.dp else 16.dp)
                    ) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                            Text(
                                m.text,
                                color = if (m.fromUser) Color.White else WatahaColors.Ink,
                                fontSize = 13.sp, lineHeight = 18.sp
                            )
                            if (!m.fromUser) {
                                Text("🐺 WILK • offline", fontSize = 9.sp, color = WatahaColors.Grey,
                                    modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                    if (m.fromUser) {
                        Spacer(Modifier.width(6.dp))
                        Text("🧑", fontSize = 16.sp)
                    }
                }
            }
            if (micError != null) {
                item {
                    Text(micError ?: "", fontSize = 11.sp, color = WatahaColors.FlagRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            if (messages.lastOrNull()?.crisis == true) {
                item {
                    GradientButton(
                        "AKTYWUJ TRYB KRYZYSOWY",
                        icon = "🚨",
                        colors = listOf(WatahaColors.FlagRed, Color(0xFF7A0018)),
                        height = 50.dp,
                        fontSize = 14,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) { onCrisis() }
                }
            }
        }

        // szybkie pytania
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Jak oczyścić wodę?" to "💧",
                "Co spakować na 72h?" to "🎒",
                "Jak rozpalić ogień?" to "🔥",
                "Pierwsza pomoc 🩹" to "🩹",
                "Co robić przy powodzi?" to "🌊",
                "Brak prądu" to "🔌",
                "Jak wysłać SOS?" to "📡",
                "Jak zaproponować wymianę?" to "🔁"
            ).forEach { (q, e) ->
                NeoChip(text = q, emoji = e, selected = false) { answerAndSpeak(q) }
            }
        }

        // pole wpisywania + mikrofon
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Napisz pytanie do WILKA…") },
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
            Spacer(Modifier.width(8.dp))
            GradientButton(
                text = "⬆",
                colors = listOf(WatahaColors.FlagRed, WatahaColors.DarkRed),
                height = 52.dp,
                fontSize = 14,
                modifier = Modifier.width(56.dp)
            ) {
                if (input.isNotBlank()) {
                    answerAndSpeak(input.trim())
                    input = ""
                }
            }
            Spacer(Modifier.width(8.dp))
            GradientButton(
                text = if (listening) "NASŁUCH…" else "",
                icon = "🎤",
                colors = if (listening) listOf(WatahaColors.FlagRed, Color(0xFF7A0018))
                else listOf(WatahaColors.Ink, Color(0xFF0F172A)),
                height = 52.dp,
                fontSize = 13,
                modifier = Modifier.width(72.dp)
            ) {
                if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    if (listening) {
                        runCatching { recognizer?.stopListening() }
                    } else {
                        listen()
                    }
                } else {
                    micPerm.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
        Text("Mikrofon używa systemowego rozpoznawania mowy (pl-PL). Bez sieci działa po pobraniu pakietu głosu w ustawieniach urządzenia.",
            fontSize = 9.sp, color = WatahaColors.Grey, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        Spacer(Modifier.height(6.dp))
    }
}
