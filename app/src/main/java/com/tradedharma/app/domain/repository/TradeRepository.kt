package com.tradedharma.app.domain.repository

import androidx.room.withTransaction
import com.tradedharma.app.data.local.*

class TradeRepository(
    private val db: TradeDatabase
) {
    private val tradeDao = db.tradeDao()
    private val reviewDao = db.reviewDao()
    private val catalogDao = db.savedCatalogDao()

    fun observeTrades() = tradeDao.observeAll()
    suspend fun get(id: Long) = tradeDao.getById(id)
    suspend fun insert(trade: TradeEntity) = tradeDao.insert(trade)
    suspend fun update(trade: TradeEntity) = tradeDao.update(trade)
    suspend fun delete(trade: TradeEntity) = tradeDao.delete(trade)
    suspend fun deleteAll() = tradeDao.deleteAll()

    fun observeReviews() = reviewDao.observeAll()
    suspend fun getReview(dateKey: String) = reviewDao.get(dateKey)
    suspend fun saveReview(review: ReviewEntity) = reviewDao.upsert(review)

    fun observeSymbols() = catalogDao.observeSymbols()
    fun observeExchanges() = catalogDao.observeExchanges()
    fun observeLotSizes() = catalogDao.observeLotSizes()

    suspend fun seedCatalogDefaults() {
        db.withTransaction {
            catalogDao.insertExchange(SavedExchangeEntity(symbolDefaultId(), "NSE", "National Stock Exchange", true))
            catalogDao.insertExchange(SavedExchangeEntity(symbolDefaultId(), "BSE", "Bombay Stock Exchange", true))
            catalogDao.insertSymbol(SavedSymbolEntity(symbolDefaultId(), "NIFTY", "NSE", true))
            catalogDao.insertSymbol(SavedSymbolEntity(symbolDefaultId(), "BANK NIFTY", "NSE", true))
            catalogDao.insertSymbol(SavedSymbolEntity(symbolDefaultId(), "SENSEX", "BSE", true))
            listOf(65.0, 30.0, 15.0, 20.0, 1.0).forEach { catalogDao.insertLotSize(SavedLotSizeEntity(symbolDefaultId(), it, true)) }
        }
    }

    private fun symbolDefaultId() = 0L

    suspend fun addSymbol(symbol: String, exchange: String): Long =
        catalogDao.insertSymbol(SavedSymbolEntity(symbol = symbol.trim().uppercase(), exchange = exchange.trim().uppercase()))

    suspend fun deleteCustomSymbol(id: Long) = catalogDao.deleteCustomSymbol(id)
    suspend fun addExchange(code: String, name: String): Long =
        catalogDao.insertExchange(SavedExchangeEntity(code = code.trim().uppercase(), name = name.trim()))
    suspend fun deleteCustomExchange(id: Long) = catalogDao.deleteCustomExchange(id)
    suspend fun addLotSize(value: Double): Long = catalogDao.insertLotSize(SavedLotSizeEntity(value = value))
    suspend fun deleteCustomLotSize(id: Long) = catalogDao.deleteCustomLotSize(id)
    suspend fun insertTradesTransactional(trades: List<TradeEntity>) = db.withTransaction { trades.forEach { tradeDao.insert(it) } }
}
