package me.matin.core.managers

@Suppress("unused")
object TextManager {

    private fun String.customSplit(separator: String): List<String> {
        if (separator.isNotEmpty()) return this.split(separator)
        if (" " in this.trim()) return this.split(" ")
        return this.split("(?<=.)(?=\\p{Lu})".toRegex())
    }

    @JvmStatic
    fun String.camelcase(splitBy: String = "", separator: String = ""): String {
        val result = StringBuilder()
        val separated = this.customSplit(splitBy)
        for ((index, value) in separated.withIndex()) {
            if (index == 0) {
                result.append(value.lowercase())
                continue
            }
            result.append(separator + value.first().uppercase() + value.drop(1).lowercase())
        }
        return result.toString()
    }

    @JvmStatic
    fun String.pascalcase(splitBy: String? = "", separator: String = ""): String {
        splitBy ?: return this.first().uppercase() + this.drop(1).lowercase()
        val result = StringBuilder()
        this.customSplit(splitBy).forEachIndexed { index, string ->
            val text = string.first().uppercase() + string.drop(1).lowercase()
            if (index == 0) result.append(text)
            else result.append(separator + text)
        }
        return result.toString()
    }

    @JvmStatic
    fun String.alternatecase(): String {
        val result = StringBuilder()
        for ((index, char) in this.toCharArray().withIndex()) {
            if (index % 2 == 0) {
                result.append(char.lowercase())
                continue
            }
            result.append(char.uppercase())
        }
        return result.toString()
    }

    @JvmStatic
    fun String.lowercase(splitBy: String = "", separator: String): String {
        val result = StringBuilder()
        this.customSplit(splitBy).forEachIndexed { index, string ->
            val text = string.lowercase()
            if (index == 0) result.append(text)
            else result.append(separator + text)
        }
        return result.toString()
    }
}