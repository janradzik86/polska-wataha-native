package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.Avatar
import pl.wataha.app.ui.components.SectionCard
import pl.wataha.app.ui.components.StatusPill
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.theme.WatahaColors

@Composable
fun ProfileScreen(repo: Repository, onBack: () -> Unit, onLoggedOut: () -> Unit, appVersion: String) {
    val me by repo.me.collectAsState()
    val badges by (me?.let { repo.badges(it.id) }?.collectAsState(initial = emptyList()))
        ?: remember { androidx.compose.runtime.mutableStateOf(emptyList<pl.wataha.app.data.model.BadgeEntity>()) }
    val online by repo.online.collectAsState()
    val forceOffline by repo.forceOffline.collectAsState()
    val pending by repo.pendingCount.collectAsState(initial = 0)
    val scope = rememberCoroutineScope()

    var showApiDialog by remember { mutableStateOf(false) }
    var apiUrl by remember { mutableStateOf(repo.apiBase) }
    var testResult by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Profil", me?.displayName ?: "", onBack = onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(me?.displayName ?: "?", size = 56.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(me?.displayName ?: "—", style = MaterialTheme.typography.titleLarge)
                    Text("@${me?.username ?: "—"}  •  ${me?.phone ?: ""}", fontSize = 12.sp, color = WatahaColors.Grey)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusPill("⭐ ${me?.reputation ?: 0.0}", WatahaColors.Amber)
                        StatusPill(if (online) "ONLINE" else "OFFLINE", if (online) WatahaColors.Green else WatahaColors.Grey)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(me?.bio ?: "", fontSize = 13.sp, color = WatahaColors.Ink)
            Spacer(Modifier.height(6.dp))
            Text("🇵🇱 Polska Wataha • Czarny Wilk — Strażnik Prawdy", fontSize = 11.sp, color = WatahaColors.Grey)

            Spacer(Modifier.height(14.dp))
            SectionCard("🎖️ Twoje odznaki (${badges.size})") {
                if (badges.isEmpty()) {
                    Text("Brak odznak — zdobywaj je aktywnością!", fontSize = 12.sp, color = WatahaColors.Grey)
                }
                badges.forEach { b ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(b.emoji, fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(b.name, style = MaterialTheme.typography.titleSmall)
                            Text(b.desc, fontSize = 11.sp, color = WatahaColors.Grey)
                        }
                    }
                }
                TextButton(onClick = { /* reputacja osobno */ }) {
                    Text("Szczegóły w Reputacji →")
                }
            }

            Spacer(Modifier.height(14.dp))
            SectionCard("⚙️ Ustawienia") {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("🧪 Tryb DEMO OFFLINE", style = MaterialTheme.typography.titleSmall)
                        Text("Wymusza tryb offline, aby przetestować kolejkę synchronizacji.",
                            fontSize = 11.sp, color = WatahaColors.Grey)
                    }
                    Switch(checked = forceOffline, onCheckedChange = { repo.setForceOffline(it) })
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { apiUrl = repo.apiBase; showApiDialog = true },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("🌐 Backend: ${repo.apiBase}")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val n = repo.syncNow()
                            testResult = if (n > 0) "Zsyncronizowano $n rekordów. ✅" else "Kolejka pusta lub brak sieci."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("🔄 SYNCHRONIZUJ TERAZ (kolejka: $pending)") }
                if (testResult != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(testResult ?: "", fontSize = 12.sp, color = WatahaColors.Ink)
                }
                Spacer(Modifier.height(8.dp))
                Text("Wersja aplikacji: $appVersion • Baza: Room/SQLite (offline-first)\nKanały powiadomień: Wiadomości • Wymiany • Pomoc • Lokalne • Kryzysowe",
                    fontSize = 10.sp, color = WatahaColors.Grey)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = {
                    repo.logout()
                    onLoggedOut()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Wyloguj się", color = WatahaColors.FlagRed)
                }
            }
        }
    }

    if (showApiDialog) {
        AlertDialog(
            onDismissRequest = { showApiDialog = false },
            title = { Text("🌐 Adres backendu") },
            text = {
                Column {
                    Text("Ustaw adres REST API (na telefonie wpisz adres komputera z backendem, np. http://192.168.1.10:8080).", fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = apiUrl, onValueChange = { apiUrl = it },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text("URL API") })
                    Text("Presety: 10.0.2.2:8080 = emulator • bez zmian = brak backendu (tryb lokalny)",
                        fontSize = 11.sp, color = WatahaColors.Grey)
                }
            },
            confirmButton = {
                Button(colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                    onClick = { repo.setApiBase(apiUrl); showApiDialog = false }) { Text("Zapisz") }
            },
            dismissButton = { TextButton(onClick = { showApiDialog = false }) { Text("Anuluj") } }
        )
    }
}
