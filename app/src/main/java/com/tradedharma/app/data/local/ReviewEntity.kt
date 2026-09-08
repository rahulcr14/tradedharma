package com.tradedharma.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tradedharma.app.domain.model.ReviewRating

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val dateKey: String,
    val rating: ReviewRating = ReviewRating.PARTIAL,
    val reflection: String = "",
    val improvement: String = "",
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
