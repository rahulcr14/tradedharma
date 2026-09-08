package com.tradedharma.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_symbols",
    indices = [Index(value = ["symbol", "exchange"], unique = true)]
)
data class SavedSymbolEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val exchange: String,
    val isDefault: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "saved_exchanges",
    indices = [Index(value = ["code"], unique = true)]
)
data class SavedExchangeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val isDefault: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "saved_lot_sizes",
    indices = [Index(value = ["value"], unique = true)]
)
data class SavedLotSizeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val value: Double,
    val isDefault: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
