package com.simpleattendance.ui.createclass

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
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
        strokeWidth = 4f // Thin line (2dp)
        color = Color.WHITE // Clean white color
    }

    private var animator: ValueAnimator? = null
    private val rectF = RectF()
    private var cornerRadius = 0f

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = paint.strokeWidth / 2f
        rectF.set(inset, inset, w - inset, h - inset)
        
        // Dynamically set corner radius to half of height to make it a perfect pill shape
        cornerRadius = h / 2f
    }

    fun startAnimation() {
        if (animator != null) return
        // The sum of dash length (16f) and gap length (12f) is 28f
        animator = ValueAnimator.ofFloat(28f, 0f).apply {
            duration = 1000 // Speed of the rotating dashes
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val phase = animation.animatedValue as Float
                paint.pathEffect = DashPathEffect(floatArrayOf(16f, 12f), phase)
                invalidate()
            }
            start()
        }
    }

    fun stopAnimation() {
        animator?.cancel()
        animator = null
        paint.pathEffect = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }
}
