package pl.wataha.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.wataha.app.comm.MeshSim
import pl.wataha.app.ui.components.StatusPill
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.theme.WatahaColors

/** MESH LAB — symulator sieci węzłów A→B→C→D (bez fizycznego LoRa). */
@Composable
fun MeshLabScreen(comm: pl.wataha.app.comm.CommManager, onBack: () -> Unit) {
    val mesh = comm.mesh
    val events by mesh.events.collectAsState()
    val buffered by mesh.buffered.collectAsState()
    val scope = rememberCoroutineScope()

    var failNode by remember { mutableStateOf<String?>(null) }

    val pathAD = remember(mesh.nodes.values.map { it.online }) { mesh.route("A", "D") }
    val pathLabel = mesh.pathLabel(pathAD)

    val positions = mapOf(
        "A" to Offset(0.22f, 0.26f),
        "B" to Offset(0.78f, 0.26f),
        "C" to Offset(0.22f, 0.74f),
        "D" to Offset(0.78f, 0.74f)
    )
    val edges = listOf("AB" to arrayOf("A", "B"), "AC" to arrayOf("A", "C"), "BC" to arrayOf("B", "C"), "CD" to arrayOf("C", "D"))

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("MESH LAB / SYMULATOR", "Topologia: A—B—C—D (+ trasa zapasowa A—C)", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(14.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(14.dp)) {
                    Canvas(
                        Modifier.fillMaxWidth().height(230.dp).pointerInput(mesh.nodes.values.map { it.online }) {
                            detectTapGestures { offset ->
                                var best: String? = null
                                var bestD = 60f
                                positions.forEach { (id, p) ->
                                    val x = p.x * size.width
                                    val y = p.y * size.height
                                    val d = kotlin.math.hypot(x - offset.x, y - offset.y)
                                    if (d < bestD) { bestD = d; best = id }
                                }
                                best?.let { failNode = it }
                            }
                        }
                    ) {
                        val w = size.width
                        val h = size.height

                        // krawędzie
                        edges.forEach { (_, ab) ->
                            val a = positions[ab[0]]!!
                            val b = positions[ab[1]]!!
                            val aOn = mesh.nodes[ab[0]]!!.online
                            val bOn = mesh.nodes[ab[1]]!!.online
                            val inPath = pathAD.zipWithNext().any { it.first == ab[0] && it.second == ab[1] } ||
                                    pathAD.zipWithNext().any { it.first == ab[1] && it.second == ab[0] }
                            val color = when {
                                !aOn || !bOn -> WatahaColors.Grey.copy(0.35f)
                                inPath -> WatahaColors.FlagRed
                                else -> WatahaColors.Grey.copy(0.5f)
                            }
                            drawLine(
                                color, Offset(a.x * w, a.y * h), Offset(b.x * w, b.y * h),
                                strokeWidth = if (inPath) 5f else 3f, cap = StrokeCap.Round
                            )
                        }

                        // węzły
                        positions.forEach { (id, p) ->
                            val n = mesh.nodes[id]!!
                            val cx = p.x * w
                            val cy = p.y * h
                            drawCircle(Color.White, 34f, Offset(cx, cy))
                            drawCircle(
                                if (n.online) WatahaColors.FlagRed else WatahaColors.Grey,
                                30f, Offset(cx, cy)
                            )
                            drawCircle(
                                if (n.online) Color.White else WatahaColors.LightGrey,
                                26f, Offset(cx, cy)
                            )
                            drawCircle(
                                if (n.online) WatahaColors.FlagRed else WatahaColors.LightGrey,
                                9f, Offset(cx, cy)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.LightGrey), shape = RoundedCornerShape(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        mesh.nodes.values.forEach { n ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(n.name, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    color = if (n.online) WatahaColors.Green else WatahaColors.FlagRed)
                                Text("${n.battery}%", fontSize = 9.sp, color = WatahaColors.Grey)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.LightRed), shape = RoundedCornerShape(10.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("🛰️ TRASA A → D:  ${pathLabel}", fontWeight = FontWeight.Bold, fontSize = 13.sp,
                            color = if (pathAD.isEmpty()) WatahaColors.FlagRed else WatahaColors.DarkRed)
                        Text("Dotknij węzeł na schemacie, aby zasymulować awarię.",
                            fontSize = 11.sp, color = WatahaColors.Ink)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { scope.launch { mesh.sendPacket("A", "D") } },
                        colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                        modifier = Modifier.weight(1f)
                    ) { Text("WYŚLIJ A→D", fontSize = 12.sp) }
                    Button(
                        onClick = { scope.launch { mesh.sendPacket("A", "C") } },
                        colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.Ink),
                        modifier = Modifier.weight(1f)
                    ) { Text("WYŚLIJ A→C", fontSize = 12.sp) }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                mesh.buffered.value.forEach { b ->
                                    mesh.flushBuffers(b.node, b.target)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("📤 WZNÓW DORĘCZANIE", fontSize = 12.sp) }
                    OutlinedButton(onClick = { mesh.resetLog() }, modifier = Modifier.weight(1f)) {
                        Text("Wyczyść log", fontSize = 12.sp)
                    }
                }
                if (buffered.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("🧊 STORE-AND-FORWARD — pakiety w buforze: ${buffered.size}",
                        fontSize = 12.sp, color = WatahaColors.Amber, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                Text("DZIENNIK PRZEBIEGU", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.Ink), shape = RoundedCornerShape(10.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        events.takeLast(14).forEach { line ->
                            Text(line, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(vertical = 1.dp))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("LoRa w warstwie APPLICATION:  aplikacja → CommService → LoRaAdapter → moduł E22/SX1262 (V0.4).\nBez modułu fizycznego symulator pozwala testować routing już dziś.",
                    fontSize = 10.sp, color = WatahaColors.Grey)
            }
        }
    }

    val fn = failNode
    if (fn != null) {
        val n = mesh.nodes[fn]!!
        AlertDialog(
            onDismissRequest = { failNode = null },
            title = { Text("${n.name} — ${if (n.online) "ONLINE" else "AWARIA"}") },
            text = {
                Column {
                    Text("Bateria: ${n.battery}% • Sąsiedzi: ${n.neighbors.joinToString(", ")}")
                    Spacer(Modifier.height(8.dp))
                    if (n.online) {
                        Text("Symulacja awarii spowoduje przeliczenie trasy (store-and-forward obejmie pakiety).")
                    } else {
                        Text("Wznowienie pracy spowoduje wznowienie routingu i doręczenie buforów.")
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (n.online) WatahaColors.Grey else WatahaColors.Green
                    ),
                    onClick = {
                        mesh.setFailed(fn, n.online) // przełącza
                        failNode = null
                    }
                ) { Text(if (n.online) "ZASYMULUJ AWARIĘ" else "WZNÓW PRACĘ") }
            },
            dismissButton = { OutlinedButton(onClick = { failNode = null }) { Text("Anuluj") } }
        )
    }
}
