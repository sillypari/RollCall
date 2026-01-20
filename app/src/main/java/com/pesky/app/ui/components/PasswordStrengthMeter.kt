package com.pesky.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pesky.app.data.models.PasswordStrength
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.ui.animations.PeskyAnimations
import com.pesky.app.utils.PasswordAnalysisResult

/**
 * Password strength meter with animated bar.
 */
@Composable
fun PasswordStrengthMeter(
    analysisResult: PasswordAnalysisResult,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    showPercentage: Boolean = true,
    showFeedback: Boolean = false
) {
    val targetProgress by animateFloatAsState(
        targetValue = analysisResult.strengthPercentage,
        animationSpec = PeskyAnimations.standard()
    )
    
    val strengthColor by animateColorAsState(
        targetValue = getStrengthColor(analysisResult.strength),
        animationSpec = PeskyAnimations.standard()
    )
    
    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Strength: ${getStrengthLabel(analysisResult.strength)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = strengthColor
                )
                
                if (showPercentage) {
                    Text(
                        text = "${analysisResult.score}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = strengthColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
        }
        
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(PeskyColors.BackgroundTertiary)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(targetProgress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(strengthColor)
            )
        }
        
        // Feedback
        if (showFeedback && analysisResult.feedback.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            analysisResult.feedback.forEach { feedback ->
                Text(
                    text = "• $feedback",
                    style = MaterialTheme.typography.bodySmall,
                    color = PeskyColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Simple password strength bar without labels.
 */
@Composable
fun PasswordStrengthBar(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    val strengthColor by animateColorAsState(
        targetValue = getStrengthColor(strength),
        animationSpec = PeskyAnimations.standard()
    )
    
    val progress by animateFloatAsState(
        targetValue = (strength.score + 1) / 5f,
        animationSpec = PeskyAnimations.standard()
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(PeskyColors.BackgroundTertiary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(2.dp))
                .background(strengthColor)
        )
    }
}

/**
 * Segmented password strength indicator.
 */
@Composable
fun PasswordStrengthSegments(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(5) { index ->
            val isActive = index <= strength.score
            val segmentColor by animateColorAsState(
                targetValue = if (isActive) getStrengthColor(strength) else PeskyColors.BackgroundTertiary,
                animationSpec = PeskyAnimations.standard()
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(segmentColor)
            )
        }
    }
}

private fun getStrengthColor(strength: PasswordStrength): Color {
    return when (strength) {
        PasswordStrength.VERY_WEAK -> PeskyColors.StrengthVeryWeak
        PasswordStrength.WEAK -> PeskyColors.StrengthWeak
        PasswordStrength.FAIR -> PeskyColors.StrengthFair
        PasswordStrength.STRONG -> PeskyColors.StrengthStrong
        PasswordStrength.VERY_STRONG -> PeskyColors.StrengthVeryStrong
    }
}

private fun getStrengthLabel(strength: PasswordStrength): String {
    return when (strength) {
        PasswordStrength.VERY_WEAK -> "Very Weak"
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.FAIR -> "Fair"
        PasswordStrength.STRONG -> "Strong"
        PasswordStrength.VERY_STRONG -> "Very Strong"
    }
}
