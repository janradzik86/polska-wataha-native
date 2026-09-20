package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.wataha.app.data.model.PostEntity
import pl.wataha.app.data.model.PostType
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.DistLabel
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.components.timeAgo
import pl.wataha.app.ui.theme.WatahaColors

@Composable
fun PostDetailScreen(
    repo: Repository,
    postId: String,
    onBack: () -> Unit,
    onOpenThread: (String) -> Unit,
    onNavigateMap: () -> Unit
) {
    var post by remember { mutableStateOf<PostEntity?>(null) }
    var showExchange by remember { mutableStateOf(false) }
    var showWrite by remember { mutableStateOf(false) }
    var msgText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val me by repo.me.collectAsState()

    androidx.compose.runtime.LaunchedEffect(postId) {
        post = repo.post(postId)
    }

    val p = post ?: return
    val type = runCatching { PostType.valueOf(p.type) }.getOrDefault(PostType.GIVE)
    val isMine = me?.id == p.authorId

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Ogłoszenie", p.authorName, onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("${type.emoji}  ${type.label}", fontSize = 13.sp, color = WatahaColors.DarkRed)
            Text(p.title, style = MaterialTheme.typography.headlineMedium, color = WatahaColors.Ink)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                Text("Autor: ${p.authorName}", fontSize = 13.sp, color = WatahaColors.Grey)
                DistLabel(repo.distanceKm(p.lat, p.lng))
            }
            Text(timeAgo(p.createdAt), fontSize = 12.sp, color = WatahaColors.Grey)
            if (p.pending) {
                Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.Amber.copy(0.15f)),
                    shape = RoundedCornerShape(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Text("⏳ Utworzono offline — czeka na synchronizację",
                        Modifier.padding(10.dp), fontSize = 12.sp, color = WatahaColors.Amber)
                }
            }
            Spacer(Modifier.height(14.dp))
            Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.LightGrey),
                shape = RoundedCornerShape(12.dp)) {
                Text(p.body, Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge, color = WatahaColors.Ink)
            }
            Spacer(Modifier.height(16.dp))

            if (!isMine) {
                when (type) {
                    PostType.GIVE -> {
                        Button(onClick = { showExchange = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                            modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            Text("🔁 ZAPROPONUJ WYMIANĘ")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { showWrite = true; msgText = "" },
                            modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            Text("💬 NAPISZ DO AUTORA")
                        }
                    }
                    PostType.NEED -> {
                        Button(onClick = { showWrite = true; msgText = "Mogę pomóc — proszę o kontakt." },
                            colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                            modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            Text("🤝 ODPOWIEM NA ZGŁOSZENIE POMOCY")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { onNavigateMap() }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            Text("🗺️ POKAŻ NA MAPIE")
                        }
                    }
                    PostType.OFFER -> {
                        Button(onClick = { showWrite = true; msgText = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                            modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            Text("💬 SKONTAKTUJ SIĘ Z POMAGAJĄCYM")
                        }
                    }
                }
            } else {
                OutlinedButton(onClick = { scope.launch { repo.closePost(p.id); onBack() } },
                    modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("Zamknij ogłoszenie")
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("🇵🇱 Wataha czuwa — zgłoś nadużycie przez tryb kryzysowy.",
                fontSize = 11.sp, color = WatahaColors.Grey, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }

    if (showExchange) {
        var msg by remember { mutableStateOf("Proponuję wymianę: mogę dostarczyć…") }
        AlertDialog(
            onDismissRequest = { showExchange = false },
            title = { Text("🔁 Propozycja wymiany") },
            text = {
                Column {
                    Text("„${p.title}” — napisz, co proponujesz w zamian.")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = msg, onValueChange = { msg = it }, modifier = Modifier.fillMaxWidth(),
                        minLines = 3)
                }
            },
            confirmButton = {
                Button(colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                    onClick = {
                        showExchange = false
                        scope.launch { repo.proposeExchange(p, msg) }
                    }) { Text("Wyślij propozycję") }
            },
            dismissButton = { OutlinedButton(onClick = { showExchange = false }) { Text("Anuluj") } }
        )
    }

    if (showWrite) {
        AlertDialog(
            onDismissRequest = { showWrite = false },
            title = { Text("💬 Wiadomość do: ${p.authorName}") },
            text = {
                OutlinedTextField(value = msgText, onValueChange = { msgText = it },
                    modifier = Modifier.fillMaxWidth(), minLines = 3)
            },
            confirmButton = {
                Button(colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                    onClick = {
                        showWrite = false
                        scope.launch {
                            val author = repo.getUser(p.authorId)
                            if (author != null && msgText.isNotBlank()) {
                                repo.sendMessage(author, msgText)
                                onOpenThread(p.authorId)
                            }
                        }
                    }) { Text("Wyślij") }
            },
            dismissButton = { OutlinedButton(onClick = { showWrite = false }) { Text("Anuluj") } }
        )
    }
}
