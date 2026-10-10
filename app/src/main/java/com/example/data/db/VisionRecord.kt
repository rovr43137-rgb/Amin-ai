package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vision_records")
data class VisionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val identifiedTitle: String,
    val fullArabicResponse: String,
    val userPrompt: String,
    val providerName: String,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)
