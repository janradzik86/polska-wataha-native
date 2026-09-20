package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.wataha.app.data.model.ThreadEntity
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.Avatar
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.components.timeAgo
import pl.wataha.app.ui.theme.WatahaColors

@Composable
fun ChatScreen(repo: Repository, onBack: () -> Unit, onOpenThread: (String) -> Unit) {
    val threads by repo.threads.collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Wiadomości", "Komunikator lokalny — również offline", onBack = onBack)
        if (threads.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(top = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💬", fontSize = 40.sp)
                Text("Brak wątków", style = MaterialTheme.typography.titleMedium)
                Text("Otwórz ogłoszenie i napisz do autora.", color = WatahaColors.Grey)
            }
        }
        LazyColumn(contentPadding = PaddingValues(12.dp)) {
            items(threads, key = { it.threadId }) { t ->
                ThreadRow(t) { onOpenThread(t.threadId) }
            }
        }
    }
}

@Composable
fun ThreadRow(t: ThreadEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = WatahaColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(t.userName)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.userName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Text(timeAgo(t.lastAt), fontSize = 10.sp, color = WatahaColors.Grey)
                }
                Text(t.lastText, maxLines = 1, fontSize = 13.sp, color = WatahaColors.Grey)
            }
            if (t.unread > 0) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.size(22.dp).background(WatahaColors.FlagRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${t.unread}", color = WatahaColors.White, fontSize = 11.sp)
                }
            }
        }
    }
}
