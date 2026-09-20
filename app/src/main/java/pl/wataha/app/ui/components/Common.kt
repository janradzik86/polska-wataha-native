package pl.wataha.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.wataha.app.ui.theme.WatahaColors
import java.util.concurrent.TimeUnit

/** Logo — Kotwica Polski Walczącej (symbol „P” + kotwica). */
@Composable
fun KotwicaLogo(size: Dp, color: Color = WatahaColors.FlagRed) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val stroke = w * 0.07f
        // pierścień
        drawCircle(color = color, radius = w * 0.16f, center = Offset(cx, h * 0.16f), style = Stroke(width = stroke))
        // trzon
        drawLine(color, Offset(cx, h * 0.28f), Offset(cx, h * 0.80f), strokeWidth = stroke, cap = StrokeCap.Round)
        // poprzeczka (P)
        drawLine(color, Offset(cx - w * 0.20f, h * 0.50f), Offset(cx + w * 0.20f, h * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
        // ramiona
        val arm = Path().apply {
            moveTo(cx, h * 0.72f)
            cubicTo(cx - w * 0.28f, h * 0.82f, cx - w * 0.34f, h * 0.80f, cx - w * 0.30f, h * 0.95f)
        }
        drawPath(arm, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
        val armR = Path().apply {
            moveTo(cx, h * 0.72f)
            cubicTo(cx + w * 0.28f, h * 0.82f, cx + w * 0.34f, h * 0.80f, cx + w * 0.30f, h * 0.95f)
        }
        drawPath(armR, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

/** Pasek w barwach flagi — biel u góry, czerwień pod spodem. */
@Composable
fun FlagStripe(modifier: Modifier = Modifier, height: Dp = 6.dp) {
    Column(modifier = modifier) {
        Box(Modifier.fillMaxWidth().height(height / 2).background(Color.White))
        Box(Modifier.fillMaxWidth().height(height / 2).background(WatahaColors.FlagRed))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatahaTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    crisisMode: Boolean = false
) {
    Column {
        TopAppBar(
            title = {
                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = if (crisisMode) Color.White else WatahaColors.Ink)
                    if (subtitle != null) {
                        Text(subtitle, fontSize = 11.sp, color = if (crisisMode) Color.White.copy(0.85f) else WatahaColors.Grey)
                    }
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz",
                            tint = if (crisisMode) Color.White else WatahaColors.Ink)
                    }
                }
            },
            actions = { action?.invoke() },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (crisisMode) WatahaColors.FlagRed else WatahaColors.White,
                titleContentColor = if (crisisMode) Color.White else WatahaColors.Ink
            )
        )
        FlagStripe()
    }
}

@Composable
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(text, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WatahaColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = WatahaColors.Ink)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, text: String) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 38.sp)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = WatahaColors.Grey, textAlign = TextAlign.Center)
    }
}

@Composable
fun DistLabel(km: Float, modifier: Modifier = Modifier) {
    val txt = if (km < 1) "${(km * 1000).toInt()} m" else "%.1f km".format(km)
    StatusPill(txt, WatahaColors.Blue, modifier)
}

fun timeAgo(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "przed chwilą"
        diff < TimeUnit.HOURS.toMillis(1) -> "przed ${diff / 60_000} min"
        diff < TimeUnit.DAYS.toMillis(1) -> "przed ${diff / 3_600_000} h"
        else -> "przed ${diff / 86_400_000} dn."
    }
}

@Composable
fun Avatar(initials: String, size: Dp = 40.dp, color: Color = WatahaColors.FlagRed) {
    Box(
        Modifier.size(size).background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initials.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.36f).sp)
    }
}
