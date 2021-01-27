package com.kevincheng.extensions

sealed class Optional<out T : Any> {
    abstract val isPresent: Boolean
}

object None : Optional<Nothing>() {
    override val isPresent: Boolean = false
}

data class Some<T : Any>(val value: T) : Optional<T>() {
    override val isPresent: Boolean = true
}

fun <T : Any> T?.asOptional(): Optional<T> = this?.let(::Some) ?: None

val <T : Any> Optional<T>.value: T?
    get() = when (this) {
        is Some -> value
        else -> null
    }

fun <T : Any> Optional<T>.asIterable(): Iterable<T> = listOfNotNull(value)

inline fun <T : Any> Optional<T>.filter(predicate: (T) -> Boolean): Optional<T> =
    value?.takeIf(predicate)?.let { this } ?: None

inline fun <T : Any, U : Any> Optional<T>.flatMap(mapper: (T) -> Optional<U>): Optional<U> =
    value?.let(mapper) ?: None

inline fun <T : Any> Optional<T>.ifPresent(consumer: (T) -> Unit) {
    value?.let(consumer)
}

inline fun <T : Any, U : Any> Optional<T>.map(mapper: (T) -> U): Optional<U> =
    flatMap { Some(mapper(it)) }

inline fun <T : Any> Optional<T>.orElse(other: () -> T): T = value ?: other()