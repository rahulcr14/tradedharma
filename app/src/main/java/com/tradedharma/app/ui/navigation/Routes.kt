package com.tradedharma.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val TRADES = "trades"
    const val ADD = "add/{tradeId}"
    const val INSIGHTS = "insights"
    const val MORE = "more"
    const val DETAIL = "detail/{tradeId}"
    const val DAILY_REVIEW = "dailyReview"
    const val WEEKLY_REVIEW = "weeklyReview"
    fun add(id: Long? = null) = "add/${id ?: -1L}"
    fun detail(id: Long) = "detail/$id"
}
