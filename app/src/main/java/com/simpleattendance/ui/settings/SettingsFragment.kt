package com.simpleattendance.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simpleattendance.ui.components.RollCallTopBar
import com.simpleattendance.ui.screens.SettingsScreen
import com.simpleattendance.ui.theme.RollCallTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    
    private val viewModel: SettingsViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RollCallTheme {
                    Scaffold(
                        topBar = {
                            RollCallTopBar(
                                title = "Settings",
                                subtitle = "Configure app behaviors and preferences"
                            )
                        }
                    ) { innerPadding ->
                        SettingsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

