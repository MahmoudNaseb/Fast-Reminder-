package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

// Celestial Calm Green & Gold Color Palette
val IslamicCream = Color(0xFFF7FBF7)
val IslamicLightGreen = Color(0xFFE9F3EA)
val IslamicCalmGreen = Color(0xFF2E6F40)
val IslamicEmerald = Color(0xFF4C9A6C)
val IslamicGold = Color(0xFFD4AF37)
val IslamicGoldSoft = Color(0x33D4AF37)

@Composable
fun OrnamentalBackground(
    modifier: Modifier = Modifier,
    showStars: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(IslamicCream, IslamicLightGreen)
                )
            )
    ) {
        // Draw elegant Islamic geometry behind the content
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw elegant gold frame border
            val padding = 24f
            drawRect(
                color = IslamicGold,
                topLeft = Offset(padding, padding),
                size = Size(width - padding * 2, height - padding * 2),
                style = Stroke(width = 2f)
            )

            // 2. Draw outer thin cream-green border
            drawRect(
                color = IslamicCalmGreen.copy(alpha = 0.4f),
                topLeft = Offset(padding + 8f, padding + 8f),
                size = Size(width - (padding + 8f) * 2, height - (padding + 8f) * 2),
                style = Stroke(width = 1f)
            )

            // 3. Draw Islamic Mihrab (Arch) Silhouette at the top
            val path = Path().apply {
                val topMargin = padding + 16f
                val left = padding + 16f
                val right = width - padding - 16f
                val bottom = height - padding - 16f
                
                moveTo(left, bottom)
                lineTo(left, height * 0.3f)
                
                // Arch curves meeting in an elegant Islamic cusp/point
                cubicTo(
                    left, height * 0.18f,
                    width * 0.35f, topMargin + 30f,
                    width * 0.5f, topMargin
                )
                cubicTo(
                    width * 0.65f, topMargin + 30f,
                    right, height * 0.18f,
                    right, height * 0.3f
                )
                lineTo(right, bottom)
            }
            
            drawPath(
                path = path,
                color = IslamicCalmGreen.copy(alpha = 0.05f),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            
            drawPath(
                path = path,
                color = IslamicCalmGreen.copy(alpha = 0.15f),
                style = Stroke(width = 1.5f)
            )

            // 4. Draw elegant 8-pointed star in the top corners as decorative ornament
            if (showStars) {
                drawIslamicStar(Offset(width * 0.15f, height * 0.15f), radius = 22f)
                drawIslamicStar(Offset(width * 0.85f, height * 0.15f), radius = 22f)
                
                drawIslamicStar(Offset(width * 0.5f, height * 0.04f), radius = 16f)

                // Bottom corners
                drawIslamicStar(Offset(padding + 30f, height - padding - 30f), radius = 15f)
                drawIslamicStar(Offset(width - padding - 30f, height - padding - 30f), radius = 15f)
            }
        }
        
        content()
    }
}

// Extension to Draw 8-pointed Islamic geometric star (Rub el Hizb)
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawIslamicStar(center: Offset, radius: Float) {
    val path1 = Path()
    val path2 = Path()

    // First square
    path1.moveTo(center.x + radius, center.y)
    for (i in 1..4) {
        val angle = i * Math.PI / 2.0
        path1.lineTo(
            (center.x + radius * cos(angle)).toFloat(),
            (center.y + radius * sin(angle)).toFloat()
        )
    }
    path1.close()

    // Second square rotated by 45 degrees
    val offsetAngle = Math.PI / 4.0
    path2.moveTo(
        (center.x + radius * cos(offsetAngle)).toFloat(),
        (center.y + radius * sin(offsetAngle)).toFloat()
    )
    for (i in 1..4) {
        val angle = offsetAngle + i * Math.PI / 2.0
        path2.lineTo(
            (center.x + radius * cos(angle)).toFloat(),
            (center.y + radius * sin(angle)).toFloat()
        )
    }
    path2.close()

    // Draw paths
    drawPath(path = path1, color = IslamicGoldSoft, style = androidx.compose.ui.graphics.drawscope.Fill)
    drawPath(path = path1, color = IslamicGold, style = Stroke(width = 1.5f))
    
    drawPath(path = path2, color = IslamicGoldSoft, style = androidx.compose.ui.graphics.drawscope.Fill)
    drawPath(path = path2, color = IslamicGold, style = Stroke(width = 1.5f))

    // Inner circle
    drawCircle(
        color = IslamicCalmGreen.copy(alpha = 0.3f),
        radius = radius * 0.4f,
        center = center
    )
}
