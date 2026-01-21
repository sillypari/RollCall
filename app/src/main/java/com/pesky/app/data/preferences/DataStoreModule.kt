package com.pesky.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Singleton DataStore for app preferences.
 * This MUST be declared as a top-level property extension to avoid multiple instances.
 */
val Context.peskyDataStore: DataStore<Preferences> by preferencesDataStore(name = "pesky_settings")
