package com.simpleattendance.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.ui.components.LaunchOrigin
import com.simpleattendance.ui.main.MainActivity
import com.simpleattendance.ui.report.ReportActivity
import com.simpleattendance.ui.screens.HistoryScreen
import com.simpleattendance.ui.theme.RollCallTheme
import com.simpleattendance.util.HeroTransitionLauncher
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
                        onSessionClick = { session, origin ->
                            hapticUtils.lightTap()
                            openReport(session, origin)
                        },
                        onSessionDeleteClick = { session ->
                            hapticUtils.mediumImpact()
                            viewModel.deleteSession(session)
                        },
                        onSearchFocusChanged = { focused ->
                            (activity as? MainActivity)?.setSearchInteractionActive(focused)
                        }
                    )
                }
            }
        }
    }
    
    private fun openReport(
        session: AttendanceSessionEntity,
        origin: LaunchOrigin
    ) {
        val intent = Intent(requireContext(), ReportActivity::class.java)
        intent.putExtra("sessionId", session.id)
        intent.putExtra("fromHistory", true)
        HeroTransitionLauncher.start(requireActivity(), intent, origin)
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.setSearchInteractionActive(false)
        super.onDestroyView()
    }
}

