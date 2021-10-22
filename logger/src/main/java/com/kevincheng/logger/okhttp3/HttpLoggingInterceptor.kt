package com.kevincheng.logger.okhttp3

import com.orhanobut.logger.Logger
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.nio.charset.Charset
import java.util.Collections
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class HttpLoggingInterceptor : Interceptor {

    private val _debug = AtomicBoolean()
    private val _headerKeys = Collections.synchronizedList(arrayListOf<String>())
    private val _headersToRedact = Collections.synchronizedSet(mutableSetOf<String>())

    var isDebug: Boolean
        get() = _debug.get()
        set(value) = _debug.set(value)

    var headerKeys: Array<String>
        get() = _headerKeys.toTypedArray()
        set(value) {
            _headerKeys.clear()
            _headerKeys.addAll(value)
        }

    var headersToRedact: Set<String>
        get() = _headersToRedact.toSet()
        set(value) {
            _headersToRedact.clear()
            _headersToRedact.addAll(value)
        }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body()

        var log = "--> ${request.method()} ${request.url()} ${chain.connection()?.protocol()}"
        log += NEW_LINE

        val requestHeaders = request.headers()
        for (index in 0 until requestHeaders.size()) {
            val key = requestHeaders.name(index)
            val value = requestHeaders.value(index)
            when (isDebug) {
                true -> log += "$key: $value$NEW_LINE"
                false -> {
                    val ignore = !headerKeys.contains(key)
                    if (!ignore) log += "${when (headersToRedact.contains(key)) {
                        true -> REDACT
                        false -> key
                    }}: $value$NEW_LINE"
                }
            }
        }

        log += when {
            requestBody == null -> "--> END ${request.method()}"
            bodyHasUnknownEncoding(request.headers()) -> "--> END ${request.method()} (encoded body omitted)"
            else -> {
                var string = ""
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                if (isPlaintext(buffer, requestBody.contentLength())) {
                    val size: String
                    when {
                        requestBody.contentLength() > 0L -> {
                            val charset: Charset = requestBody.contentType()?.charset(UTF8) ?: UTF8
                            string += "${buffer.readString(charset)}$NEW_LINE"
                            size = "${requestBody.contentLength()}-byte"
                        }
                        requestBody.contentLength() == 0L -> {
                            size = "empty"
                        }
                        else -> {
                            size = "unknown-byte"
                        }
                    }
                    string += "--> END ${request.method()} ($size body)"
                } else {
                    string =
                        ("--> END ${request.method()} (binary ${requestBody.contentLength()}-byte body omitted)")
                }
                string
            }
        }
        log += NEW_LINE

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            log += "<-- HTTP FAILED: $e"
            Logger.t(TAG).v(log)
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = requireNotNull(response.body())

        log += "<-- ${response.code()}" +
            if (response.message().isEmpty()) "" else " ${response.message()}" +
                " ${response.request().url()}" +
                " (${tookMs}ms)$NEW_LINE"

        val responseHeaders = response.headers()
        for (index in 0 until responseHeaders.size()) {
            val key = responseHeaders.name(index)
            val value = responseHeaders.value(index)
            when (isDebug) {
                true -> log += "$key: $value$NEW_LINE"
                false -> {
                    val ignore = !headerKeys.contains(key)
                    if (!ignore) log += "${when (headersToRedact.contains(key)) {
                        true -> REDACT
                        false -> key
                    }}: $value$NEW_LINE"
                }
            }
        }

        log += when {
            !HttpHeaders.hasBody(response) -> "<-- END HTTP"
            bodyHasUnknownEncoding(response.headers()) -> "<-- END HTTP (encoded body omitted)"
            else -> {
                var string = ""
                val source = responseBody.source()
                source.request(Long.MAX_VALUE)
                var buffer = source.buffer

                var gzippedLength: Long? = null
                if ("gzip".equals(response.headers().get("Content-Encoding"), ignoreCase = true)) {
                    gzippedLength = responseBody.contentLength()
                    var gzippedResponseBody: GzipSource? = null
                    try {
                        gzippedResponseBody = GzipSource(buffer.clone())
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    } finally {
                        gzippedResponseBody?.also { it.close() }
                    }
                }

                if (isPlaintext(buffer, responseBody.contentLength())) {
                    when {
                        responseBody.contentLength() != 0L -> {
                            val charset: Charset = responseBody.contentType()?.charset(UTF8) ?: UTF8
                            string += "${buffer.clone().readString(charset)}$NEW_LINE"
                        }
                    }
                    val size = when {
                        gzippedLength != null -> "${gzippedLength.takeIf { it != -1L } ?: "unknown"}-gzipped-byte"
                        responseBody.contentLength() > 0L -> "${responseBody.contentLength()}-byte"
                        responseBody.contentLength() == 0L -> "empty"
                        else -> "unknown-byte"
                    }
                    string += "<-- END HTTP ($size body)"
                } else {
                    string += "<-- END HTTP (binary ${responseBody.contentLength()
                        .takeIf { it != -1L } ?: "unknown"}-byte body omitted)"
                }

                string
            }
        }
        Logger.t(TAG).v(log)

        return response
    }

    companion object {
        private const val TAG = "http-request"
        private const val REDACT = "██"
        private val UTF8 = Charset.forName("UTF-8")
        private val NEW_LINE = System.getProperty("line.separator") ?: "\n"

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        private fun isPlaintext(buffer: Buffer, contentLength: Long): Boolean {
            if (contentLength < 0) return true
            try {
                val prefix = Buffer()
                val byteCount = if (contentLength < 64) contentLength else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                return true
            } catch (e: EOFException) {
                return false // Truncated UTF-8 sequence.
            }
        }

        private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
            val contentEncoding = headers.get("Content-Encoding")
            return (contentEncoding != null &&
                !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true))
        }
    }
}