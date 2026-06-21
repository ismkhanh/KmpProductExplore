package com.ism.qmobilityproduct.domain.listener

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class FavouriteEvent(
    val productId: Int,
    val isFavourite: Boolean,
)

interface FavouriteListener {
    val events: Flow<FavouriteEvent>
    fun notifyChanged(event: FavouriteEvent)
}

class FavouriteListenerImpl : FavouriteListener {

    private val _events = MutableSharedFlow<FavouriteEvent>(extraBufferCapacity = 1)
    override val events: Flow<FavouriteEvent> = _events.asSharedFlow()

    override fun notifyChanged(event: FavouriteEvent) {
        _events.tryEmit(event)
    }
}
