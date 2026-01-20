package com.pesky.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
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
    }
    
    /**
     * Copies text to clipboard with automatic clearing after timeout.
     * 
     * @param text The text to copy
     * @param label A label for the clipboard content (shown in some Android versions)
     * @param timeoutSeconds Seconds before auto-clear (default 30)
     * @param isSensitive Whether this is sensitive data (affects clear behavior)
     */
    fun copyWithTimeout(
        text: String,
        label: String = "Copied text",
        timeoutSeconds: Int = DEFAULT_TIMEOUT_SECONDS,
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
        
        // Schedule clear
        if (timeoutSeconds > 0) {
            clearRunnable = Runnable { clearClipboard() }
            handler.postDelayed(clearRunnable!!, timeoutSeconds * 1000L)
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
