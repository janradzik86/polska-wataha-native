package pl.wataha.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.wataha.app.comm.CommManager
import pl.wataha.app.comm.TransportKind
import pl.wataha.app.comm.TransportState
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.SectionCard
import pl.wataha.app.ui.components.StatusPill
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.theme.WatahaColors

@Composable
fun NetworkScreen(repo: Repository, comm: CommManager, onBack: () -> Unit, onMesh: () -> Unit) {
    val statuses by comm.statuses.collectAsState()
    val online by repo.online.collectAsState()
    val pending by repo.pendingCount.collectAsState(initial = 0)
    val log by comm.testLog.collectAsState()
    val nodes by repo.nodes.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var bleResult by remember { mutableStateOf<String?>(null) }

    val blePerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.BLUETOOTH_SCAN] == true
        if (granted) {
            scope.launch {
                val n = comm.bleScanCount()
                bleResult = if (n < 0) "Skan BLE niemożliwy (Bluetooth wyłączony?)" else "Wykryto $n urządzeń BLE w pobliżu."
            }
        } else {
            bleResult = "Brak uprawnień Bluetooth — przyznaj, aby przeskanować."
        }
    }

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Status sieci", "ARCHITEKTURA: APP → COMM SERVICE → ADAPTERY", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(14.dp)) {
            item {
                SectionCard("🌐 Połączenie") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (online) "🟢 Internet: DOSTĘPNY" else "🔴 Internet: BRAK / OFFLINE DEMO",
                            Modifier.weight(1f), fontWeight = FontWeight.Bold,
                            color = if (online) WatahaColors.Green else WatahaColors.FlagRed)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row {
                        StatusPill("Kolejka sync: $pending", if (pending > 0) WatahaColors.Amber else WatahaColors.Green)
                        Spacer(Modifier.width(8.dp))
                        StatusPill("Backend: ${repo.apiBase}", WatahaColors.Blue)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val (ok, msg) = repo.pingServer()
                                comm.logTest("PING → ${if (ok) "OK" else "FAIL"}: $msg")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.Ink),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🏓 TEST POŁĄCZENIA (PING)") }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { scope.launch { val n = repo.syncNow(); comm.logTest("Synchronizacja: $n rekordów wysłanych.") } },
                        colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🔄 SYNCHRONIZUJ KOLEJKĘ ($pending)") }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Adaptery
            item {
                Text("ARCHITEKTURA KOMUNIKACJI", style = MaterialTheme.typography.titleSmall)
                Text("COMMUNICATION ADAPTER → INTERNET / BLUETOOTH / WI-FI DIRECT / LoRa",
                    fontSize = 10.sp, color = WatahaColors.Grey)
                Spacer(Modifier.height(6.dp))
            }
            items(comm.adapters.toList()) { adapter ->
                val st = statuses[adapter.kind] ?: TransportState.OFFLINE
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (adapter.kind) {
                            TransportKind.INTERNET -> WatahaColors.White
                            TransportKind.LORA -> WatahaColors.LightGrey
                            else -> WatahaColors.White
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${adapter.kind.emoji}  ${adapter.kind.label}", Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            StatusPill(st.label, when (st) {
                                TransportState.READY -> WatahaColors.Green
                                TransportState.SCANNING -> WatahaColors.Amber
                                TransportState.DEMO -> WatahaColors.Blue
                                TransportState.OFFLINE -> WatahaColors.Grey
                                TransportState.NO_HARDWARE -> WatahaColors.Grey
                            })
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(adapter.describe(), fontSize = 11.sp, color = WatahaColors.Grey)
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { scope.launch { comm.testTransport(adapter.kind) } },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("Przetestuj warstwę", fontSize = 12.sp) }
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.LightRed),
                    shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("🛠️ TEST BLE (realne skanowanie)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WatahaColors.DarkRed)
                        Text("BluetoothAdapter wykonuje prawdziwy skan BLE — policzy urządzenia w pobliżu.",
                            fontSize = 11.sp, color = WatahaColors.Ink)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { blePerm.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)) },
                            colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.DarkRed),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("🔵 URUCHOM SKAN BLE (4 s)") }
                        if (bleResult != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(bleResult ?: "", fontSize = 12.sp, color = WatahaColors.Ink)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                SectionCard("🧪 MESH LAB") {
                    Text("Symulator węzłów A–D: testuj routing i awarie bez fizycznego sprzętu LoRa.",
                        fontSize = 12.sp, color = WatahaColors.Grey)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        StatusPill("Węzły online: ${nodes.count { it.status == "ONLINE" }}/${nodes.size}", WatahaColors.Green)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onMesh, colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.Ink),
                        modifier = Modifier.fillMaxWidth()) {
                        Text("OTWÓRZ MESH LAB / SYMULATOR")
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            item {
                Text("DZIENNIK TESTÓW", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Card(colors = CardDefaults.cardColors(containerColor = WatahaColors.Ink), shape = RoundedCornerShape(10.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        if (log.isEmpty()) {
                            Text("— brak testów —", color = Color.White.copy(0.6f), fontSize = 11.sp)
                        }
                        log.reversed().take(20).forEach { line ->
                            Text(line, color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
