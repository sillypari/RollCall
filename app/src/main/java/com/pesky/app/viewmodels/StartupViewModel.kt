package com.pesky.app.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.pesky.app.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

sealed class StartupState {
    object Loading : StartupState()
    object NewUser : StartupState()
    object ReturningUser : StartupState()
}

@HiltViewModel
class StartupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) : ViewModel() {
    
    private val _startupState = MutableStateFlow<StartupState>(StartupState.Loading)
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()
    
    val databaseName: String
        get() = appPreferences.databaseName.ifEmpty { "My Vault" }
    
    init {
        checkStartupState()
    }
    
    private fun checkStartupState() {
        // Check if there's an existing database in internal storage
        val dbDir = File(context.filesDir, "databases")
        val hasInternalDb = dbDir.exists() && 
            dbDir.listFiles()?.any { it.extension == "pesky" && it.length() > 0 } == true
        
        // Check preferences
        val hasDatabase = appPreferences.hasDatabase || hasInternalDb
        
        _startupState.value = if (hasDatabase) {
            StartupState.ReturningUser
        } else {
            StartupState.NewUser
        }
    }
    
    /**
     * Force refresh the startup state (useful after creating a new database)
     */
    fun refreshState() {
        checkStartupState()
    }
}
