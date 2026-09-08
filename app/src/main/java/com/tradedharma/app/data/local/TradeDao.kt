package com.tradedharma.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades ORDER BY openedAtEpochMs DESC")
    fun observeAll(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE id = :id")
    suspend fun getById(id: Long): TradeEntity?

    @Insert
    suspend fun insert(trade: TradeEntity): Long

    @Update
    suspend fun update(trade: TradeEntity)

    @Delete
    suspend fun delete(trade: TradeEntity)

    @Query("DELETE FROM trades")
    suspend fun deleteAll()
}
