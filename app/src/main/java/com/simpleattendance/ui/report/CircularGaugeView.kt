package com.simpleattendance.ui.report

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.simpleattendance.R

class CircularGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Float = 0f
    private var animatedProgress: Float = 0f
    
    private val strokeWidthSize = 24f
    private val padding = 30f
    
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthSize
        strokeCap = Paint.Cap.ROUND
    }
    
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthSize
        strokeCap = Paint.Cap.ROUND
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    
    private val rectF = RectF()
    
    init {
        trackPaint.color = ContextCompat.getColor(context, R.color.background_tertiary)
        
        // Load custom Outfit font for percentage text if available, fallback to bold system
        val customFont = try {
            ResourcesCompat.getFont(context, R.font.outfit)
        } catch (e: Exception) {
            Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        textPaint.typeface = customFont
        textPaint.color = ContextCompat.getColor(context, R.color.text_primary)
    }
    
    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 100f)
        animateProgress()
    }
    
    private fun animateProgress() {
        val animator = ValueAnimator.ofFloat(0f, progress).apply {
            duration = 1200
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                animatedProgress = animation.animatedValue as Float
                invalidate()
            }
        }
        animator.start()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = Math.min(w, h).toFloat()
        rectF.set(
            padding + strokeWidthSize / 2,
            padding + strokeWidthSize / 2,
            size - padding - strokeWidthSize / 2,
            size - padding - strokeWidthSize / 2
        )
        textPaint.textSize = size * 0.22f
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw track circle (full 360 degrees)
        canvas.drawArc(rectF, 0f, 360f, false, trackPaint)
        
        // Dynamic arc color based on progress tier
        progressPaint.color = when {
            animatedProgress >= 75f -> ContextCompat.getColor(context, R.color.success_green)
            animatedProgress >= 50f -> ContextCompat.getColor(context, R.color.warning_yellow)
            else -> ContextCompat.getColor(context, R.color.error_red)
        }
        
        // Draw progress sweep arc (starts from top -90deg)
        val sweepAngle = (animatedProgress / 100f) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)
        
        // Draw centered percentage text
        val textY = (height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(String.format("%.0f%%", animatedProgress), (width / 2).toFloat(), textY, textPaint)
    }
}
