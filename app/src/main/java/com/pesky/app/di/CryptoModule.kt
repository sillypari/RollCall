package com.pesky.app.di

import android.content.Context
import com.pesky.app.data.crypto.AESCryptoManager
import com.pesky.app.data.crypto.Argon2KeyDerivation
import com.pesky.app.data.crypto.CompressionManager
import com.pesky.app.data.crypto.DatabaseCryptoManager
import com.pesky.app.data.crypto.HMACValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing cryptography dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {
    
    @Provides
    @Singleton
    fun provideArgon2KeyDerivation(): Argon2KeyDerivation {
        return Argon2KeyDerivation()
    }
    
    @Provides
    @Singleton
    fun provideAESCryptoManager(): AESCryptoManager {
        return AESCryptoManager()
    }
    
    @Provides
    @Singleton
    fun provideHMACValidator(): HMACValidator {
        return HMACValidator()
    }
    
    @Provides
    @Singleton
    fun provideCompressionManager(): CompressionManager {
        return CompressionManager()
    }
    
    @Provides
    @Singleton
    fun provideDatabaseCryptoManager(
        argon2KeyDerivation: Argon2KeyDerivation,
        aesCryptoManager: AESCryptoManager,
        hmacValidator: HMACValidator,
        compressionManager: CompressionManager
    ): DatabaseCryptoManager {
        return DatabaseCryptoManager(
            argon2KeyDerivation = argon2KeyDerivation,
            aesCryptoManager = aesCryptoManager,
            hmacValidator = hmacValidator,
            compressionManager = compressionManager
        )
    }
}
