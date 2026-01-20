package com.pesky.app.utils

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper for biometric authentication (fingerprint, face, etc.)
 */
@Singleton
class BiometricHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val biometricManager = BiometricManager.from(context)
    
    /**
     * Checks if biometric authentication is available on this device.
     */
    fun isBiometricAvailable(): BiometricAvailability {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SecurityUpdateRequired
            else -> BiometricAvailability.Unknown
        }
    }
    
    /**
     * Shows the biometric authentication prompt.
     * 
     * @param activity The activity context
     * @param title The title shown in the prompt
     * @param subtitle Optional subtitle
     * @param negativeButtonText Text for the negative button (e.g., "Use Password")
     * @param callback Callback for authentication result
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Pesky",
        subtitle: String? = "Use fingerprint to unlock vault",
        negativeButtonText: String = "Use Password",
        callback: BiometricAuthCallback
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback.onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED -> callback.onCancel()
                        else -> callback.onError(errString.toString())
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback.onFailure()
                }
            })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply { subtitle?.let { setSubtitle(it) } }
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Gets a description of biometric type available on device.
     */
    fun getBiometricType(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when {
                biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) 
                    == BiometricManager.BIOMETRIC_SUCCESS -> "Biometric"
                else -> "Biometric"
            }
        } else {
            "Fingerprint"
        }
    }
}

/**
 * Biometric availability states.
 */
enum class BiometricAvailability {
    Available,
    NoHardware,
    HardwareUnavailable,
    NoneEnrolled,
    SecurityUpdateRequired,
    Unknown
}

/**
 * Callback interface for biometric authentication.
 */
interface BiometricAuthCallback {
    fun onSuccess()
    fun onFailure()
    fun onError(message: String)
    fun onCancel()
}
