package com.simpleattendance.ui.attendance

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simpleattendance.R
import com.simpleattendance.databinding.ActivityAttendanceBinding
import com.simpleattendance.ui.report.ReportActivity
import com.simpleattendance.util.AnimationUtils
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AttendanceActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAttendanceBinding
    private val viewModel: AttendanceViewModel by viewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    private var hasShownCompleteDialog = false
    private var currentFillAnimator: ObjectAnimator? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupButtons()
        observeState()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            hapticUtils.lightTap()
            confirmExit()
        }
        
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_reset -> {
                    hapticUtils.heavyImpact()
                    showResetConfirmation()
                    true
                }
                R.id.action_sort -> {
                    hapticUtils.lightTap()
                    showSortOptions()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun showResetConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Attendance?")
            .setMessage("This will clear all marked students and start over.")
            .setPositiveButton("Reset") { _, _ ->
                hasShownCompleteDialog = false
                viewModel.resetAttendance()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showSortOptions() {
        val options = arrayOf("Alphabetical (A-Z)", "Original Order (As in list)")
        MaterialAlertDialogBuilder(this)
            .setTitle("Sort Students")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.sortAlphabetically()
                    1 -> viewModel.sortByOriginalOrder()
                }
            }
            .show()
    }
    
    private fun setupButtons() {
        // Present button - light haptic with glass fill animation
        binding.presentButton.setOnClickListener {
            hapticUtils.lightTap()
            animateButtonFill(binding.presentFill, binding.presentText, true)
            animateNameFlash(true) // Flash name green
            viewModel.markPresent()
        }
        
        // Absent button - strong haptic with glass fill animation
        binding.absentButton.setOnClickListener {
            hapticUtils.heavyImpact()
            animateButtonFill(binding.absentFill, binding.absentText, false)
            animateNameFlash(false) // Flash name red
            viewModel.markAbsent()
        }
        
        binding.previousButton.setOnClickListener {
            hapticUtils.lightTap()
            viewModel.goToPrevious()
        }
        
        binding.nextButton.setOnClickListener {
            hapticUtils.lightTap()
            viewModel.goToNext()
        }
        
        binding.finishButton.setOnClickListener {
            hapticUtils.heavyImpact()
            showSaveConfirmation()
        }
    }
    
    private fun animateNameFlash(isPresent: Boolean) {
        val color = if (isPresent) getColor(R.color.success_green) else getColor(R.color.error_red)
        
        // Immediately set the status color
        binding.studentName.setTextColor(color)
        binding.studentCard.strokeColor = color
        
        // Animate stroke width for a "pulse" effect on the card border
        val originalStrokeWidth = resources.getDimensionPixelSize(R.dimen.card_stroke_width)
        binding.studentCard.strokeWidth = (originalStrokeWidth * 3) // Make border thicker
        binding.studentCard.postDelayed({
            binding.studentCard.strokeWidth = originalStrokeWidth // Return to normal
        }, 150)
        
        // Scale up animation for emphasis
        binding.studentCard.animate()
            .scaleX(1.03f)
            .scaleY(1.03f)
            .setDuration(100)
            .withEndAction {
                binding.studentCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
        
        // Animate the name text with a quick scale pop
        binding.studentName.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(80)
            .withEndAction {
                binding.studentName.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start()
            }
            .start()
        
        // Trigger progress bar glow animation
        animateProgressGlow(isPresent)
    }
    
    private fun animateProgressGlow(isPresent: Boolean) {
        val color = if (isPresent) getColor(R.color.success_green) else getColor(R.color.error_red)
        
        // Tint the glow with the action color
        binding.progressGlow.background.setTint(color)
        
        // Get the current width of the progress bar container
        val trackWidth = binding.progressTrack.width.toFloat()
        if (trackWidth <= 0) return
        
        // Animate glow sweeping across the bar
        binding.progressGlow.apply {
            alpha = 0.7f
            translationX = -width.toFloat()
            animate()
                .translationX(trackWidth)
                .alpha(0f)
                .setDuration(500)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
        
        // Also pulse the progress bar slightly
        binding.progressTrack.animate()
            .scaleY(1.3f)
            .setDuration(100)
            .withEndAction {
                binding.progressTrack.animate()
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
    
    private fun updateProgressBar(presentCount: Int, absentCount: Int, totalCount: Int) {
        if (totalCount <= 0) return
        
        val trackWidth = binding.progressTrack.width
        if (trackWidth <= 0) {
            // Post to run after layout
            binding.progressTrack.post {
                updateProgressBar(presentCount, absentCount, totalCount)
            }
            return
        }
        
        val presentWidth = (presentCount.toFloat() / totalCount * trackWidth).toInt()
        val absentWidth = (absentCount.toFloat() / totalCount * trackWidth).toInt()
        
        // Animate present bar width
        binding.progressPresent.animate()
            .setDuration(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setUpdateListener { animator ->
                val params = binding.progressPresent.layoutParams
                params.width = (presentWidth * animator.animatedFraction).toInt()
                binding.progressPresent.layoutParams = params
            }
            .start()
        
        // Animate absent bar (positioned after present)
        binding.progressAbsent.animate()
            .setDuration(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setUpdateListener { animator ->
                val params = binding.progressAbsent.layoutParams
                params.width = (absentWidth * animator.animatedFraction).toInt()
                binding.progressAbsent.layoutParams = params
                binding.progressAbsent.translationX = presentWidth.toFloat()
            }
            .start()
    }
    
    private fun animateButtonFill(fillView: View, textView: View, isPresent: Boolean) {
        // Cancel any running animation
        currentFillAnimator?.cancel()
        
        // Animate fill from 0 to 0.9 alpha (glass effect)
        currentFillAnimator = ObjectAnimator.ofFloat(fillView, "alpha", 0f, 0.85f).apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        // Change text color to white
        (textView as? android.widget.TextView)?.setTextColor(
            ContextCompat.getColor(this, R.color.on_primary)
        )
    }
    
    private fun updateButtonVisuals(status: String?) {
        // Cancel any running animation first
        currentFillAnimator?.cancel()
        currentFillAnimator = null
        
        // Reset both buttons first
        binding.presentFill.alpha = 0f
        binding.absentFill.alpha = 0f
        binding.presentText.setTextColor(ContextCompat.getColor(this, R.color.success_green))
        binding.absentText.setTextColor(ContextCompat.getColor(this, R.color.error_red))
        
        when (status) {
            "P" -> {
                // Present is selected - show filled
                binding.presentFill.alpha = 0.85f
                binding.presentText.setTextColor(ContextCompat.getColor(this, R.color.on_primary))
            }
            "A" -> {
                // Absent is selected - show filled
                binding.absentFill.alpha = 0.85f
                binding.absentText.setTextColor(ContextCompat.getColor(this, R.color.on_primary))
            }
        }
    }
    
    private fun showSaveConfirmation() {
        val state = viewModel.uiState.value
        val unmarkedCount = state.students.size - state.markedCount
        
        if (unmarkedCount > 0) {
            // Show warning for incomplete attendance
            MaterialAlertDialogBuilder(this)
                .setTitle("Incomplete Attendance")
                .setMessage("$unmarkedCount students are not marked yet.\n\nPresent: ${state.presentCount}\nAbsent: ${state.absentCount}\nUnmarked: $unmarkedCount\n\nWhat would you like to do?")
                .setPositiveButton("Save Anyway") { _, _ ->
                    hapticUtils.successPattern()
                    viewModel.saveAttendance()
                }
                .setNegativeButton("Continue Taking", null)
                .show()
        } else {
            // All marked, just confirm
            MaterialAlertDialogBuilder(this)
                .setTitle("Save Attendance")
                .setMessage("Present: ${state.presentCount}\nAbsent: ${state.absentCount}\n\nSave this attendance record?")
                .setPositiveButton("Save") { _, _ ->
                    hapticUtils.successPattern()
                    viewModel.saveAttendance()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun showCompletionDialog() {
        if (hasShownCompleteDialog) return
        hasShownCompleteDialog = true
        
        val state = viewModel.uiState.value
        hapticUtils.successPattern()
        
        MaterialAlertDialogBuilder(this)
            .setTitle("All Students Marked!")
            .setMessage("Present: ${state.presentCount}\nAbsent: ${state.absentCount}\n\nWould you like to save the attendance now?")
            .setPositiveButton("Save Now") { _, _ ->
                hapticUtils.successPattern()
                viewModel.saveAttendance()
            }
            .setNegativeButton("Review First", null)
            .setCancelable(false)
            .show()
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Loading
                    if (state.isLoading) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.contentLayout.visibility = View.GONE
                        return@collect
                    }
                    
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                    
                    // Class info
                    state.classEntity?.let { classEntity ->
                        binding.toolbar.title = classEntity.displayName
                        binding.toolbar.subtitle = classEntity.subject
                    }
                    
                    // Current student
                    state.currentStudent?.let { studentAttendance ->
                        binding.studentName.text = studentAttendance.student.name
                        binding.studentNumber.text = "${state.currentIndex + 1} / ${state.students.size}"
                        binding.rollNumber.text = studentAttendance.student.rollNo.ifEmpty { "#${state.currentIndex + 1}" }
                        
                        // Animate student card
                        AnimationUtils.scaleIn(binding.studentCard)
                        
                        // Update name color and card border based on status
                        when (studentAttendance.status) {
                            "P" -> {
                                binding.studentName.setTextColor(getColor(R.color.success_green))
                                binding.studentCard.strokeColor = getColor(R.color.success_green)
                            }
                            "A" -> {
                                binding.studentName.setTextColor(getColor(R.color.error_red))
                                binding.studentCard.strokeColor = getColor(R.color.error_red)
                            }
                            else -> {
                                binding.studentName.setTextColor(getColor(R.color.text_primary))
                                binding.studentCard.strokeColor = getColor(R.color.glass_border)
                            }
                        }
                        
                        // Update A/P button visuals based on current student's status
                        updateButtonVisuals(studentAttendance.status)
                    }
                    
                    // Progress
                    val progressPercent = if (state.students.isNotEmpty()) {
                        ((state.currentIndex + 1).toFloat() / state.students.size * 100).toInt()
                    } else 0
                    binding.progressText.text = "${state.markedCount}/${state.students.size} marked"
                    
                    // Update animated progress bar
                    updateProgressBar(state.presentCount, state.absentCount, state.students.size)
                    
                    // Live stats row
                    binding.livePresent.text = "${state.presentCount} Present"
                    binding.liveAbsent.text = "${state.absentCount} Absent"
                    val remaining = state.students.size - state.markedCount
                    binding.liveRemaining.text = "$remaining Left"
                    
                    // Navigation buttons
                    binding.previousButton.isEnabled = state.currentIndex > 0
                    binding.nextButton.isEnabled = state.currentIndex < state.students.size - 1
                    
                    // Finish button - always enabled, can save anytime
                    binding.finishButton.isEnabled = true
                    binding.finishButton.alpha = 1f
                    
                    // Show completion dialog when all are marked
                    if (state.allMarked && !hasShownCompleteDialog) {
                        showCompletionDialog()
                    }
                    
                    // Handle save complete
                    state.savedSessionId?.let { sessionId ->
                        navigateToReport(sessionId)
                    }
                }
            }
        }
    }
    
    private fun navigateToReport(sessionId: Long) {
        val intent = Intent(this, ReportActivity::class.java)
        intent.putExtra("sessionId", sessionId)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    
    private fun confirmExit() {
        val state = viewModel.uiState.value
        if (state.markedCount > 0) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Discard Attendance?")
                .setMessage("You have marked ${state.markedCount} students. Discard this session?")
                .setPositiveButton("Discard") { _, _ -> finish() }
                .setNegativeButton("Continue", null)
                .show()
        } else {
            finish()
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        confirmExit()
    }
}