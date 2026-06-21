package com.ism.qmobilityproduct.domain

import kotlinx.coroutines.flow.Flow

data class FavouriteEvent(
    val productId: Int,
    val isFavourite: Boolean,
)

interface FavouriteListener {
    val events: Flow<FavouriteEvent>
    fun notifyChanged(event: FavouriteEvent)
}
