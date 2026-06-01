package com.simpleattendance.ui.report

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simpleattendance.databinding.ActivityReportBinding
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReportActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReportBinding
    private val viewModel: ReportViewModel by viewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    private var fromHistory = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        fromHistory = intent.getBooleanExtra("fromHistory", false)
        
        setupToolbar()
        setupButtons()
        observeState()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            hapticUtils.lightTap()
            handleBack()
        }
    }
    
    private fun setupButtons() {
        binding.shareButton.setOnClickListener {
            hapticUtils.mediumImpact()
            showShareOptions()
        }
        
        binding.copyButton.setOnClickListener {
            hapticUtils.lightTap()
            copyReport()
        }
    }
    
    private fun showShareOptions() {
        val options = arrayOf("Share as Beautiful Image (PNG)", "Share as Plain Text")
        MaterialAlertDialogBuilder(this)
            .setTitle("Share Attendance Report")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareReportAsImage()
                    1 -> shareReport()
                }
            }
            .show()
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading) {
                        return@collect
                    }
                    
                    // Class info
                    binding.classNameText.text = state.classDisplayName
                    binding.dateText.text = state.formattedDate
                    
                    // Stats
                    binding.presentCountText.text = state.presentCount.toString()
                    binding.absentCountText.text = state.absentCount.toString()
                    
                    // Circular Gauge View Progress
                    binding.circularGauge.setProgress(state.percentage)
                    
                    // Report text
                    binding.reportTextView.text = state.reportText
                }
            }
        }
    }
    
    private fun shareReport() {
        val state = viewModel.uiState.value
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Attendance Report - ${state.classDisplayName}")
            putExtra(Intent.EXTRA_TEXT, state.reportText)
        }
        startActivity(Intent.createChooser(intent, "Share Report"))
    }
    
    private fun shareReportAsImage() {
        try {
            // Capture the Circular Gauge Card itself (card holding gauge view)
            val targetCard = binding.circularGauge.parent.parent as View
            
            val bitmap = android.graphics.Bitmap.createBitmap(
                targetCard.width,
                targetCard.height,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            targetCard.draw(canvas)
            
            // Save to cache path
            val cachePath = java.io.File(cacheDir, "images").apply { mkdirs() }
            val file = java.io.File(cachePath, "attendance_report.png")
            val stream = java.io.FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Report Card"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to plain text
            shareReport()
        }
    }
    
    private fun copyReport() {
        val state = viewModel.uiState.value
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Attendance Report", state.reportText)
        clipboard.setPrimaryClip(clip)
        hapticUtils.successPattern()
        Toast.makeText(this, "Report copied to clipboard", Toast.LENGTH_SHORT).show()
    }
    
    private fun handleBack() {
        if (fromHistory) {
            // Just finish to go back to history fragment
            finish()
        } else {
            navigateToMain()
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, com.simpleattendance.ui.main.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleBack()
    }
}
