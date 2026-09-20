package pl.wataha.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.wataha.app.R
import pl.wataha.app.WatahaApp
import pl.wataha.app.ui.theme.WatahaColors

private data class MenuItem(val emoji: String, val label: String, val route: String?, val danger: Boolean = false)

/**
 * Wysuwane menu — lewa strona, zamykane ✕. W tle wataha czarnych wilków,
 * u góry profil użytkownika, pozycje z animacją dotyku.
 */
@Composable
fun WatahaDrawerContent(
    app: WatahaApp,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    onLogout: () -> Unit
) {
    val me by app.repo.me.collectAsState()
    val online by app.repo.online.collectAsState()

    val items = listOf(
        MenuItem("🛡️", "Pulpit", "home"),
        MenuItem("📦", "Ogłoszenia", "posts"),
        MenuItem("🔁", "Wymiany", "exchanges"),
        MenuItem("🤝", "Pomoc", "help"),
        MenuItem("🗺️", "Mapa", "map"),
        MenuItem("🧠", "Asystent WILK", "assistant"),
        MenuItem("🏕️", "Poradnik survivalowy", "survival"),
        MenuItem("💬", "Wiadomości", "chat"),
        MenuItem("🏅", "Reputacja i odznaki", "reputation"),
        MenuItem("📡", "Status sieci", "network"),
        MenuItem("🧪", "MESH LAB — symulator", "mesh"),
        MenuItem("🔔", "Powiadomienia", "notification-test"),
        MenuItem("💡", "Masz pomysł? Napisz!", "feedback"),
        MenuItem("⚙️", "Profil i ustawienia", "profile"),
        MenuItem("🚨", "TRYB KRYZYSOWY", "crisis", danger = true)
    )

    Column(Modifier.fillMaxSize().background(Color.White)) {
        // ── nagłówek: wataha wilków + zamknięcie ✕ ──
        Box(Modifier.fillMaxWidth().height(190.dp)) {
            Image(
                painter = painterResource(R.drawable.wolf_pack),
                contentDescription = "Wataha czarnych wilków",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(
                            WatahaColors.Ink.copy(alpha = 0.55f),
                            WatahaColors.Ink.copy(alpha = 0.25f),
                            WatahaColors.Ink.copy(alpha = 0.35f)
                        )
                    )
                )
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                    .clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.18f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Zamknij menu", tint = Color.White)
            }
            Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text("● WATAHA", color = Color.White.copy(0.85f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("POLSKA WATAHA", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                Text("Czarny Wilk — Strażnik Prawdy", color = Color.White.copy(0.8f), fontSize = 11.sp)
            }
        }

        // ── profil użytkownika ──
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(me?.displayName ?: "?", size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(me?.displayName ?: "…", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WatahaColors.Ink)
                Text("@${me?.username ?: "…"} • ⭐ ${me?.reputation ?: 0.0}", fontSize = 11.sp, color = WatahaColors.Grey)
            }
            StatusPill(if (online) "ONLINE" else "OFFLINE", if (online) WatahaColors.Green else WatahaColors.Grey)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(WatahaColors.Border))

        // ── pozycje menu ──
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 10.dp)
        ) {
            items.forEach { item ->
                val shape = RoundedCornerShape(12.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(shape)
                        .background(
                            if (item.danger) Brush.horizontalGradient(listOf(WatahaColors.FlagRed, WatahaColors.DarkRed))
                            else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable {
                            if (item.route == null) onLogout() else onNavigate(item.route)
                        }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.emoji, fontSize = 17.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        item.label,
                        fontSize = 14.sp,
                        fontWeight = if (item.danger) FontWeight.Bold else FontWeight.Medium,
                        color = if (item.danger) Color.White else WatahaColors.Ink
                    )
                    Spacer(Modifier.weight(1f))
                    if (item.danger) {
                        Text("! ! !", color = Color.White.copy(0.9f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ── stopka ──
        Box(Modifier.fillMaxWidth().height(1.dp).background(WatahaColors.Border))
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("V0.2 • offline-first", fontSize = 10.sp, color = WatahaColors.Grey, modifier = Modifier.weight(1f))
            Text(
                "🚪 Wyloguj",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = WatahaColors.FlagRed,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onLogout() }.padding(8.dp)
            )
        }
    }
}
