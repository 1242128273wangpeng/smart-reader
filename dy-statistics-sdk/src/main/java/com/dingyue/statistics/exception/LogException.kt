package com.dingyue.statistics.exception


class LogException : Exception {
    private val serialVersionUID = -3451945810203597732L
    private var errorCode: String? = null

    private var requestId: String? = null

    constructor(message: String) : super(message)

    constructor(code: String, message: String) : super(message) {
        this.errorCode = code
    }

    constructor(code: String, message: String, requestId: String) : super(message) {
        this.errorCode = code
        this.requestId = requestId
    }

    constructor(code: String, message: String, cause: Throwable,
                requestId: String) : super(message, cause) {
        this.errorCode = code
        this.requestId = requestId
    }

    fun getErrorCode(): String? {
        return this.errorCode
    }

    fun getErrorMessage(): String {
        return super.message.orEmpty()
    }

    /**
     * Get the request id
     *
     * @return request id, if the error is happened in the client, the request
     * id is empty
     */
    fun getRequestId(): String? {
        return this.requestId
    }
}
