package com.simpleattendance.di

import android.content.Context
import androidx.room.Room
import com.simpleattendance.data.local.AppDatabase
import com.simpleattendance.data.local.dao.ClassDao
import com.simpleattendance.data.local.dao.StudentDao
import com.simpleattendance.data.local.dao.AttendanceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rollcall.db"
        ).build()
    }
    
    @Provides
    fun provideClassDao(database: AppDatabase): ClassDao = database.classDao()
    
    @Provides
    fun provideStudentDao(database: AppDatabase): StudentDao = database.studentDao()
    
    @Provides
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao = database.attendanceDao()
}
