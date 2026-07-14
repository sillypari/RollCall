package com.simpleattendance.ui.classlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.ui.attendance.AttendanceActivity
import com.simpleattendance.ui.createclass.CreateClassActivity
import com.simpleattendance.ui.screens.ClassListScreen
import com.simpleattendance.ui.theme.RollCallTheme
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClassListFragment : Fragment() {
    
    private val viewModel: ClassListViewModel by activityViewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RollCallTheme {
                    ClassListScreen(
                        viewModel = viewModel,
                        onClassClick = { classEntity ->
                            hapticUtils.lightTap()
                            startAttendance(classEntity)
                        },
                        onClassOptionsClick = { classEntity ->
                            hapticUtils.mediumImpact()
                            showClassOptions(classEntity)
                        }
                    )
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
}

