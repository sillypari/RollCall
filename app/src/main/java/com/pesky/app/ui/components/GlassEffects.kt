package com.pesky.app.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pesky.app.ui.theme.PeskyColors

/**
 * Glass morphism modifiers for Apple Maps-style frosted glass effects.
 */
object GlassModifiers {
    
    /**
     * Apply a frosted glass background effect.
     * Falls back to semi-transparent background on older devices.
     */
    @Composable
    fun Modifier.glassBackground(
        color: Color = PeskyColors.GlassBackground,
        cornerRadius: Dp = 16.dp,
        borderColor: Color = PeskyColors.GlassBorder
    ): Modifier = this
        .clip(RoundedCornerShape(cornerRadius))
        .background(color)
        .drawWithContent {
            drawContent()
            // Draw subtle border for glass effect
            drawRoundRect(
                color = borderColor,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    cornerRadius.toPx(), 
                    cornerRadius.toPx()
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }
    
    /**
     * Floating card modifier with shadow and elevation.
     */
    fun Modifier.floatingCard(
        elevation: Dp = 8.dp,
        cornerRadius: Dp = 16.dp
    ): Modifier = this
        .shadow(
            elevation = elevation,
            shape = RoundedCornerShape(cornerRadius)
        )
        .clip(RoundedCornerShape(cornerRadius))
        .background(PeskyColors.CardBackground)
}

/**
 * Shimmer loading effect for skeleton screens.
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            PeskyColors.ShimmerBase,
            PeskyColors.ShimmerHighlight,
            PeskyColors.ShimmerBase
        ),
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim + 200f, 0f)
    )
    
    return this.background(shimmerBrush)
}

/**
 * Skeleton loading placeholder.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .shimmerEffect()
    )
}

/**
 * Skeleton card placeholder matching PasswordEntryCard dimensions.
 */
@Composable
fun ShimmerEntryCard(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PeskyColors.CardBackground)
            .padding(16.dp)
    ) {
        // Avatar placeholder
        ShimmerBox(
            modifier = Modifier.size(44.dp),
            cornerRadius = 10.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            // Title placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp),
                cornerRadius = 4.dp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Username placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp),
                cornerRadius = 4.dp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Date placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.25f)
                    .height(10.dp),
                cornerRadius = 4.dp
            )
        }
        
        // Icons placeholder
        Spacer(modifier = Modifier.width(8.dp))
        ShimmerBox(
            modifier = Modifier.size(24.dp),
            cornerRadius = 12.dp
        )
    }
}

/**
 * Loading skeleton for vault screen.
 */
@Composable
fun VaultLoadingSkeleton(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        repeat(itemCount) {
            ShimmerEntryCard()
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
