package com.pesky.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager worker that clears the clipboard.
 * This runs even if the app is closed.
 */
class ClipboardClearWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        
        // Clear clipboard
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            clipboard?.clearPrimaryClip()
        } else {
            clipboard?.setPrimaryClip(ClipData.newPlainText("", ""))
        }
        
        return Result.success()
    }
    
    companion object {
        const val WORK_NAME = "clipboard_clear_work"
    }
}
