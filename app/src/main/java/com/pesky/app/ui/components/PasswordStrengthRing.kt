package com.pesky.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.utils.PasswordAnalysisResult

/**
 * Apple Health-style circular password strength indicator.
 */
@Composable
fun PasswordStrengthRing(
    score: Int, // 0-4
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    showPercentage: Boolean = true,
    showLabel: Boolean = true,
    animated: Boolean = true
) {
    val percentage = (score + 1) * 20 // Convert 0-4 to 20-100%
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) percentage / 100f else percentage / 100f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "ring_progress"
    )
    
    val ringColor = when (score) {
        0, 1 -> PeskyColors.StrengthRingWeak
        2 -> PeskyColors.StrengthRingMedium
        else -> PeskyColors.StrengthRingStrong
    }
    
    val label = when (score) {
        0 -> "Very Weak"
        1 -> "Weak"
        2 -> "Fair"
        3 -> "Strong"
        4 -> "Very Strong"
        else -> ""
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = Size(
                this.size.width - strokeWidthPx,
                this.size.height - strokeWidthPx
            )
            val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
            
            // Background ring
            drawArc(
                color = PeskyColors.StrengthRingBackground,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            
            // Progress ring
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        
        // Center content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showPercentage) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PeskyColors.TextPrimary
                )
            }
            
            if (showLabel) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = ringColor
                )
            }
        }
    }
}

/**
 * Compact inline password strength ring.
 */
@Composable
fun PasswordStrengthRingCompact(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    PasswordStrengthRing(
        score = score,
        modifier = modifier,
        size = size,
        strokeWidth = 3.dp,
        showPercentage = false,
        showLabel = false,
        animated = true
    )
}

/**
 * Password strength ring with analysis result.
 */
@Composable
fun PasswordStrengthRingWithLabel(
    analysisResult: PasswordAnalysisResult,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PasswordStrengthRing(
            score = analysisResult.score,
            size = size,
            strokeWidth = 10.dp,
            showPercentage = true,
            showLabel = true
        )
        
        if (analysisResult.feedback.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = analysisResult.feedback.first(),
                style = MaterialTheme.typography.bodySmall,
                color = PeskyColors.TextSecondary
            )
        }
    }
}
