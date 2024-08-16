package me.matin.core.managers

import java.util.*
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Suppress("unused")
object Extras {

    /** Returns an [Optional] representation of the object. */
    @JvmStatic
    val <T: Any> T?.optional get() = Optional.ofNullable(this)

    /** Returns an [Optional] representation of the object. */
    @JvmStatic
    val <T: Any> T?.opt get() = Optional.ofNullable(this)

    /** Converts this [Duration] value to server ticks. */
    @JvmStatic
    val Duration.ticks: Long
        get() {
            val seconds = this.toDouble(DurationUnit.SECONDS)
            return (seconds * 20).roundToLong()
        }

    /** Returns a readable text representation of this duration. */
    @JvmStatic
    fun Duration.text(separator: String = " "): String = buildString {
        this@text.toComponents { days, hours, minutes, seconds, nanos ->
            val millis = nanos / 1_000_000
            append("${separator + days}d")
            addTime(days, "d", separator)
            addTime(hours, "h", separator)
            addTime(minutes, "m", separator)
            addTime(seconds, "s", separator)
            addTime(millis, "ms", separator)
            if (toString().isBlank()) append("0ms")
        }
    }.removeSuffix(separator)

    private fun <T: Number> StringBuilder.addTime(
        time: T,
        suffix: String,
        separator: String
    ): StringBuilder? = time.takeIf { it.toInt() > 0 }?.let {
        this.append(time.toString() + suffix + separator)
    }
}