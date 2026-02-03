package com.simpleattendance.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.databinding.FragmentClassListBinding
import com.simpleattendance.ui.attendance.AttendanceActivity
import com.simpleattendance.ui.createclass.CreateClassActivity
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClassListFragment : Fragment() {
    
    private var _binding: FragmentClassListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by activityViewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    private val classAdapter by lazy {
        ClassAdapter(
            onClassClick = { classEntity ->
                hapticUtils.lightTap()
                startAttendance(classEntity)
            },
            onClassLongClick = { classEntity ->
                hapticUtils.mediumImpact()
                showClassOptions(classEntity)
            },
            onGroupClick = { group ->
                hapticUtils.lightTap()
                viewModel.toggleGroupExpansion(group.batchKey)
            }
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassListBinding.inflate(inflater, container, false)
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
            adapter = classAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.emptyState.visibility = if (state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (!state.isEmpty && !state.isLoading) View.VISIBLE else View.GONE
                    
                    classAdapter.submitList(state.groupedClasses)
                }
            }
        }
    }
    
    private fun startAttendance(classEntity: ClassEntity) {
        val intent = Intent(requireContext(), AttendanceActivity::class.java)
        intent.putExtra("classId", classEntity.id)
        startActivity(intent)
    }
    
    private fun showClassOptions(classEntity: ClassEntity) {
        val options = arrayOf("Edit Class", "Duplicate Class", "Delete Class")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(classEntity.fullDisplayName)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editClass(classEntity)
                    1 -> duplicateClass(classEntity)
                    2 -> confirmDeleteClass(classEntity)
                }
            }
            .show()
    }
    
    private fun editClass(classEntity: ClassEntity) {
        val intent = Intent(requireContext(), CreateClassActivity::class.java)
        intent.putExtra("classId", classEntity.id)
        startActivity(intent)
    }
    
    private fun duplicateClass(classEntity: ClassEntity) {
        hapticUtils.lightTap()
        val intent = Intent(requireContext(), CreateClassActivity::class.java)
        intent.putExtra("duplicateClassId", classEntity.id)
        startActivity(intent)
    }
    
    private fun confirmDeleteClass(classEntity: ClassEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Class")
            .setMessage("Are you sure you want to delete ${classEntity.fullDisplayName}? This will also delete all attendance records for this class.")
            .setPositiveButton("Delete") { _, _ ->
                hapticUtils.mediumImpact()
                viewModel.deleteClass(classEntity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
