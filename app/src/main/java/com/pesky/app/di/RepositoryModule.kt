package com.pesky.app.di

import android.content.Context
import com.pesky.app.data.crypto.DatabaseCryptoManager
import com.pesky.app.data.database.PeskyDatabaseHandler
import com.pesky.app.data.database.XMLParser
import com.pesky.app.data.repository.VaultRepository
import com.pesky.app.utils.PasswordStrengthAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing repository and database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideXMLParser(): XMLParser {
        return XMLParser()
    }
    
    @Provides
    @Singleton
    fun providePeskyDatabaseHandler(
        xmlParser: XMLParser,
        cryptoManager: DatabaseCryptoManager
    ): PeskyDatabaseHandler {
        return PeskyDatabaseHandler(
            cryptoManager = cryptoManager,
            xmlParser = xmlParser
        )
    }
}
