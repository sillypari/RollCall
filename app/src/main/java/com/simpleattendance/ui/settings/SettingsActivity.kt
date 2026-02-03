package com.simpleattendance.ui.settings

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.simpleattendance.R
import com.simpleattendance.databinding.ActivitySettingsBinding
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupTemplateOptions()
        setupNumberingOptions()
        setupHapticsToggle()
        observeState()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            hapticUtils.lightTap()
            finish()
        }
    }
    
    private fun setupTemplateOptions() {
        binding.templateRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            hapticUtils.lightTap()
            val template = when (checkedId) {
                R.id.templateAbsentOnly -> "absent_only"
                R.id.templatePresentOnly -> "present_only"
                else -> "both"
            }
            viewModel.setReportTemplate(template)
        }
    }
    
    private fun setupNumberingOptions() {
        binding.numberingRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            hapticUtils.lightTap()
            val mode = when (checkedId) {
                R.id.numberingRelative -> "relative"
                else -> "absolute"
            }
            viewModel.setNumberingMode(mode)
        }
    }
    
    private fun setupHapticsToggle() {
        binding.hapticsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) hapticUtils.lightTap()
            viewModel.setHapticsEnabled(isChecked)
        }
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { settings ->
                    // Report Template
                    val templateId = when (settings.reportTemplate) {
                        "absent_only" -> R.id.templateAbsentOnly
                        "present_only" -> R.id.templatePresentOnly
                        else -> R.id.templateBoth
                    }
                    if (binding.templateRadioGroup.checkedRadioButtonId != templateId) {
                        binding.templateRadioGroup.check(templateId)
                    }
                    
                    // Numbering Mode
                    val numberingId = when (settings.numberingMode) {
                        "relative" -> R.id.numberingRelative
                        else -> R.id.numberingAbsolute
                    }
                    if (binding.numberingRadioGroup.checkedRadioButtonId != numberingId) {
                        binding.numberingRadioGroup.check(numberingId)
                    }
                    
                    // Haptics
                    binding.hapticsSwitch.isChecked = settings.hapticsEnabled
                }
            }
        }
    }
}
