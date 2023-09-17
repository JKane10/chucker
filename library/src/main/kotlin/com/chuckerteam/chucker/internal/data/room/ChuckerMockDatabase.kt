package com.chuckerteam.chucker.internal.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction

@Database(entities = [HttpTransaction::class], version = 1, exportSchema = false)
internal abstract class ChuckerMockDatabase : RoomDatabase() {

    abstract fun transactionDao(): HttpTransactionDao

    companion object {
        private const val DB_NAME = "chucker.db.mock"

        fun create(applicationContext: Context): ChuckerMockDatabase {
            return Room.databaseBuilder(applicationContext, ChuckerMockDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
