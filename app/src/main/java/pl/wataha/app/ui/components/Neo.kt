package pl.wataha.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.wataha.app.ui.theme.WatahaColors

/**
 * Nowoczesne komponenty 3D: gradientowe przyciski „wypukłe”, szklane karty (glassmorphism),
 * hero z fotografią i nakładką. Wszystko z animacjami dotyku (skala + cień + wibracja).
 */

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(WatahaColors.FlagRed, WatahaColors.DarkRed),
    icon: String? = null,
    fontSize: Int = 15,
    height: Dp = 52.dp,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, animationSpec = tween(120), label = "btn-scale")
    val elevation by animateFloatAsState(if (pressed) 3.dp.value else 10.dp.value, animationSpec = tween(120), label = "btn-elev")
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = if (enabled) scale else 1f
                scaleY = if (enabled) scale else 1f
            }
            .shadow(elevation.dp, shape, ambientColor = WatahaColors.Ink.copy(alpha = 0.35f))
            .clip(shape)
            .background(Brush.linearGradient(colors))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Text(icon, fontSize = (fontSize + 3).sp)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text,
                color = Color.White,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        // połysk 3D (subtelny gradient na górze)
        Box(
            Modifier
                .fillMaxWidth()
                .height((height.value / 2.2f).dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        )
    }
}

/** Szklana karta — glassmorphism: półprzezroczystość, gradientowa ramka, głęboki cień. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, animationSpec = tween(120), label = "glass-scale")
    val shape = RoundedCornerShape(18.dp)

    Card(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(12.dp, shape, ambientColor = WatahaColors.Ink.copy(alpha = 0.30f), spotColor = WatahaColors.DarkRed.copy(alpha = 0.18f)),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.87f)),
        border = BorderStroke(
            1.dp,
            Brush.verticalGradient(listOf(Color.White.copy(0.95f), WatahaColors.FlagRed.copy(0.28f)))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            Modifier.let { m ->
                if (onClick != null)
                    m.clickable(interactionSource = interaction, indication = null) { onClick() }
                else m
            }
        ) {
            content()
        }
    }
}

/** Płytka 3D z emoji na gradiencie (ikony kategorii / miniaturki). */
@Composable
fun EmojiTile(emoji: String, size: Dp = 56.dp, gradient: List<Color> = listOf(WatahaColors.FlagRed, WatahaColors.DarkRed), fontSize: Int = 26) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier
            .size(size)
            .shadow(6.dp, shape, ambientColor = WatahaColors.Ink.copy(alpha = 0.3f))
            .clip(shape)
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = fontSize.sp)
    }
}

/** Chip 3D (filtry, statusy) — onClick jako OSTATNI parametr (trailing lambda). */
@Composable
fun NeoChip(
    text: String,
    emoji: String? = null,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, animationSpec = tween(100), label = "chip-scale")
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (selected) 8.dp else 0.dp, shape, ambientColor = WatahaColors.DarkRed.copy(alpha = 0.4f))
            .clip(shape)
            .background(
                if (selected) Brush.linearGradient(listOf(WatahaColors.FlagRed, WatahaColors.DarkRed))
                else Brush.linearGradient(listOf(Color.White, WatahaColors.LightGrey))
            )
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(emoji, fontSize = 13.sp)
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else WatahaColors.Ink
            )
        }
    }
}

/** Sekcja z nagłówkiem w stylu OLX. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(18.dp).clip(RoundedCornerShape(2.dp)).background(WatahaColors.FlagRed))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, color = WatahaColors.Ink, fontWeight = FontWeight.Bold)
    }
}
