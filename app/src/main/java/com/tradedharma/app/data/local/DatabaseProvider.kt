package com.tradedharma.app.data.local

import android.content.Context
import androidx.room.*

object DatabaseProvider {
    @Volatile private var instance: TradeDatabase? = null

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS saved_symbols (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    symbol TEXT NOT NULL,
                    exchange TEXT NOT NULL,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    createdAtEpochMs INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_saved_symbols_symbol_exchange ON saved_symbols(symbol, exchange)")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS saved_exchanges (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    code TEXT NOT NULL,
                    name TEXT NOT NULL,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    createdAtEpochMs INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_saved_exchanges_code ON saved_exchanges(code)")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS saved_lot_sizes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    value REAL NOT NULL,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    createdAtEpochMs INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_saved_lot_sizes_value ON saved_lot_sizes(value)")
        }
    }

    fun get(context: Context): TradeDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(context.applicationContext, TradeDatabase::class.java, "tradelog.db")
            .addMigrations(MIGRATION_2_3)
            .build()
            .also { instance = it }
    }
}
