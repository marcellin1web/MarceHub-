package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.drawText

// Premium MarceHub Linear Gradients
val PremiumDarkGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F172A), // Deep Slate Dark
        Color(0xFF1E1B4B), // Indigo Dark
        Color(0xFF020617)  // Obsidian Black
    )
)

val PremiumLightGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF8FAFC), // Off-white
        Color(0xFFEEF2F6), // Cool Grey
        Color(0xFFE2E8F0)  // Slate Light
    )
)

val AccentGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF6366F1), // Indigo Light
        Color(0xFFEC4899), // Pink Accent
        Color(0xFF8B5CF6)  // Purple Highlight
    )
)

@Composable
fun RotatingWatermark(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "watermark")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val textMeasurer = rememberTextMeasurer()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val textColor = if (isDark) Color.White.copy(alpha = 0.02f) else Color.Black.copy(alpha = 0.025f)
    
    val textStyle = TextStyle(
        fontSize = 42.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.SansSerif,
        color = textColor,
        letterSpacing = 8.sp
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val textLayoutResult = textMeasurer.measure("MARCEHUB", textStyle)
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height
        
        rotate(rotation, center) {
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(center.x - textWidth / 2, center.y - textHeight / 2)
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val backgroundColor = if (isDark) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.White.copy(alpha = 0.7f)
    }
    
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

// Extension to check luminance easily
fun Color.luminance(): Float {
    return 0.2126f * red + 0.7152f * green + 0.0722f * blue
}
