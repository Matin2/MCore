package me.matin.core.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

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

    /**
     * Returns a component representation of the object.
     *
     * @param color (Optional) Color of the component.
     * @param decorations (Optional) Decorations of the component.
     * @see toString
     */
    @JvmStatic
    fun <T: Any> T.toComponent(color: TextColor? = null, vararg decorations: TextDecoration = arrayOf()): Component =
        Component.text(this.toString(), color, *decorations)
}