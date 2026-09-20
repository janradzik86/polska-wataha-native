package pl.wataha.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Motyw „Polska Wataha” — biel i czerwień. */
object WatahaColors {
    val White = Color(0xFFFFFFFF)
    val FlagRed = Color(0xFFD4213D)
    val DarkRed = Color(0xFFA81C30)
    val LightRed = Color(0xFFFDE8EC)
    val Ink = Color(0xFF1F2A44)
    val Grey = Color(0xFF6B7280)
    val LightGrey = Color(0xFFF3F4F6)
    val Border = Color(0xFFE5E7EB)
    val Green = Color(0xFF2E7D32)
    val Amber = Color(0xFFF59E0B)
    val Blue = Color(0xFF1D4ED8)
}

private val LightColors = lightColorScheme(
    primary = WatahaColors.FlagRed,
    onPrimary = Color.White,
    primaryContainer = WatahaColors.LightRed,
    onPrimaryContainer = WatahaColors.DarkRed,
    secondary = WatahaColors.Ink,
    onSecondary = Color.White,
    background = WatahaColors.White,
    onBackground = WatahaColors.Ink,
    surface = WatahaColors.White,
    onSurface = WatahaColors.Ink,
    surfaceVariant = WatahaColors.LightGrey,
    onSurfaceVariant = WatahaColors.Grey,
    error = WatahaColors.FlagRed
)

@Composable
fun WatahaTheme(content: @Composable () -> Unit) {
    // Celowo zawsze jasny motyw (czytelność w terenie + oszczędność energii)
    MaterialTheme(
        colorScheme = LightColors,
        typography = WatahaTypography,
        content = content
    )
}
