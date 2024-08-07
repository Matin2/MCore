package me.matin.core.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Suppress("unused")
object TextManager {

    private fun String.customSplit(separator: String): List<String> {
        if (separator.isNotEmpty()) return this.split(separator)
        if (" " in this.trim()) return this.split(" ")
        return this.split("(?<=.)(?=\\p{Lu})".toRegex())
    }

    @JvmStatic
    fun String.camelcase(splitBy: String = "", separator: String = ""): String {
        val separated = this.customSplit(splitBy)
        return buildString {
            for ((index, value) in separated.withIndex()) {
                if (index == 0) {
                    append(value.lowercase())
                    continue
                }
                append(separator + value.first().uppercase() + value.drop(1).lowercase())
            }
        }
    }

    @JvmStatic
    fun String.pascalcase(splitBy: String? = "", separator: String = ""): String {
        splitBy ?: return this.first().uppercase() + this.drop(1).lowercase()
        return buildString {
            this@pascalcase.customSplit(splitBy).forEachIndexed { index, string ->
                when (index) {
                    0 -> append(string.first().uppercase() + string.drop(1).lowercase())
                    else -> append(separator + string.first().uppercase() + string.drop(1).lowercase())
                }
            }
        }
    }

    @JvmStatic
    fun String.alternatecase(): String {
        return buildString {
            for ((index, char) in this@alternatecase.toCharArray().withIndex()) {
                if (index % 2 == 0) {
                    append(char.lowercase())
                    continue
                }
                append(char.uppercase())
            }
        }
    }

    @JvmStatic
    fun String.lowercase(splitBy: String = "", separator: String): String {
        return buildString {
            this@lowercase.customSplit(splitBy).forEachIndexed { index, string ->
                val text = string.lowercase()
                if (index == 0) append(text)
                else append(separator + text)
            }
        }
    }

    @JvmStatic
    fun Duration.toReadableString(
        separator: String = " ",
        makePlural: Boolean = false,
        daySuffix: String = "d",
        hourSuffix: String = "h",
        minuteSuffix: String = "m",
        secondSuffix: String = "s",
        millisecondSuffix: String = "ms"
    ): String = buildString {
        this@toReadableString.toComponents { days, hours, minutes, seconds, nanos ->
            addTime(days, daySuffix, makePlural, separator)
            addTime(hours.toLong(), hourSuffix, makePlural, separator)
            addTime(minutes, minuteSuffix, makePlural, separator)
            addTime(seconds, secondSuffix, makePlural, separator)
            addTime(nanos / 1_000_000, millisecondSuffix, makePlural, separator)
            if (toString().isBlank()) append("0$millisecondSuffix")
        }
    }.removeSuffix(separator)

    private fun <T: Number> StringBuilder.addTime(time: T, suffix: String, makePlural: Boolean, separator: String) {
        time.takeIf { it.toInt() > 0 }?.also {
            if (!makePlural) {
                append(time.toString() + suffix + separator)
                return
            }
            val newSuffix = if (it.toInt() > 1) "${suffix}s" else suffix
            append(it.toString() + newSuffix + separator)
        }
    }

    @JvmStatic
    fun Duration.toTicks(): Long {
        val seconds = this.toDouble(DurationUnit.SECONDS)
        return (seconds * 20).roundToLong()
    }

    /**
     * Converts the given string to a [Component]
     *
     * @param color (Optional) Color of the component.
     * @param decorations (Optional) Decorations of the component.
     * @return The converted component.
     * @receiver The string to convert.
     */
    @JvmStatic
    fun <T: Any> T.toComponent(color: TextColor? = null, vararg decorations: TextDecoration = arrayOf()): Component =
        Component.text(this.toString(), color, *decorations)
}