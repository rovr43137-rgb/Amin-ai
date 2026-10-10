package com.example.data.db

import kotlinx.coroutines.flow.Flow

class VisionHistoryRepository(private val dao: VisionRecordDao) {

    val allRecords: Flow<List<VisionRecord>> = dao.getAllRecords()

    suspend fun saveRecord(
        identifiedTitle: String,
        fullArabicResponse: String,
        userPrompt: String,
        providerName: String,
        latencyMs: Long
    ): Long {
        val record = VisionRecord(
            identifiedTitle = identifiedTitle,
            fullArabicResponse = fullArabicResponse,
            userPrompt = userPrompt,
            providerName = providerName,
            latencyMs = latencyMs
        )
        return dao.insertRecord(record)
    }

    suspend fun deleteRecord(id: Long) {
        dao.deleteRecordById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllRecords()
    }
}
