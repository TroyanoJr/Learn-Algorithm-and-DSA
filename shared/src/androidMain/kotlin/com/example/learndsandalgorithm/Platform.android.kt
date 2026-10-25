package com.example.learndsandalgorithm.core.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import java.util.WeakHashMap

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun LandscapeOrientationEffect(enabled: Boolean) {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity, enabled) {
        if (enabled && activity != null) {
            LandscapeOrientationRequests.acquire(activity)
        }
        onDispose {
            if (enabled && activity != null) {
                LandscapeOrientationRequests.release(activity)
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        val next = current.baseContext
        if (next === current) return null
        current = next
    }
    return current as? Activity
}

private object LandscapeOrientationRequests {
    private data class RequestState(
        val previousOrientation: Int,
        var requestCount: Int
    )

    private val states = WeakHashMap<Activity, RequestState>()

    fun acquire(activity: Activity) {
        synchronized(states) {
            val state = states[activity]
            if (state == null) {
                states[activity] = RequestState(activity.requestedOrientation, 1)
            } else {
                state.requestCount++
            }
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    fun release(activity: Activity) {
        synchronized(states) {
            val state = states[activity] ?: return
            state.requestCount--
            if (state.requestCount == 0) {
                states.remove(activity)
                if (!activity.isFinishing && !activity.isDestroyed) {
                    activity.requestedOrientation = state.previousOrientation
                }
            }
        }
    }
}
