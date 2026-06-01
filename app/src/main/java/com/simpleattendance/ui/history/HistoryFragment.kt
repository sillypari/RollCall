package com.simpleattendance.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.databinding.FragmentHistoryBinding
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
    
    private val filterChipsAdapter by lazy {
        FilterChipsAdapter(
            onChipSelected = { classEntity, chipView ->
                hapticUtils.lightTap()
                viewModel.filterByClass(classEntity?.id)
            }
        )
    }
    
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
        
        binding.classFilterChips.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = filterChipsAdapter
            setHasFixedSize(true)
            
            // Prevent ViewPager2 from intercepting horizontal scrolls on the filter chips
            addOnItemTouchListener(object : androidx.recyclerview.widget.RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: androidx.recyclerview.widget.RecyclerView, e: android.view.MotionEvent): Boolean {
                    when (e.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            rv.parent.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                    return false
                }
                override fun onTouchEvent(rv: androidx.recyclerview.widget.RecyclerView, e: android.view.MotionEvent) {}
                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            })
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.emptyState.visibility = if (state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (!state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE
                    
                    // Submit classes to horizontal filter chips
                    filterChipsAdapter.submitClasses(state.classes)
                    filterChipsAdapter.setSelectedClassId(state.selectedClassId)
                    
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
