package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VisionRecordDao {

    @Query("SELECT * FROM vision_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<VisionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: VisionRecord): Long

    @Query("DELETE FROM vision_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM vision_records")
    suspend fun clearAllRecords()

    @Query("SELECT COUNT(*) FROM vision_records")
    suspend fun getCount(): Int
}
