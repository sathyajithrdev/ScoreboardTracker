package com.saj.android.scoreboardtracker.model

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?,
    val errorCode: Int = 0
) {
    fun isSuccess(): Boolean = status == Status.SUCCESS

    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> networkError(msg: String, data: T?): Resource<T> {
            return Resource(Status.NETWORK_ERROR, data, msg)
        }

        fun <T> httpError(errorCode: Int, msg: String, data: T?): Resource<T> {
            return Resource(Status.HTTP_ERROR, data, msg, errorCode)
        }

        fun <T> genericError(msg: String, data: T? = null): Resource<T> {
            return Resource(Status.GENERIC_ERROR, data, msg)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}