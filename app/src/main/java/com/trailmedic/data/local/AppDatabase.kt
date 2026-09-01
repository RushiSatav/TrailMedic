package com.trailmedic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.trailmedic.data.local.dao.SessionDao
import com.trailmedic.data.local.entity.SessionEntity

@Database(entities = [SessionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
