package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.wataha.app.data.db.Badges
import pl.wataha.app.data.model.UserEntity
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.Avatar
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.theme.WatahaColors

@Composable
fun ReputationScreen(repo: Repository, onBack: () -> Unit) {
    val users by repo.users.collectAsState(initial = emptyList())
    val me by repo.me.collectAsState()
    val myBadges by (me?.let { repo.badges(it.id) }?.collectAsState(initial = emptyList()))
        ?: remember { androidx.compose.runtime.mutableStateOf(emptyList<pl.wataha.app.data.model.BadgeEntity>()) }

    val earnedCodes = myBadges.map { it.code }.toSet()
    val medal = when {
        (me?.reputation ?: 0.0) >= 4.5 -> "🥇"
        (me?.reputation ?: 0.0) >= 4.0 -> "🥈"
        else -> "🥉"
    }

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Reputacja", "Zaufanie w watahe", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(14.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$medal  Twoja reputacja", style = MaterialTheme.typography.titleSmall)
                        Text("%.1f / 5.0".format(me?.reputation ?: 0.0),
                            style = MaterialTheme.typography.headlineLarge, color = WatahaColors.FlagRed)
                        LinearProgressIndicator(
                            progress = { ((me?.reputation ?: 0.0) / 5.0).toFloat() },
                            color = WatahaColors.FlagRed,
                            trackColor = WatahaColors.LightRed,
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        )
                        Text("Reputację budują: rzetelne wymiany, pomoc i oddane rzeczy.",
                            fontSize = 11.sp, color = WatahaColors.Grey)
                    }
                }
                Spacer(Modifier.padding(top = 8.dp))
                Text("🎖️ ODZNAKI — KATALOG", style = MaterialTheme.typography.titleSmall)
                Row {
                    Badges.ALL.chunked(2).forEach { row ->
                        Column(Modifier.weight(1f)) {
                            row.forEach { b ->
                                val earned = b.code in earnedCodes
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (earned) WatahaColors.LightRed else WatahaColors.LightGrey
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(b.emoji, fontSize = 18.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(b.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                                color = if (earned) WatahaColors.Ink else WatahaColors.Grey)
                                            Text(b.desc, fontSize = 10.sp, color = WatahaColors.Grey)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.padding(top = 10.dp))
                Text("🏆 RANKING WATAHY", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.padding(top = 4.dp))
            }
            items(users, key = { it.id }) { u ->
                UserRankRow(u, isMe = u.id == me?.id)
            }
        }
    }
}

@Composable
fun UserRankRow(u: UserEntity, isMe: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = if (isMe) WatahaColors.LightRed else WatahaColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMe) 1.dp else 0.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(u.displayName, size = 34.dp,
                color = if (u.reputation >= 4.5) WatahaColors.Amber else WatahaColors.FlagRed)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(u.displayName + if (isMe) " (Ty)" else "", fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, color = WatahaColors.Ink)
                Text("@${u.username} • ${u.bio.take(40)}", fontSize = 10.sp, color = WatahaColors.Grey, maxLines = 1)
            }
            Box(Modifier.background(if (u.reputation >= 4.5) WatahaColors.Amber else WatahaColors.Blue, CircleShape)
                .padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text("%.1f".format(u.reputation), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
