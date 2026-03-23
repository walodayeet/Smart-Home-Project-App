package com.example.smarthomedemo2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntry>>

    @Insert
    suspend fun insertLog(log: LogEntry)

    @Query("DELETE FROM activity_logs")
    suspend fun clearLogs()
}
