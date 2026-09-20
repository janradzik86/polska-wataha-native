@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package pl.wataha.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.wataha.app.data.model.MarkerKind
import pl.wataha.app.data.model.PostType
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.theme.WatahaColors

data class UiPoint(
    val id: String,
    val kind: String,     // POST / CRISIS / MARKER / NODE / SELF
    val type: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val lat: Double,
    val lng: Double,
    val color: Color,
    val authorId: String = ""
)

@Composable
fun MapScreen(repo: Repository, onBack: () -> Unit, onWriteTo: (String) -> Unit) {
    val posts by repo.posts.collectAsState(initial = emptyList())
    val markers by repo.markers.collectAsState(initial = emptyList())
    val nodes by repo.nodes.collectAsState(initial = emptyList())
    val crisis by repo.crisis.collectAsState(initial = emptyList())
    val me by repo.me.collectAsState()

    val context = LocalContext.current
    var filter by remember { mutableStateOf<String>("ALL") }
    var selected by remember { mutableStateOf<UiPoint?>(null) }
    var gpsGranted by remember { mutableStateOf(
        androidx.core.content.ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) }
    val scope = rememberCoroutineScope()

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        gpsGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (gpsGranted) {
            // odczyt pozycji
            try {
                val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
                val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) repo.setMyLocation(loc.latitude, loc.longitude)
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(Unit) {
        if (gpsGranted) {
            try {
                val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
                val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) repo.setMyLocation(loc.latitude, loc.longitude)
            } catch (_: Exception) {
            }
        }
    }

    val self = repo.myLocation()

    val points = remember(posts, markers, nodes, crisis, filter, self, me) {
        val list = mutableListOf<UiPoint>()
        posts.filter {
            when (filter) {
                "ALL" -> true
                "CRISIS" -> false
                else -> it.type == filter
            }
        }.forEach { p ->
            val t = runCatching { PostType.valueOf(p.type) }.getOrDefault(PostType.GIVE)
            val color = when (t) {
                PostType.GIVE -> WatahaColors.FlagRed
                PostType.NEED -> WatahaColors.Amber
                PostType.OFFER -> WatahaColors.Green
            }
            list += UiPoint(p.id, "POST", t.name, p.title, "${t.emoji} ${t.label} • ${p.authorName}", t.emoji, p.lat, p.lng, color, p.authorId)
        }
        if (filter == "ALL" || filter == "CRISIS") {
            crisis.take(15).forEach { c ->
                list += UiPoint(c.id, "CRISIS", c.type, "${c.userName}: ${c.type}", c.text.take(80), "🚨", c.lat, c.lng, WatahaColors.DarkRed)
            }
        }
        if (filter == "ALL" || filter == "POINTS") {
            markers.forEach { m ->
                val kind = runCatching { MarkerKind.valueOf(m.kind) }.getOrDefault(MarkerKind.POINT_POMOCY)
                list += UiPoint(m.id, "MARKER", m.kind, m.title, m.subtitle, m.emoji, m.lat, m.lng, Color(kind.color))
            }
        }
        if (filter == "ALL" || filter == "NODES") {
            nodes.filter { it.type == "MESH" || it.type == "GATEWAY" }.forEach { n ->
                list += UiPoint(n.id, "NODE", n.type, n.name, "${n.status} • bateria ${n.battery}%", "📡", n.lat, n.lng,
                    if (n.status == "ONLINE") WatahaColors.Blue else WatahaColors.Grey)
            }
        }
        if (filter == "ALL" || filter == "SELF") {
            list += UiPoint("self", "SELF", "", "Twoja pozycja", if (gpsGranted) "GPS on" else "GPS off — pozycja domyślna (Warszawa)", "📍", self.first, self.second, WatahaColors.Blue)
        }
        list
    }

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Mapa", "Ogłoszenia • pomoc • punkty • sieć • Ty", onBack = onBack)

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(selected = filter == "ALL", onClick = { filter = "ALL" }, label = { Text("Wszystko") })
            FilterChip(selected = filter == "GIVE", onClick = { filter = "GIVE" }, label = { Text("📦") })
            FilterChip(selected = filter == "NEED", onClick = { filter = "NEED" }, label = { Text("🆘") })
            FilterChip(selected = filter == "OFFER", onClick = { filter = "OFFER" }, label = { Text("🤝") })
            FilterChip(selected = filter == "POINTS", onClick = { filter = "POINTS" }, label = { Text("🏛") })
            FilterChip(selected = filter == "CRISIS", onClick = { filter = "CRISIS" }, label = { Text("🚨") })
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            CommunityMap(
                points = points,
                self = self,
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                onSelect = { selected = it }
            )
            Card(
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                colors = CardDefaults.cardColors(containerColor = WatahaColors.White.copy(0.92f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text("🗺️ Mapa lokalna", fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("• bez klucza Google Maps\n• działa offline", fontSize = 9.sp, color = WatahaColors.Grey)
                    OutlinedButton(
                        onClick = { permLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
                    ) { Text("🎯 GPS", fontSize = 10.sp) }
                }
            }
        }

        // Karta szczegółów wybranego punktu
        val sel = selected
        if (sel != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                colors = CardDefaults.cardColors(containerColor = WatahaColors.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    Modifier.padding(14.dp).verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(sel.emoji, fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(sel.title, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                            Text("${sel.subtitle} • %.2f, %.2f".format(sel.lat, sel.lng), fontSize = 11.sp, color = WatahaColors.Grey)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { selected = null }, modifier = Modifier.weight(1f)) { Text("Zamknij") }
                        OutlinedButton(
                            onClick = {
                                val geo = "geo:${sel.lat},${sel.lng}?q=${sel.lat},${sel.lng}"
                                try {
                                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(geo))
                                    if (i.resolveActivity(context.packageManager) != null) context.startActivity(i)
                                } catch (_: Exception) {
                                }
                            }, modifier = Modifier.weight(1f)
                        ) { Text("🧭 Nawiguj") }
                        if (sel.kind == "POST") {
                            Button(
                                onClick = { onWriteTo(sel.authorId) },
                                colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                                modifier = Modifier.weight(1f)
                            ) { Text("💬 Napisz") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityMap(
    points: List<UiPoint>,
    self: Pair<Double, Double>,
    modifier: Modifier = Modifier,
    onSelect: (UiPoint) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseT by pulse.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulseT"
    )

    Canvas(modifier = modifier.pointerInput(points) {
        detectTapGestures { offset ->
            // szukamy najbliższego punktu w promieniu 30 px
            var best: Pair<Float, UiPoint>? = null
            for (p in points) {
                val (x, y) = project(p.lat, p.lng, points, size.width.toFloat(), size.height.toFloat(), self)
                val d = kotlin.math.hypot(x - offset.x, y - offset.y)
                if (d < 60f && (best == null || d < best!!.first)) best = d to p
            }
            best?.second?.let { onSelect(it) }
        }
    }) {
        val w = size.width
        val h = size.height

        // tło + siatka
        drawRect(Color(0xFFF4F5F0))
        val gridStep = 34f
        var gx = 0f
        while (gx <= w) {
            drawLine(Color(0xFFDDE0DA), Offset(gx, 0f), Offset(gx, h), 1f)
            gx += gridStep
        }
        var gy = 0f
        while (gy <= h) {
            drawLine(Color(0xFFDDE0DA), Offset(0f, gy), Offset(w, gy), 1f)
            gy += gridStep
        }
        // ramka bordo (styl „planu” patriotycznego)
        drawRect(WatahaColors.DarkRed.copy(0.7f), topLeft = Offset(0f, 0f), size = size, style = Stroke(width = 3f))

        points.forEach { p ->
            val (x, y) = project(p.lat, p.lng, points, w, h, self)
            if (p.kind == "SELF") {
                val r = 10f * pulseT + 6f
                drawCircle(WatahaColors.Blue.copy(alpha = 0.25f), radius = r + 8f, center = Offset(x, y))
                drawCircle(WatahaColors.Blue, 9f, Offset(x, y))
                drawCircle(Color.White, 4f, Offset(x, y))
            } else {
                drawCircle(Color.White, 15f, Offset(x, y))
                drawCircle(p.color, 12f, Offset(x, y))
                drawText(
                    textMeasurer = textMeasurer,
                    text = p.emoji,
                    topLeft = Offset(x - 7f, y - 8f),
                    style = TextStyle(fontSize = 11.sp)
                )
            }
        }

    }
}

private fun project(
    lat: Double, lng: Double,
    points: List<UiPoint>,
    w: Float, h: Float,
    self: Pair<Double, Double>
): Pair<Float, Float> {
    val all = points.map { it.lat to it.lng }
    val lats = all.map { it.first } + self.first
    val lngs = all.map { it.second } + self.second
    val minLat = lats.min() - 0.012
    val maxLat = lats.max() + 0.012
    val minLng = lngs.min() - 0.012
    val maxLng = lngs.max() + 0.012
    val pad = 26f
    val x = pad + ((lng - minLng) / (maxLng - minLng)).toFloat() * (w - 2 * pad)
    val y = pad + (1f - ((lat - minLat) / (maxLat - minLat)).toFloat()) * (h - 2 * pad)
    return x to y
}
