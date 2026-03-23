package com.example.smarthomedemo2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis()
)
