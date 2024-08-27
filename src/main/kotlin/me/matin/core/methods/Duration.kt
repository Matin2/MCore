@file:Suppress("unused")

package me.matin.core.methods

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/** Converts this [Duration] value to server ticks. */
val Duration.inTicks: Double get() = toDouble(DurationUnit.SECONDS) * 20

/** Converts this [Duration] value to server ticks. */
val Duration.inWholeTicks: Long get() = inTicks.toLong()

/** Converts this integer value to server ticks ([Duration]). */
val Int.ticks: Duration get() = toDouble().div(20).toDuration(DurationUnit.SECONDS)

/** Converts this long value to server ticks ([Duration]). */
val Long.ticks: Duration get() = toDouble().div(20).toDuration(DurationUnit.SECONDS)