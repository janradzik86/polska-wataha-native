package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.wataha.app.data.model.PostType
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.DistLabel
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.components.timeAgo
import pl.wataha.app.ui.theme.WatahaColors

@Composable
fun HelpScreen(
    repo: Repository,
    onBack: () -> Unit,
    onNeed: () -> Unit,
    onOffer: () -> Unit,
    onOpen: (String) -> Unit
) {
    val posts by repo.posts.collectAsState(initial = emptyList())
    val signals by repo.crisis.collectAsState(initial = emptyList())

    val needs = posts.filter { it.type == PostType.NEED.name }
    val offers = posts.filter { it.type == PostType.OFFER.name }
    val broadcasts = signals.filter { it.type == "KOMUNIKAT" }.take(3)

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Pomoc", "Zgłoś potrzebę lub zaoferuj wsparcie", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(14.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onNeed,
                        colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                        modifier = Modifier.weight(1f).height(52.dp)) {
                        Text("🆘 POTRZEBUJĘ\nPOMOCY", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 12.sp)
                    }
                    Button(onClick = onOffer,
                        colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.Ink),
                        modifier = Modifier.weight(1f).height(52.dp)) {
                        Text("🤝 MOGĘ\nPOMÓC", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 12.sp)
                    }
                }
                if (broadcasts.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text("📢 KOMUNIKATY KRYZYSOWE", style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                    broadcasts.forEach { s ->
                        Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.FlagRed.copy(0.08f)),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(s.text, fontSize = 13.sp, color = WatahaColors.Ink)
                                Text("— ${s.userName}, ${timeAgo(s.createdAt)}", fontSize = 11.sp, color = WatahaColors.Grey)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("🆘 AKTYWNE ZGŁOSZENIA POMOCY (${needs.size})",
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
            }
            if (needs.isEmpty()) {
                item { Text("Brak aktywnych zgłoszeń — bądź pierwszy.", fontSize = 12.sp, color = WatahaColors.Grey) }
            }
            items(needs, key = { "n_" + it.id }) { p ->
                HelpCard(p.title, "🆘", p.body, p.authorName, p.createdAt, repo.distanceKm(p.lat, p.lng)) {
                    onOpen(p.id)
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Text("🤝 OFERTY POMOCY (${offers.size})", style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
            }
            if (offers.isEmpty()) {
                item { Text("Brak ofert — zaproponuj swoją pomoc.", fontSize = 12.sp, color = WatahaColors.Grey) }
            }
            items(offers, key = { "o_" + it.id }) { p ->
                HelpCard(p.title, "🤝", p.body, p.authorName, p.createdAt, repo.distanceKm(p.lat, p.lng)) {
                    onOpen(p.id)
                }
            }
            item {
                Spacer(Modifier.height(20.dp))
                OutlinedButton(onClick = onNeed, modifier = Modifier.fillMaxWidth()) {
                    Text("🚨 Zgłoś potrzebę pilną (Tryb Kryzysowy)")
                }
            }
        }
    }
}

@Composable
fun HelpCard(title: String, emoji: String, body: String, author: String, ts: Long, km: Float, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = WatahaColors.LightGrey),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp).clickable(onClick = onClick)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f))
                DistLabel(km)
            }
            Text(body, fontSize = 13.sp, color = WatahaColors.Grey, maxLines = 2)
            Text("${author} • ${timeAgo(ts)}", fontSize = 11.sp, color = WatahaColors.Grey)
        }
    }
}
