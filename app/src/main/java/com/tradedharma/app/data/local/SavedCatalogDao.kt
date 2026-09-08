package com.tradedharma.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCatalogDao {
    @Query("SELECT * FROM saved_symbols ORDER BY isDefault DESC, CASE symbol WHEN 'NIFTY' THEN 0 WHEN 'BANK NIFTY' THEN 1 WHEN 'SENSEX' THEN 2 ELSE 100 END, symbol COLLATE NOCASE ASC")
    fun observeSymbols(): Flow<List<SavedSymbolEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSymbol(entity: SavedSymbolEntity): Long

    @Query("DELETE FROM saved_symbols WHERE id = :id AND isDefault = 0")
    suspend fun deleteCustomSymbol(id: Long): Int

    @Query("SELECT COUNT(*) FROM saved_symbols WHERE isDefault = 0")
    suspend fun countCustomSymbols(): Int

    @Query("SELECT * FROM saved_exchanges ORDER BY isDefault DESC, CASE code WHEN 'NSE' THEN 0 WHEN 'BSE' THEN 1 ELSE 100 END, code COLLATE NOCASE ASC")
    fun observeExchanges(): Flow<List<SavedExchangeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExchange(entity: SavedExchangeEntity): Long

    @Query("DELETE FROM saved_exchanges WHERE id = :id AND isDefault = 0")
    suspend fun deleteCustomExchange(id: Long): Int

    @Query("SELECT COUNT(*) FROM saved_exchanges WHERE isDefault = 0")
    suspend fun countCustomExchanges(): Int

    @Query("SELECT * FROM saved_lot_sizes ORDER BY isDefault DESC, CASE value WHEN 65.0 THEN 0 WHEN 30.0 THEN 1 WHEN 15.0 THEN 2 WHEN 20.0 THEN 3 WHEN 1.0 THEN 4 ELSE 100 END, value ASC")
    fun observeLotSizes(): Flow<List<SavedLotSizeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLotSize(entity: SavedLotSizeEntity): Long

    @Query("DELETE FROM saved_lot_sizes WHERE id = :id AND isDefault = 0")
    suspend fun deleteCustomLotSize(id: Long): Int

    @Query("SELECT COUNT(*) FROM saved_lot_sizes WHERE isDefault = 0")
    suspend fun countCustomLotSizes(): Int

    @Query("SELECT COUNT(*) FROM saved_lot_sizes WHERE isDefault = 1")
    suspend fun countDefaultLotSizes(): Int
}
