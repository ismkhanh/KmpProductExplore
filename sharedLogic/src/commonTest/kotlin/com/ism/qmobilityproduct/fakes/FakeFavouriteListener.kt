package com.ism.qmobilityproduct.fakes

import com.ism.qmobilityproduct.domain.FavouriteEvent
import com.ism.qmobilityproduct.domain.FavouriteListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakeFavouriteListener : FavouriteListener {

    private val _events = MutableSharedFlow<FavouriteEvent>(extraBufferCapacity = 1)
    override val events: Flow<FavouriteEvent> = _events.asSharedFlow()

    val emittedEvents = mutableListOf<FavouriteEvent>()

    override fun notifyChanged(event: FavouriteEvent) {
        emittedEvents.add(event)
        _events.tryEmit(event)
    }
}
