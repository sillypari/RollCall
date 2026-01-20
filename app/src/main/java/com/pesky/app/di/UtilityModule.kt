package com.pesky.app.di

import android.content.Context
import com.pesky.app.utils.BiometricHelper
import com.pesky.app.utils.PasswordGenerator
import com.pesky.app.utils.PasswordStrengthAnalyzer
import com.pesky.app.utils.SecureClipboardManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing utility dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {
    
    @Provides
    @Singleton
    fun providePasswordGenerator(): PasswordGenerator {
        return PasswordGenerator()
    }
    
    @Provides
    @Singleton
    fun providePasswordStrengthAnalyzer(): PasswordStrengthAnalyzer {
        return PasswordStrengthAnalyzer()
    }
    
    @Provides
    @Singleton
    fun provideSecureClipboardManager(
        @ApplicationContext context: Context
    ): SecureClipboardManager {
        return SecureClipboardManager(context)
    }
    
    @Provides
    @Singleton
    fun provideBiometricHelper(
        @ApplicationContext context: Context
    ): BiometricHelper {
        return BiometricHelper(context)
    }
}
