package com.simpleattendance.ui.createclass

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class AnimatedBorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f // Width of the snake outline
    }

    private var animator: ValueAnimator? = null
    private var rotationAngle = 0f
    private val sweepGradientColors = intArrayOf(
        Color.parseColor("#76B900"), // Nvidia green
        Color.TRANSPARENT,
        Color.TRANSPARENT,
        Color.parseColor("#76B900")
    )
    private val sweepGradientPositions = floatArrayOf(0.0f, 0.3f, 0.7f, 1.0f)
    private var sweepGradient: SweepGradient? = null
    private val rectF = RectF()
    private val gradientMatrix = Matrix()

    // Corner radius of the rounded border (matches Material3 button shape)
    private var cornerRadius = 16f * resources.displayMetrics.density

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = paint.strokeWidth / 2f
        rectF.set(inset, inset, w - inset, h - inset)
        
        sweepGradient = SweepGradient(w / 2f, h / 2f, sweepGradientColors, sweepGradientPositions)
        paint.shader = sweepGradient
    }

    fun startAnimation() {
        if (animator != null) return
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1800 // Duration for one full loop
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                rotationAngle = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopAnimation() {
        animator?.cancel()
        animator = null
        rotationAngle = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        sweepGradient?.let { shader ->
            gradientMatrix.reset()
            gradientMatrix.postRotate(rotationAngle, width / 2f, height / 2f)
            shader.setLocalMatrix(gradientMatrix)
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
        }
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }
}
