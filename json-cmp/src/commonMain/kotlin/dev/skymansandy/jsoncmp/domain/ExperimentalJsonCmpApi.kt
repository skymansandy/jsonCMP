package dev.skymansandy.jsoncmp.domain

/**
 * Marks an API as experimental within the JsonCMP library.
 *
 * Marked API is subject to change or removal in future releases
 * and does not provide any compatibility guarantees.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "JsonCMP is in experimental state. Public API can change in future releases",
)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
@SinceKotlin("2.2")
annotation class ExperimentalJsonCmpApi
