package com.simpleattendance

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.simpleattendance.data.repository.SettingsRepository
import com.simpleattendance.data.repository.toNightMode
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RollCallApplication : Application() {
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(settingsRepository.cachedTheme().toNightMode())
    }
}
