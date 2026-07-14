package com.simpleattendance.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.ui.report.ReportActivity
import com.simpleattendance.ui.screens.HistoryScreen
import com.simpleattendance.ui.theme.RollCallTheme
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    
    private val viewModel: HistoryViewModel by viewModels()
    
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
                    HistoryScreen(
                        viewModel = viewModel,
                        onSessionClick = { session ->
                            hapticUtils.lightTap()
                            openReport(session)
                        },
                        onSessionDeleteClick = { session ->
                            hapticUtils.mediumImpact()
                            confirmDelete(session)
                        }
                    )
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
            .setMessage("Are you sure you want to delete this attendance session? All records associated with this session will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                hapticUtils.mediumImpact()
                viewModel.deleteSession(session)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

