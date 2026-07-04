@file:Suppress("unused")

package com.github.matin2.mcore.methods.utils

import kotlin.time.Duration
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/** Returns a [Duration] equal to this [Int] number of server ticks. */
inline val Int.ticks get() = times(50).toDuration(MILLISECONDS)

/** Returns a [Duration] equal to this [Long] number of server ticks. */
inline val Long.ticks get() = times(50).toDuration(MILLISECONDS)

/**
 * The value of this duration expressed as a [Long] number of server ticks.
 *
 * The part of this duration that is smaller than a server tick becomes a
 * fractional part of the result and then is truncated (rounded towards
 * zero).
 *
 * An infinite duration value is converted either to [Long.MAX_VALUE] or
 * [Long.MIN_VALUE] depending on its sign.
 */
inline val Duration.inTicks get() = toLong(MILLISECONDS) / 50
