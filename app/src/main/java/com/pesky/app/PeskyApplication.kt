package com.pesky.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for Pesky Password Manager.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class PeskyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize any app-level components here
        // Security Provider initialization for crypto operations
        initializeSecurityProvider()
    }
    
    /**
     * Initialize Bouncy Castle as the security provider for Argon2.
     */
    private fun initializeSecurityProvider() {
        try {
            java.security.Security.addProvider(
                org.bouncycastle.jce.provider.BouncyCastleProvider()
            )
        } catch (e: Exception) {
            // Provider already added or not available
        }
    }
}
