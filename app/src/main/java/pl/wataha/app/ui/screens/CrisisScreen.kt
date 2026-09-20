package pl.wataha.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.wataha.app.data.model.Types
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.KotwicaLogo
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.components.timeAgo
import pl.wataha.app.ui.theme.WatahaColors

/**
 * TRYB KRYZYSOWY — uproszczony interfejs: duże przyciski, zero zbędnych animacji,
 * czytelny nawet przy słabym świetle i niskiej baterii.
 */
@Composable
fun CrisisScreen(
    repo: Repository,
    onBack: () -> Unit,
    onMap: () -> Unit,
    onNetwork: () -> Unit
) {
    val me by repo.me.collectAsState()
    val signals by repo.crisis.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var dialog by remember { mutableStateOf<String?>(null) } // SOS / BROADCAST / LOCATION
    var dialogText by remember { mutableStateOf("") }

    val loc = repo.myLocation()

    @Composable
    fun CrisisButton(emoji: String, label: String, sub: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 26.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WatahaColors.DarkRed)
                    Text(sub, fontSize = 11.sp, color = WatahaColors.Grey)
                }
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(WatahaColors.FlagRed)
    ) {
        WatahaTopBar("TRYB KRYZYSOWY", "Aktywnie: ${me?.displayName ?: "—"}", onBack = onBack, crisisMode = true)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KotwicaLogo(size = 40.dp, color = Color.White)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("WATAHA W STANIE GOTOWOŚCI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Tryb oszczędny: mniej animacji, kluczowe działania", color = Color.White.copy(0.85f), fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(12.dp))

            CrisisButton("🆘", "POTRZEBUJĘ POMOCY", "Wyślij SOS z Twoją lokalizacją", onClick = {
                dialog = "SOS"; dialogText = ""
            })
            CrisisButton("🤝", "MOGĘ POMÓC", "Opublikuj ofertę pomocy w watahe", onClick = {
                dialog = "OFFER"; dialogText = ""
            })
            CrisisButton("📢", "KOMUNIKAT", "Nadaj komunikat do całej sieci", onClick = {
                dialog = "BROADCAST"; dialogText = ""
            })
            CrisisButton("📍", "MOJA LOKALIZACJA", "Pokaż i udostępnij współrzędne", onClick = {
                dialog = "LOCATION"; dialogText = ""
            })
            CrisisButton("🗺️", "MAPA", "Ogłoszenia, punkty pomocy, węzły", onClick = onMap)
            CrisisButton("📡", "STATUS SIECI", "Adaptery: Internet / BLE / Wi-Fi Direct / LoRa", onClick = onNetwork)

            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.12f)),
                shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("NAJNOWSZE SYGNAŁY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    signals.take(4).forEach { s ->
                        Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Text(if (s.type == "KOMUNIKAT") "📢" else "🚨", fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text("${s.userName}: ${s.text.take(70)}", color = Color.White, fontSize = 11.sp)
                                Text(timeAgo(s.createdAt), color = Color.White.copy(0.7f), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("ZAKOŃCZ TRYB KRYZYSOWY", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (dialog != null) {
        val d = dialog!!
        AlertDialog(
            onDismissRequest = { dialog = null },
            containerColor = Color.White,
            title = { Text(
                when (d) {
                    "SOS" -> "🆘 POTRZEBUJĘ POMOCY"
                    "OFFER" -> "🤝 MOGĘ POMÓC"
                    "BROADCAST" -> "📢 KOMUNIKAT"
                    else -> "📍 MOJA LOKALIZACJA"
                }
            ) },
            text = {
                Column {
                    if (d == "LOCATION") {
                        Text("Twoja pozycja:")
                        Text("%.5f, %.5f".format(loc.first, loc.second), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                try {
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "🚨 POLSKA WATAHA — MOJA LOKALIZACJA\n%.5f, %.5f".format(loc.first, loc.second))
                                    }
                                    context.startActivity(Intent.createChooser(send, "Udostępnij lokalizację"))
                                } catch (_: Exception) {
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Udostępnij SMS / aplikacją") }
                    } else {
                        OutlinedTextField(
                            value = dialogText,
                            onValueChange = { dialogText = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            placeholder = {
                                Text(if (d == "SOS") "Co się dzieje? np. „Brak wody, potrzebuję leków…”" else "Treść komunikatu…")
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                    onClick = {
                        val what = d
                        dialog = null
                        scope.launch {
                            when (what) {
                                "SOS" -> repo.reportCrisis(Types.CRISIS_SOS, dialogText.ifBlank { "SOS: potrzebuję pomocy — proszę o kontakt." })
                                "BROADCAST" -> repo.reportCrisis(Types.CRISIS_BROADCAST, dialogText.ifBlank { "KOMUNIKAT WATAHY" })
                                "OFFER" -> repo.createPost(
                                    pl.wataha.app.data.model.PostType.OFFER,
                                    "Pomoc kryzysowa",
                                    dialogText.ifBlank { "Mogę pomóc — proszę o kontakt." },
                                    "", loc.first, loc.second
                                )
                                else -> repo.reportCrisis(Types.CRISIS_LOCATION, "Moja lokalizacja: %.5f, %.5f".format(loc.first, loc.second))
                            }
                        }
                    }
                ) { Text(if (d == "OFFER") "Opublikuj ofertę" else "WYŚLIJ") }
            },
            dismissButton = { OutlinedButton(onClick = { dialog = null }) { Text("Anuluj") } }
        )
    }
}
