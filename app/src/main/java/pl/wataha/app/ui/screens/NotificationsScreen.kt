package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.wataha.app.service.NotificationHelper
import pl.wataha.app.ui.components.SectionCard
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.theme.WatahaColors

/** Test kanałów powiadomień — każdy przycisk wysyła testowe powiadomienie do swojego kanału. */
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Powiadomienia", "Kanały: Wiadomości • Wymiany • Pomoc • Lokalne • Kryzysowe", onBack = onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Wyślij testowe powiadomienie i sprawdź je w szufladzie (notifications) telefonu. Android 13+: pierwsze powiadomienie wywoła prośbę o uprawnienie.",
                fontSize = 12.sp, color = WatahaColors.Grey)
            Spacer(Modifier.height(12.dp))

            TestRow("💬", "Wiadomości", "Zwykły wątek rozmowy", WatahaColors.Blue) {
                NotificationHelper.notify(NotificationHelper.CHAT, 11, "Nowa wiadomość — Sokół",
                    "Widzę, że masz apteczkę — mogę ją zawieźć na Bemowo.")
            }
            TestRow("🔁", "Wymiany", "Status propozycji wymiany", WatahaColors.Green) {
                NotificationHelper.notify(NotificationHelper.EXCHANGE, 12, "Wymiana przyjęta ✅",
                    "„Powerbank” — umów odbiór w wiadomościach.")
            }
            TestRow("🤝", "Pomoc", "Zgłoszenie pomocy", WatahaColors.Amber) {
                NotificationHelper.notify(NotificationHelper.HELP, 13, "Nowe zgłoszenie pomocy",
                    "„Potrzebuję leków na cukrzycę” — 3,2 km od Ciebie.")
            }
            TestRow("📍", "Lokalne", "Informacja z watahy", WatahaColors.Ink) {
                NotificationHelper.notify(NotificationHelper.LOCAL, 14, "Nowa odznaka: ⚓ Kotwica",
                    "Za pierwszą wysłaną wiadomość. Gratulacje!")
            }
            TestRow("🚨", "Kryzysowe", "NAJWYŻSZY priorytet + alarm", WatahaColors.FlagRed) {
                NotificationHelper.notify(NotificationHelper.CRISIS, 15, "🚨 ALERT KRYZYSOWY",
                    "SYGNAŁ SOS: „Brak wody — okolica Woli”",
                    bigText = "Sygnał kryzysowy na Twoim obszarze. Sprawdź mapę i status sieci.",
                    fullScreen = true)
            }
            Spacer(Modifier.height(10.dp))
            Text("Kanały są tworzone przy starcie aplikacji. Priorytety: Kryzysowe = MAX (alarm + pełny ekran, o ile system pozwoli), pozostałe = HIGH.",
                fontSize = 10.sp, color = WatahaColors.Grey)
        }
    }
}

@Composable
private fun TestRow(emoji: String, name: String, desc: String, color: androidx.compose.ui.graphics.Color, onSend: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = WatahaColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("$emoji  $name", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WatahaColors.Ink)
            Text(desc, fontSize = 11.sp, color = WatahaColors.Grey)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onSend, colors = ButtonDefaults.buttonColors(containerColor = color),
                modifier = Modifier.fillMaxWidth()) {
                Text("Wyślij testowe powiadomienie")
            }
        }
    }
}
