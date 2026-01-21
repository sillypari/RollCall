package com.pesky.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.pesky.app.data.preferences.peskyDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.pesky.app.navigation.PeskyNavHost
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.ui.theme.PeskyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Main activity for Pesky Password Manager.
 * Uses FLAG_SECURE to prevent screenshots for security (configurable in settings).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var keepSplashScreen = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen().setKeepOnScreenCondition { keepSplashScreen }
        
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Check screenshot protection setting (default: enabled/blocked)
        val screenshotProtectionKey = booleanPreferencesKey("screenshot_protection_enabled")
        val isScreenshotProtectionEnabled = runBlocking {
            peskyDataStore.data.map { prefs -> 
                prefs[screenshotProtectionKey] ?: true  // Default: true (blocked)
            }.first()
        }
        
        // Apply FLAG_SECURE based on setting
        if (isScreenshotProtectionEnabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        
        // Remove splash screen after a short delay
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay(500)
                keepSplashScreen = false
            }
        }
        
        setContent {
            PeskyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PeskyColors.BackgroundPrimary
                ) {
                    val navController = rememberNavController()
                    
                    PeskyNavHost(navController = navController)
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Additional security: blur screen in recent apps
        // This is handled by FLAG_SECURE above
    }
    
    override fun onStop() {
        super.onStop()
        // Could trigger auto-lock timer here
    }
}
