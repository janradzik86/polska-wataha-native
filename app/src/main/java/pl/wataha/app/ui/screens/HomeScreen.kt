package pl.wataha.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.wataha.app.R
import pl.wataha.app.ai.SurvivalData
import pl.wataha.app.comm.CommManager
import pl.wataha.app.data.model.PostEntity
import pl.wataha.app.data.model.PostType
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.DistLabel
import pl.wataha.app.ui.components.EmojiTile
import pl.wataha.app.ui.components.GlassCard
import pl.wataha.app.ui.components.GradientButton
import pl.wataha.app.ui.components.NeoChip
import pl.wataha.app.ui.components.SectionTitle
import pl.wataha.app.ui.components.StatusPill
import pl.wataha.app.ui.components.timeAgo
import pl.wataha.app.ui.theme.WatahaColors
import java.time.LocalDate

@Composable
fun HomeScreen(
    repo: Repository,
    comm: CommManager,
    onOpenDrawer: () -> Unit,
    onNav: (String) -> Unit
) {
    val me by repo.me.collectAsState()
    val online by repo.online.collectAsState()
    val posts by repo.posts.collectAsState(initial = emptyList())
    val pending by repo.pendingCount.collectAsState(initial = 0)
    val nodes by repo.nodes.collectAsState(initial = emptyList())

    val quick = listOf(
        "assistant" to "🧠 Asystent WILK",
        "survival" to "🏕️ Survival",
        "map" to "🗺️ Mapa",
        "exchanges" to "🔁 Wymiany",
        "help" to "🤝 Pomoc",
        "network" to "📡 Sieć",
        "mesh" to "🧪 MESH LAB",
        "feedback" to "💡 Pomysły"
    )

    LazyColumn(
        Modifier.fillMaxSize().background(WatahaColors.White),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // ── HERO: orzeł w koronie (fotorealistyczny) + wataha ──
        item {
            Box(Modifier.fillMaxWidth().height(270.dp)) {
                Image(
                    painter = painterResource(R.drawable.hero_eagle),
                    contentDescription = "Orzeł w koronie",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                WatahaColors.Ink.copy(alpha = 0.45f),
                                Color.Transparent,
                                WatahaColors.White.copy(alpha = 0.92f)
                            )
                        )
                    )
                )
                // górny pasek: menu + nazwa
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text("POLSKA WATAHA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("biel i czerwień • ${me?.displayName ?: "…"}", color = Color.White.copy(0.9f), fontSize = 10.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onNav("notification-test") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Powiadomienia", tint = Color.White)
                    }
                }
                // dolny blok hero
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text("CZARNY WILK CZUWA 🐺", color = WatahaColors.DarkRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("Razem przetrwamy", color = WatahaColors.Ink, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp)
                    Text("Wymieniaj • Pomagaj • Przetrwaj — lokalnie, offline, po sąsiedzku",
                        color = WatahaColors.Grey, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    // wyszukiwarka OLX
                    Row(
                        Modifier.fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = WatahaColors.Ink.copy(alpha = 0.3f))
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .clickable { onNav("posts") }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔍", fontSize = 15.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Szukaj: oddam, potrzebuję, pomoc, wymiana…",
                            color = WatahaColors.Grey, fontSize = 13.sp)
                    }
                }
            }
        }

        // ── TRYB KRYZYSOWY — wielki 3D ──
        item {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                GradientButton(
                    text = "TRYB KRYZYSOWY",
                    icon = "🚨",
                    colors = listOf(WatahaColors.FlagRed, Color(0xFF7A0018)),
                    height = 62.dp,
                    fontSize = 17,
                    modifier = Modifier.fillMaxWidth()
                ) { onNav("crisis") }
            }
        }

        // ── szybkie wejścia ──
        item {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quick.forEach { (route, label) ->
                    NeoChip(text = label.substringAfter(" "), emoji = label.take(2), selected = false) {
                        onNav(route)
                    }
                }
            }
        }

        // ── status ──
        item {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(if (online) "🟢 ONLINE" else "🔴 OFFLINE", if (online) WatahaColors.Green else WatahaColors.FlagRed)
                StatusPill("⏳ kolejka: $pending", if (pending > 0) WatahaColors.Amber else WatahaColors.Green)
                StatusPill("📡 węzły: ${nodes.count { it.status == "ONLINE" }}/${nodes.size}", WatahaColors.Blue)
            }
        }

        // ── ogłoszenia na stronie głównej (OLX) ──
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("NAJNOWSZE OGŁOSZENIA", Modifier.weight(1f))
                Text("Zobacz wszystkie →", color = WatahaColors.FlagRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNav("posts") })
            }
        }
        items(posts.take(8), key = { "h_" + it.id }) { p ->
            PostBigCard(repo, p, onOpen = { onNav("post/${p.id}") })
        }

        // ── wskazówka dnia ──
        item {
            Spacer(Modifier.height(10.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                GlassCard {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 26.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("WSKAZÓWKA WATAHY NA DZIŚ", color = WatahaColors.DarkRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                SurvivalData.tipForDay(LocalDate.now().dayOfYear),
                                color = WatahaColors.Ink, fontSize = 13.sp, lineHeight = 18.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("🇵🇱 Polska Wataha V0.2 • Czarny Wilk — Strażnik Prawdy",
                    color = WatahaColors.Grey, fontSize = 10.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun PostBigCard(repo: Repository, p: PostEntity, onOpen: () -> Unit) {
    val type = runCatching { PostType.valueOf(p.type) }.getOrDefault(PostType.GIVE)
    val gradient = when (type) {
        PostType.GIVE -> listOf(WatahaColors.FlagRed, WatahaColors.DarkRed)
        PostType.NEED -> listOf(Color(0xFFF59E0B), Color(0xFFB45309))
        PostType.OFFER -> listOf(Color(0xFF2E7D32), Color(0xFF14532D))
    }
    val tag = when (type) {
        PostType.GIVE -> "WYMIANA"
        PostType.NEED -> "POTRZEBUJĘ"
        PostType.OFFER -> "POMOGĘ"
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
        GlassCard(onClick = onOpen) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmojiTile(type.emoji, size = 54.dp, gradient = gradient, fontSize = 25)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WatahaColors.Ink, maxLines = 2)
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(p.authorName, fontSize = 11.sp, color = WatahaColors.Grey)
                            Text("  •  ", fontSize = 11.sp, color = WatahaColors.Grey)
                            Text(timeAgo(p.createdAt), fontSize = 11.sp, color = WatahaColors.Grey)
                        }
                    }
                    StatusPill(tag, gradient.first())
                    Spacer(Modifier.width(6.dp))
                    DistLabel(repo.distanceKm(p.lat, p.lng))
                }
                Spacer(Modifier.height(8.dp))
                Text(p.body, fontSize = 12.sp, color = WatahaColors.Grey, maxLines = 2, lineHeight = 16.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GradientButton("Napisz", icon = "💬", colors = listOf(WatahaColors.Blue, Color(0xFF1E3A8A)),
                        height = 38.dp, fontSize = 12,
                        modifier = Modifier.weight(1f)) { onOpen() }
                    if (type == PostType.GIVE) {
                        GradientButton("Wymiana", icon = "🔁", colors = gradient, height = 38.dp, fontSize = 12,
                            modifier = Modifier.weight(1f)) { onOpen() }
                    }
                    if (type == PostType.NEED) {
                        GradientButton("Pomogę", icon = "🤝", colors = listOf(Color(0xFF2E7D32), Color(0xFF14532D)),
                            height = 38.dp, fontSize = 12,
                            modifier = Modifier.weight(1f)) { onOpen() }
                    }
                }
            }
        }
    }
}
