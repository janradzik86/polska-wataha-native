package pl.wataha.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.wataha.app.ai.SurvivalData
import pl.wataha.app.ai.SurvivalSection
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.EmojiTile
import pl.wataha.app.ui.components.GlassCard
import pl.wataha.app.ui.components.GradientButton
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.theme.WatahaColors
import java.text.Normalizer

/** PORADNIK SURVIVALOWY — 12 sekcji, w całości offline. */
@Composable
fun SurvivalScreen(repo: Repository, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var openId by remember { mutableStateOf<String?>("bagaz72") }
    var checked by remember { mutableStateOf(setOf<String>()) }

    fun norm(s: String) = Normalizer.normalize(s, Normalizer.Form.NFD).replace("\\p{Mn}+".toRegex(), "").lowercase()

    val sections = SurvivalData.sections.filter { s ->
        query.isBlank() ||
                norm(s.title).contains(norm(query)) ||
                norm(s.tags.joinToString(" ")).contains(norm(query))
    }

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Poradnik survivalowy", "12 sekcji • offline • lista 72h z checklistą", onBack = onBack)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("🔍  Szukaj: woda, rko, powódź, ogień…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
            items(sections, key = { it.id }) { s ->
                SurvivalSectionCard(
                    section = s,
                    expanded = openId == s.id,
                    checked = checked,
                    onToggleOpen = { openId = if (openId == s.id) null else s.id },
                    onToggleCheck = { point -> checked = if (point in checked) checked - point else checked + point }
                )
            }
            item {
                Spacer(Modifier.height(10.dp))
                GlassCard {
                    Column(Modifier.padding(14.dp)) {
                        Text("🧠 Masz wątpliwości?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Zapytaj Asystenta WILK (menu) — odpowiada głosem i tekstem o sytuacjach kryzysowych i przetrwaniu.",
                            fontSize = 11.sp, color = WatahaColors.Grey, lineHeight = 16.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SurvivalSectionCard(
    section: SurvivalSection,
    expanded: Boolean,
    checked: Set<String>,
    onToggleOpen: () -> Unit,
    onToggleCheck: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        GlassCard(modifier = Modifier.animateContentSize(tween(220)), onClick = onToggleOpen) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmojiTile(section.emoji, size = 46.dp, fontSize = 22)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(section.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WatahaColors.Ink)
                        Text(section.tags.take(3).joinToString(" • "), fontSize = 10.sp, color = WatahaColors.Grey)
                    }
                    Text(if (expanded) "▲" else "▼", color = WatahaColors.FlagRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                if (expanded) {
                    Spacer(Modifier.height(10.dp))
                    Column {
                        section.points.forEachIndexed { i, point ->
                            val key = section.id + "_" + i
                            val isCheck = section.id == "bagaz72"
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(MaterialTheme.shapes.small)
                                    .background(if (checked.contains(key)) WatahaColors.LightRed else WatahaColors.White)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isCheck) {
                                    Checkbox(
                                        checked = checked.contains(key),
                                        onCheckedChange = { onToggleCheck(key) }
                                    )
                                } else {
                                    Text("•", color = WatahaColors.FlagRed, fontSize = 14.sp)
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(
                                    point,
                                    fontSize = 12.sp,
                                    color = WatahaColors.Ink,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    if (section.id == "bagaz72") {
                        Spacer(Modifier.height(8.dp))
                        GradientButton(
                            text = "✔ GOTOWY: ${checked.size}/${section.points.size}",
                            colors = if (checked.size == section.points.size)
                                listOf(WatahaColors.Green, Color(0xFF14532D))
                            else listOf(WatahaColors.Ink, Color(0xFF0F172A)),
                            height = 40.dp, fontSize = 12,
                            modifier = Modifier.fillMaxWidth()
                        ) { onToggleOpen() }
                    }
                }
            }
        }
    }
}
