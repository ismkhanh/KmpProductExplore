package com.ism.qmobilityproduct.domain.model

sealed class ProductResult<out T> {
    data class Success<T>(val data: T) : ProductResult<T>()
    data class Failure(val error: DataError) : ProductResult<Nothing>()
}

sealed class DataError {
    data class Network(val message: String) : DataError()
    data class Server(val code: Int, val message: String) : DataError()
    data class Unknown(val message: String) : DataError()
}

fun DataError.toUserMessage(): String = when (this) {
    is DataError.Network -> "No internet connection. Please check your network."
    is DataError.Server -> "Server error ($code). Please try again later."
    is DataError.Unknown -> "Something went wrong. Please try again."
}
