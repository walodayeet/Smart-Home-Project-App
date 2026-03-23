package com.example.smarthomedemo2.data

import kotlinx.coroutines.flow.Flow

class LogRepository(private val logDao: LogDao) {
    val allLogs: Flow<List<LogEntry>> = logDao.getAllLogs()

    suspend fun insertLog(action: String, location: String) {
        logDao.insertLog(LogEntry(action = action, location = location))
    }

    suspend fun clearLogs() {
        logDao.clearLogs()
    }
}
