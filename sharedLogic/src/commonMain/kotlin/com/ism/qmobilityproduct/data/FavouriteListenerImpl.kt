package com.ism.qmobilityproduct.data

import com.ism.qmobilityproduct.domain.FavouriteEvent
import com.ism.qmobilityproduct.domain.FavouriteListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FavouriteListenerImpl : FavouriteListener {

    private val _events = MutableSharedFlow<FavouriteEvent>(extraBufferCapacity = 1)
    override val events: Flow<FavouriteEvent> = _events.asSharedFlow()

    override fun notifyChanged(event: FavouriteEvent) {
        _events.tryEmit(event)
    }
}
