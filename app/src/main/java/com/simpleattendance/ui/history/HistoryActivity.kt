package com.simpleattendance.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.databinding.ActivityHistoryBinding
import com.simpleattendance.ui.main.MainActivity
import com.simpleattendance.ui.report.ReportActivity
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    private val historyAdapter by lazy {
        HistoryAdapter(
            onSessionClick = { session ->
                hapticUtils.lightTap()
                openReport(session)
            },
            onSessionLongClick = { session ->
                hapticUtils.mediumImpact()
                confirmDelete(session)
            }
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupFilter()
        setupBottomNavigation()
        observeState()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            hapticUtils.lightTap()
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupFilter() {
        // Will be populated when classes are loaded
    }
    
    private fun setupBottomNavigation() {
        // Bottom navigation removed from layout - navigation handled by toolbar
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.emptyState.visibility = if (state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (!state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE
                    
                    // Update filter spinner
                    if (state.classes.isNotEmpty()) {
                        val classNames = listOf("All Classes") + state.classes.map { it.fullDisplayName }
                        val adapter = ArrayAdapter(this@HistoryActivity, android.R.layout.simple_spinner_item, classNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.classFilterSpinner.adapter = adapter
                        
                        binding.classFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                hapticUtils.lightTap()
                                val classId = if (position == 0) null else state.classes[position - 1].id
                                viewModel.filterByClass(classId)
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                    }
                    
                    historyAdapter.submitList(state.sessions)
                }
            }
        }
    }
    
    private fun openReport(session: AttendanceSessionEntity) {
        val intent = Intent(this, ReportActivity::class.java)
        intent.putExtra("sessionId", session.id)
        startActivity(intent)
    }
    
    private fun confirmDelete(session: AttendanceSessionEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Session")
            .setMessage("Are you sure you want to delete this attendance session?")
            .setPositiveButton("Delete") { _, _ ->
                hapticUtils.mediumImpact()
                viewModel.deleteSession(session)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
