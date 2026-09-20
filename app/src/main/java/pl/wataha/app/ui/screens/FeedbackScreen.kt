package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.wataha.app.data.model.FeedbackEntity
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.GlassCard
import pl.wataha.app.ui.components.GradientButton
import pl.wataha.app.ui.components.NeoChip
import pl.wataha.app.ui.components.StatusPill
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.components.timeAgo
import pl.wataha.app.ui.theme.WatahaColors

/**
 * „Masz pomysł? To napisz!” — zgłoszenia trafiają do Administracji Watahy
 * (lokalnie → kolejka → backend `/api/feedback` + panel web).
 */
@Composable
fun FeedbackScreen(repo: Repository, onBack: () -> Unit) {
    val me by repo.me.collectAsState()
    val mine by (me?.let { repo.myFeedback(it.id) }?.collectAsState(initial = emptyList()))
        ?: remember { androidx.compose.runtime.mutableStateOf(emptyList<FeedbackEntity>()) }
    val scope = rememberCoroutineScope()

    val subjects = listOf(
        "💡 Pomysł na funkcję",
        "🐞 Błąd / usterka",
        "🛡️ Zgłoszenie nadużycia",
        "🤝 Współpraca / wolontariat",
        "📮 Inne"
    )
    var subject by remember { mutableStateOf(subjects.first()) }
    var text by remember { mutableStateOf("") }
    var sentInfo by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Masz pomysł? To napisz!", "Wiadomości przekazywane do Administracji", onBack = onBack)
        Column(
            Modifier
                .weight(0.45f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            GlassCard {
                Column(Modifier.padding(14.dp)) {
                    Text("💡 Wataha rośnie dzięki Tobie", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        "Każda wiadomość trafia prosto do Administracji — czytamy wszystko, a najlepsze pomysły wchodzą do kolejnych wersji (V0.3+). Pisz śmiało: funkcja, błąd, nadużycie, współpraca.",
                        fontSize = 12.sp, color = WatahaColors.Grey, lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth().height(46.dp).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { s ->
                            NeoChip(
                                text = s.substringAfter(" "),
                                emoji = s.take(2),
                                selected = s == subject,
                                modifier = Modifier.width(150.dp)
                            ) { subject = s }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("Opisz swój pomysł / problem… (min. 10 znaków)") },
                        minLines = 4,
                        maxLines = 7,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (sentInfo != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(sentInfo ?: "", fontSize = 12.sp, color = WatahaColors.Green, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                    GradientButton(
                        text = "WYŚLIJ DO ADMINISTRACJI",
                        icon = "📨",
                        height = 52.dp,
                        fontSize = 14,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        scope.launch {
                            if (text.trim().length >= 10) {
                                repo.submitFeedback(subject, text.trim())
                                text = ""
                                sentInfo = "✅ Wysłano! Dziękujemy — administracja odpowie przez wiadomości."
                            } else {
                                sentInfo = "Napisz coś konkretnego (min. 10 znaków)."
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Column(Modifier.weight(0.55f)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("MOJE ZGŁOSZENIA", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text("${mine.size}", color = WatahaColors.Grey, fontSize = 12.sp)
            }
            if (mine.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 30.sp)
                    Text("Brak zgłoszeń — bądź pierwszy!", fontSize = 12.sp, color = WatahaColors.Grey)
                }
            }
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
                items(mine, key = { it.id }) { f ->
                    FeedbackRow(f)
                }
            }
        }
    }
}

@Composable
private fun FeedbackRow(f: FeedbackEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = WatahaColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(f.subject, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                StatusPill(if (f.pending) "⏳ w kolejce" else "✅ wysłane",
                    if (f.pending) WatahaColors.Amber else WatahaColors.Green)
            }
            Text(f.text, fontSize = 12.sp, color = WatahaColors.Grey, lineHeight = 16.sp, maxLines = 3)
            Text(timeAgo(f.createdAt), fontSize = 10.sp, color = WatahaColors.Grey)
        }
    }
}
