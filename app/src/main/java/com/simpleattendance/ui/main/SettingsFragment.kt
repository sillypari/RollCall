package com.simpleattendance.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayout
import com.simpleattendance.R
import com.simpleattendance.databinding.FragmentSettingsBinding
import com.simpleattendance.ui.settings.SettingsViewModel
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    // Flag to prevent haptic loops during initial state loading
    private var isInitializing = true
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupSettings()
        observeState()
        
        // Show General tab by default
        showGeneralSettings()
    }
    
    private fun setupTabs() {
        binding.settingsTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                hapticUtils.lightTap()
                when (tab?.position) {
                    0 -> showGeneralSettings()
                    1 -> showReportsSettings()
                    2 -> showAboutSettings()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun showGeneralSettings() {
        binding.generalSettingsContainer.visibility = View.VISIBLE
        binding.reportsSettingsContainer.visibility = View.GONE
        binding.aboutSettingsContainer.visibility = View.GONE
    }
    
    private fun showReportsSettings() {
        binding.generalSettingsContainer.visibility = View.GONE
        binding.reportsSettingsContainer.visibility = View.VISIBLE
        binding.aboutSettingsContainer.visibility = View.GONE
    }
    
    private fun showAboutSettings() {
        binding.generalSettingsContainer.visibility = View.GONE
        binding.reportsSettingsContainer.visibility = View.GONE
        binding.aboutSettingsContainer.visibility = View.VISIBLE
    }
    
    private fun setupSettings() {
        // Dark Mode Switch
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isInitializing) {
                hapticUtils.lightTap()
                viewModel.setTheme(if (isChecked) "dark" else "light")
            }
        }
        
        // Haptic Feedback Switch
        binding.hapticsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isInitializing) {
                if (isChecked) hapticUtils.lightTap()
                viewModel.setHapticsEnabled(isChecked)
            }
        }
        
        // Report Template
        binding.templateRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (!isInitializing) {
                hapticUtils.lightTap()
                val template = when (checkedId) {
                    R.id.templateAbsentOnly -> "absent_only"
                    R.id.templatePresentOnly -> "present_only"
                    else -> "both"
                }
                viewModel.setReportTemplate(template)
            }
        }
        
        // Numbering Mode
        binding.numberingRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (!isInitializing) {
                hapticUtils.lightTap()
                val mode = when (checkedId) {
                    R.id.numberingRelative -> "relative"
                    else -> "absolute"
                }
                viewModel.setNumberingMode(mode)
            }
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { settings ->
                    isInitializing = true
                    
                    // Dark Mode
                    val isDarkMode = settings.theme == "dark"
                    if (binding.darkModeSwitch.isChecked != isDarkMode) {
                        binding.darkModeSwitch.isChecked = isDarkMode
                    }
                    
                    // Haptics
                    if (binding.hapticsSwitch.isChecked != settings.hapticsEnabled) {
                        binding.hapticsSwitch.isChecked = settings.hapticsEnabled
                    }
                    
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
                    
                    isInitializing = false
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
