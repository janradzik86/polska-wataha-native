package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.wataha.app.data.model.ExchangeEntity
import pl.wataha.app.data.model.ExchangeStatus
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.StatusPill
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.components.timeAgo
import pl.wataha.app.ui.theme.WatahaColors

@Composable
fun ExchangesScreen(repo: Repository, onBack: () -> Unit) {
    val all by repo.exchanges.collectAsState(initial = emptyList())
    val me by repo.me.collectAsState()
    val scope = rememberCoroutineScope()

    val incoming = all.filter { it.ownerId == me?.id }
    val outgoing = all.filter { it.proposerId == me?.id }

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Wymiany", "Propozycje wymiany rzeczy i usług", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(14.dp)) {
            if (incoming.isEmpty() && outgoing.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔁", fontSize = 40.sp)
                        Text("Brak wymian", style = MaterialTheme.typography.titleMedium)
                        Text("Zaproponuj wymianę z ekranu ogłoszenia.", color = WatahaColors.Grey)
                    }
                }
            }
            if (incoming.isNotEmpty()) {
                item {
                    Text("📥 PROPOZYCJE DLA CIEBIE", style = MaterialTheme.typography.titleSmall, color = WatahaColors.Ink)
                    Spacer(Modifier.padding(top = 6.dp))
                }
                items(incoming, key = { "i_" + it.id }) { e ->
                    ExchangeCard(e, mine = false) { accept ->
                        scope.launch { repo.respondExchange(e.id, accept) }
                    }
                }
            }
            if (outgoing.isNotEmpty()) {
                item {
                    Spacer(Modifier.padding(top = 14.dp))
                    Text("📤 TWOJE PROPOZYCJE", style = MaterialTheme.typography.titleSmall, color = WatahaColors.Ink)
                    Spacer(Modifier.padding(top = 6.dp))
                }
                items(outgoing, key = { "o_" + it.id }) { e ->
                    ExchangeCard(e, mine = true) { _ ->
                        scope.launch { repo.simulateIncomingAccept(e.id) }
                    }
                }
            }
            item {
                Spacer(Modifier.padding(top = 16.dp))
                Text("🧪 DEMO: aby zobaczyć powiadomienia, użyj „Symuluj odpowiedź właściciela” (kanał Wymiany).",
                    fontSize = 11.sp, color = WatahaColors.Grey)
            }
        }
    }
}

@Composable
fun ExchangeCard(e: ExchangeEntity, mine: Boolean, onAction: (Boolean) -> Unit) {
    val statusColor = when (e.status) {
        ExchangeStatus.PENDING -> WatahaColors.Amber
        ExchangeStatus.ACCEPTED -> WatahaColors.Green
        ExchangeStatus.REJECTED -> WatahaColors.Grey
        else -> WatahaColors.Grey
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = WatahaColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (mine) "Do: ${e.proposerName}" else "Od: ${e.proposerName}",
                    style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f)
                )
                StatusPill(e.status, statusColor)
            }
            Text("„${e.postTitle}”", fontSize = 13.sp, color = WatahaColors.Ink)
            Spacer(Modifier.padding(top = 4.dp))
            Text(e.message, fontSize = 13.sp, color = WatahaColors.Grey)
            Text(timeAgo(e.createdAt), fontSize = 10.sp, color = WatahaColors.Grey)
            if (e.pending) {
                Text("⏳ utworzono offline", fontSize = 10.sp, color = WatahaColors.Amber)
            }
            if (!mine && e.status == ExchangeStatus.PENDING) {
                Spacer(Modifier.padding(top = 8.dp))
                Row {
                    Button(onClick = { onAction(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.Green),
                        modifier = Modifier.weight(1f)) { Text("PRZYJMUJĘ") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { onAction(false) }, modifier = Modifier.weight(1f)) { Text("Odrzucam") }
                }
            }
            if (mine && e.status == ExchangeStatus.PENDING) {
                Spacer(Modifier.padding(top = 8.dp))
                OutlinedButton(onClick = { onAction(true) }, modifier = Modifier.fillMaxWidth()) {
                    Text("🧪 Symuluj odpowiedź właściciela")
                }
            }
        }
    }
}
