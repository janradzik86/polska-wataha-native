package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.wataha.app.data.model.PostType
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.theme.WatahaColors

@Composable
fun PostsScreen(
    repo: Repository,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit
) {
    val posts by repo.posts.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<String?>(null) }

    val filtered = posts
        .filter { filter == null || it.type == filter }
        .filter {
            query.isBlank() ||
                    it.title.contains(query, true) ||
                    it.body.contains(query, true) ||
                    it.authorName.contains(query, true)
        }

    Scaffold(
        containerColor = WatahaColors.White,
        topBar = { WatahaTopBar("Ogłoszenia", "Społeczność — oddaj, pomóż, poproś", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate, containerColor = WatahaColors.FlagRed) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj ogłoszenie", tint = WatahaColors.White)
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("🔍  Szukaj: rzecz, pomoc, osoba…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filter == null, onClick = { filter = null },
                    label = { Text("Wszystkie") })
                PostType.entries.forEach { t ->
                    FilterChip(selected = filter == t.name, onClick = { filter = t.name },
                        label = { Text("${t.emoji} ${t.label}") })
                }
            }
            Spacer(Modifier.height(6.dp))
            if (filtered.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("🦅", fontSize = 40.sp)
                    Text("Brak wyników", style = MaterialTheme.typography.titleMedium)
                    Text("Zmień filtry albo dodaj własne ogłoszenie.", color = WatahaColors.Grey)
                }
            }
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
                items(filtered, key = { it.id }) { p ->
                    PostRow(p, repo) { onOpen(p.id) }
                }
            }
        }
    }
}

@Composable
private fun PostRow(p: pl.wataha.app.data.model.PostEntity, repo: Repository, onClick: () -> Unit) {
    val type = runCatching { PostType.valueOf(p.type) }.getOrDefault(PostType.GIVE)
    Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        pl.wataha.app.ui.components.GlassCard(onClick = onClick) {
            Row(Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                pl.wataha.app.ui.components.EmojiTile(type.emoji, size = 48.dp, fontSize = 22)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.title, style = MaterialTheme.typography.titleSmall, color = WatahaColors.Ink, maxLines = 2)
                    Spacer(Modifier.height(2.dp))
                    Text("${p.authorName} • ${pl.wataha.app.ui.components.timeAgo(p.createdAt)}",
                        fontSize = 11.sp, color = WatahaColors.Grey)
                }
                pl.wataha.app.ui.components.DistLabel(repo.distanceKm(p.lat, p.lng))
            }
        }
    }
}
