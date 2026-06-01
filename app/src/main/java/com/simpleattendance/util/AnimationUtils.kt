package com.simpleattendance.util

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import com.simpleattendance.R

object AnimationUtils {
    
    fun scaleIn(view: View) {
        view.alpha = 0f
        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()
    }
    
    fun scaleOut(view: View, onEnd: () -> Unit = {}) {
        view.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(200)
            .withEndAction(onEnd)
            .start()
    }
    
    fun slideInFromRight(view: View) {
        view.translationX = view.width.toFloat()
        view.alpha = 0f
        view.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(300)
            .start()
    }
    
    fun slideInFromLeft(view: View) {
        view.translationX = -view.width.toFloat()
        view.alpha = 0f
        view.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(300)
            .start()
    }
    
    fun fadeIn(view: View, duration: Long = 300) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .start()
    }
    
    fun fadeOut(view: View, duration: Long = 300, onEnd: () -> Unit = {}) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                view.visibility = View.GONE
                onEnd()
            }
            .start()
    }
    
    fun pulse(view: View) {
        view.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
    
    fun shake(view: View) {
        val shakeDistance = 10f
        view.animate()
            .translationX(shakeDistance)
            .setDuration(50)
            .withEndAction {
                view.animate()
                    .translationX(-shakeDistance)
                    .setDuration(50)
                    .withEndAction {
                        view.animate()
                            .translationX(0f)
                            .setDuration(50)
                            .start()
                    }
                    .start()
            }
            .start()
    }
    
    fun startPulsing(view: View) {
        stopPulsing(view)
        
        val scaleX = android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.08f)
        val scaleY = android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.08f)
        
        val animator = android.animation.ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).apply {
            duration = 800
            repeatCount = android.animation.ValueAnimator.INFINITE
            repeatMode = android.animation.ValueAnimator.REVERSE
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        }
        view.tag = animator
        animator.start()
    }
    
    fun stopPulsing(view: View) {
        (view.tag as? android.animation.Animator)?.cancel()
        view.tag = null
        view.animate().cancel()
        view.scaleX = 1f
        view.scaleY = 1f
    }

    fun applySpringScale(view: View) {
        try {
            val springX = androidx.dynamicanimation.animation.SpringAnimation(view, androidx.dynamicanimation.animation.DynamicAnimation.SCALE_X)
            val springY = androidx.dynamicanimation.animation.SpringAnimation(view, androidx.dynamicanimation.animation.DynamicAnimation.SCALE_Y)
            
            springX.spring = androidx.dynamicanimation.animation.SpringForce(1f).apply {
                dampingRatio = androidx.dynamicanimation.animation.SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                stiffness = androidx.dynamicanimation.animation.SpringForce.STIFFNESS_MEDIUM
            }
            springY.spring = androidx.dynamicanimation.animation.SpringForce(1f).apply {
                dampingRatio = androidx.dynamicanimation.animation.SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                stiffness = androidx.dynamicanimation.animation.SpringForce.STIFFNESS_MEDIUM
            }
            
            view.scaleX = 0.92f
            view.scaleY = 0.92f
            
            springX.start()
            springY.start()
        } catch (e: Exception) {
            // Fallback to standard scale animations if dependencies fail to bind at runtime
            view.scaleX = 0.95f
            view.scaleY = 0.95f
            view.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
        }
    }
}
