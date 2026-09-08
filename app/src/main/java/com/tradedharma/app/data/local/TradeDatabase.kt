package com.tradedharma.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TradeEntity::class, ReviewEntity::class, SavedSymbolEntity::class, SavedExchangeEntity::class, SavedLotSizeEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TradeDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao
    abstract fun reviewDao(): ReviewDao
    abstract fun savedCatalogDao(): SavedCatalogDao
}
