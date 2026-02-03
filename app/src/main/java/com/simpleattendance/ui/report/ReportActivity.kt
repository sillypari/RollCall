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
            shareReport()
        }
        
        binding.copyButton.setOnClickListener {
            hapticUtils.lightTap()
            copyReport()
        }
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
                    binding.percentageText.text = String.format("%.1f%%", state.percentage)
                    
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
