package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import pl.wataha.app.data.model.MessageEntity
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.components.timeAgo
import pl.wataha.app.ui.theme.WatahaColors

@Composable
fun ChatThreadScreen(
    repo: Repository,
    threadId: String? = null,
    recipientId: String? = null,
    onBack: () -> Unit
) {
    val me by repo.me.collectAsState()
    // efektywny identyfikator wątku: istniejący lub nowy (rozmowa z wybranym użytkownikiem)
    val effectiveThreadId = remember(threadId, recipientId, me?.id) {
        if (threadId != null) threadId
        else "t_" + listOf(me?.id ?: "?", recipientId ?: "?").sorted().joinToString("_")
    }
    val messages by repo.messages(effectiveThreadId).collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var counterpart by remember { mutableStateOf<pl.wataha.app.data.model.UserEntity?>(null) }

    LaunchedEffect(effectiveThreadId) {
        repo.thread(effectiveThreadId)?.let { t ->
            repo.markThreadRead(effectiveThreadId)
            counterpart = repo.getUser(t.userId)
        }
        if (counterpart == null && recipientId != null) {
            counterpart = repo.getUser(recipientId)
        }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar(
            counterpart?.displayName ?: "Wątek",
            "Wiadomości działają offline (kolejka)",
            onBack = onBack
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages, key = { it.id }) { m ->
                MessageBubble(m, isMine = m.fromId == me?.id)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.LightGrey), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text, onValueChange = { text = it },
                    placeholder = { Text("Napisz wiadomość…") },
                    modifier = Modifier.weight(1f), maxLines = 3
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val to = counterpart ?: return@IconButton
                        if (text.isNotBlank()) {
                            scope.launch {
                                repo.sendMessage(to, text.trim())
                                text = ""
                            }
                        }
                    },
                    modifier = Modifier.background(WatahaColors.FlagRed, RoundedCornerShape(50)).padding(2.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Wyślij", tint = WatahaColors.White)
                }
            }
            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🧪 DEMO: ", fontSize = 11.sp, color = WatahaColors.Grey)
                OutlinedButton(onClick = {
                    scope.launch { repo.simulateReply(effectiveThreadId) }
                }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)) {
                    Text("Symuluj odpowiedź rozmówcy", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(m: MessageEntity, isMine: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start) {
        Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isMine) WatahaColors.FlagRed else WatahaColors.LightGrey
                ),
                shape = RoundedCornerShape(if (isMine) 14.dp else 14.dp)
            ) {
                Text(
                    m.text,
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (isMine) WatahaColors.White else WatahaColors.Ink,
                    fontSize = 14.sp
                )
            }
            Text(
                (if (m.pending) "⏳ oczekuje na wysyłkę • " else "") + timeAgo(m.createdAt),
                fontSize = 10.sp, color = if (m.pending) WatahaColors.Amber else WatahaColors.Grey,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
