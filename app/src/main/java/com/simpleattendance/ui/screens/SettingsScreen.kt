package com.simpleattendance.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simpleattendance.BuildConfig
import com.simpleattendance.ui.components.InfoSettingRow
import com.simpleattendance.ui.components.RollCallSurface
import com.simpleattendance.ui.components.SegmentedSettingRow
import com.simpleattendance.ui.components.SwitchSettingRow
import com.simpleattendance.ui.settings.SettingsViewModel
import com.simpleattendance.ui.theme.RollCallSpacing

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = RollCallSpacing.screenHorizontal)
            .padding(top = RollCallSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(RollCallSpacing.xl)
    ) {
        SettingsSectionCard(title = "Appearance", accented = true) {
            SegmentedSettingRow(
                title = "Theme",
                description = "Follow your device or choose a fixed appearance",
                options = listOf(
                    "System" to "system",
                    "Light" to "light",
                    "Dark" to "dark"
                ),
                selectedValue = settings.theme,
                onValueSelected = viewModel::setTheme
            )
        }

        SettingsSectionCard(title = "Interaction") {
            SwitchSettingRow(
                title = "Haptic Feedback",
                description = "Tactile response for marks and saves",
                checked = settings.hapticsEnabled,
                onCheckedChange = viewModel::setHapticsEnabled
            )
            SettingsDivider()
            SegmentedSettingRow(
                title = "Attendance Input",
                description = "Choose how students are marked",
                options = listOf(
                    "Buttons" to "buttons",
                    "Swipe" to "swipe",
                    "Both" to "both"
                ),
                selectedValue = settings.attendanceMode,
                onValueSelected = viewModel::setAttendanceMode
            )
        }

        SettingsSectionCard(title = "Reports") {
            SegmentedSettingRow(
                title = "Report Template",
                description = "Choose which students appear in reports",
                options = listOf(
                    "Both" to "both",
                    "Absent" to "absent_only",
                    "Present" to "present_only"
                ),
                selectedValue = settings.reportTemplate,
                onValueSelected = viewModel::setReportTemplate
            )
            SettingsDivider()
            SegmentedSettingRow(
                title = "Numbering",
                description = "Student numbering used in reports",
                options = listOf(
                    "Relative" to "relative",
                    "Absolute" to "absolute"
                ),
                selectedValue = settings.numberingMode,
                onValueSelected = viewModel::setNumberingMode
            )
        }

        SettingsSectionCard(title = "About") {
            InfoSettingRow(
                title = "Version",
                value = "v${BuildConfig.VERSION_NAME} Stable"
            )
            SettingsDivider()
            InfoSettingRow(title = "Developer", value = "Parikshit Singh Bais")
            SettingsDivider()
            InfoSettingRow(title = "Privacy", value = "Offline, no ads, no tracking")
        }

        Spacer(Modifier.height(104.dp))
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    accented: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = RollCallSpacing.lg,
                end = RollCallSpacing.lg,
                bottom = RollCallSpacing.sm
            )
        )
        val brush = if (accented) {
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.surface,
                    lerp(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer,
                        0.22f
                    )
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.surface,
                    lerp(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        0.35f
                    )
                )
            )
        }
        RollCallSurface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
                    shape = MaterialTheme.shapes.large
                ),
            shape = MaterialTheme.shapes.large,
            brush = brush
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(horizontal = RollCallSpacing.lg),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
    )
}
