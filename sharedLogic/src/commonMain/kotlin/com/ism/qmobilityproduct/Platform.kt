package com.ism.qmobilityproduct

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform