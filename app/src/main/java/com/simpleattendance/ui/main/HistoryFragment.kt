package com.simpleattendance.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.databinding.FragmentHistoryBinding
import com.simpleattendance.ui.history.GroupedHistoryAdapter
import com.simpleattendance.ui.history.HistoryViewModel
import com.simpleattendance.ui.report.ReportActivity
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HistoryViewModel by viewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    // Track if spinner is being set programmatically
    private var isSpinnerInitializing = false
    private var spinnerInitialized = false
    
    private val historyAdapter by lazy {
        GroupedHistoryAdapter(
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
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeState()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            setHasFixedSize(false)
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.emptyState.visibility = if (state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (!state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE
                    
                    // Setup filter spinner only once
                    if (state.classes.isNotEmpty() && !spinnerInitialized) {
                        spinnerInitialized = true
                        isSpinnerInitializing = true
                        
                        val classNames = listOf("All Classes") + state.classes.map { it.fullDisplayName }
                        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, classNames)
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.classFilterSpinner.adapter = spinnerAdapter
                        
                        binding.classFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                if (isSpinnerInitializing) {
                                    isSpinnerInitializing = false
                                    return
                                }
                                hapticUtils.lightTap()
                                val classId = if (position == 0) null else state.classes[position - 1].id
                                viewModel.filterByClass(classId)
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                    }
                    
                    // Use grouped adapter
                    historyAdapter.setSessionsGroupedByDate(state.sessions)
                }
            }
        }
    }
    
    private fun openReport(session: AttendanceSessionEntity) {
        val intent = Intent(requireContext(), ReportActivity::class.java)
        intent.putExtra("sessionId", session.id)
        intent.putExtra("fromHistory", true)
        startActivity(intent)
    }
    
    private fun confirmDelete(session: AttendanceSessionEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Session")
            .setMessage("Are you sure you want to delete this attendance session?")
            .setPositiveButton("Delete") { _, _ ->
                hapticUtils.mediumImpact()
                viewModel.deleteSession(session)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
