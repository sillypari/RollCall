package com.pesky.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.datastore.preferences.core.intPreferencesKey
import com.pesky.app.data.preferences.peskyDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages secure clipboard operations with auto-clear functionality.
 */
@Singleton
class SecureClipboardManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val clipboard: ClipboardManager? = 
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    
    private val handler = Handler(Looper.getMainLooper())
    private var clearRunnable: Runnable? = null
    
    companion object {
        const val DEFAULT_TIMEOUT_SECONDS = 30
        private val CLIPBOARD_CLEAR_TIMEOUT = intPreferencesKey("clipboard_clear_timeout")
    }
    
    /**
     * Gets the clipboard clear timeout from settings.
     */
    private fun getTimeoutFromSettings(): Int {
        return try {
            runBlocking {
                context.peskyDataStore.data.map { prefs ->
                    prefs[CLIPBOARD_CLEAR_TIMEOUT] ?: DEFAULT_TIMEOUT_SECONDS
                }.first()
            }
        } catch (e: Exception) {
            DEFAULT_TIMEOUT_SECONDS
        }
    }
    
    /**
     * Copies text to clipboard with automatic clearing after timeout.
     * 
     * @param text The text to copy
     * @param label A label for the clipboard content (shown in some Android versions)
     * @param timeoutSeconds Seconds before auto-clear (null = use settings, default 30)
     * @param isSensitive Whether this is sensitive data (affects clear behavior)
     */
    fun copyWithTimeout(
        text: String,
        label: String = "Copied text",
        timeoutSeconds: Int? = null,
        isSensitive: Boolean = true
    ) {
        // Cancel any pending clear operation
        clearRunnable?.let { handler.removeCallbacks(it) }
        
        // Copy to clipboard
        val clip = ClipData.newPlainText(label, text)
        
        // On Android 13+, mark as sensitive to prevent showing in clipboard preview
        if (isSensitive && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = android.os.PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
        
        clipboard?.setPrimaryClip(clip)
        
        // Get timeout from settings if not specified
        val actualTimeout = timeoutSeconds ?: getTimeoutFromSettings()
        
        // Schedule clear
        if (actualTimeout > 0) {
            clearRunnable = Runnable { clearClipboard() }
            handler.postDelayed(clearRunnable!!, actualTimeout * 1000L)
        }
    }
    
    /**
     * Immediately clears the clipboard.
     */
    fun clearClipboard() {
        clearRunnable?.let { handler.removeCallbacks(it) }
        clearRunnable = null
        
        // Clear clipboard by setting empty content
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            clipboard?.clearPrimaryClip()
        } else {
            clipboard?.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }
    
    /**
     * Checks if the current clipboard content matches the given text.
     */
    fun hasClipboardContent(text: String): Boolean {
        return try {
            clipboard?.primaryClip?.getItemAt(0)?.text?.toString() == text
        } catch (e: Exception) {
            false
        }
    }
}
