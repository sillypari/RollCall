package com.simpleattendance.util

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.simpleattendance.ui.components.LaunchOrigin

/**
 * Bridges the current Activity/Compose hybrid navigation to an origin-aware
 * scale transition. It preserves spatial continuity until navigation moves to
 * a single Compose host with true shared-element transitions.
 */
object HeroTransitionLauncher {

    fun start(
        activity: Activity,
        intent: Intent,
        origin: LaunchOrigin
    ) {
        if (!origin.isValid) {
            activity.startActivity(intent)
            return
        }

        val options = ActivityOptionsCompat.makeScaleUpAnimation(
            activity.window.decorView,
            origin.left,
            origin.top,
            origin.width,
            origin.height
        )
        ActivityCompat.startActivity(activity, intent, options.toBundle())
    }

    fun start(
        activity: Activity,
        intent: Intent,
        sourceView: View
    ) {
        val location = IntArray(2)
        sourceView.getLocationInWindow(location)
        start(
            activity = activity,
            intent = intent,
            origin = LaunchOrigin(
                left = location[0],
                top = location[1],
                width = sourceView.width,
                height = sourceView.height
            )
        )
    }
}
