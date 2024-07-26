package me.matin.core.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
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

    @JvmStatic
    fun parse(
        input: String,
        color: Boolean = true,
        bold: Boolean = true,
        italic: Boolean = true,
        underlined: Boolean = true,
        strike: Boolean = true,
        obfuscated: Boolean = true,
        click: Boolean = true,
        hover: Boolean = true,
        keybind: Boolean = true,
        translatable: Boolean = true,
        insertion: Boolean = true,
        rainbow: Boolean = true,
        gradient: Boolean = true,
        transition: Boolean = true,
        font: Boolean = true,
        newline: Boolean = true,
        selector: Boolean = true,
        score: Boolean = true,
        nbt: Boolean = true,
        legacy: Char? = null,
        vararg extraTags: TagResolver
    ): Component = parse(
        input,
        color,
        bold,
        italic,
        underlined,
        strike,
        obfuscated,
        click,
        hover,
        keybind,
        translatable,
        insertion,
        rainbow,
        gradient,
        transition,
        font,
        newline,
        selector,
        score,
        nbt,
        legacy,
        extraTags.toSet()
    )

    @JvmStatic
    fun parse(
        input: String,
        color: Boolean = true,
        bold: Boolean = true,
        italic: Boolean = true,
        underlined: Boolean = true,
        strike: Boolean = true,
        obfuscated: Boolean = true,
        click: Boolean = true,
        hover: Boolean = true,
        keybind: Boolean = true,
        translatable: Boolean = true,
        insertion: Boolean = true,
        rainbow: Boolean = true,
        gradient: Boolean = true,
        transition: Boolean = true,
        font: Boolean = true,
        newline: Boolean = true,
        selector: Boolean = true,
        score: Boolean = true,
        nbt: Boolean = true,
        legacy: Char? = null,
    ): Component = parse(
        input,
        color,
        bold,
        italic,
        underlined,
        strike,
        obfuscated,
        click,
        hover,
        keybind,
        translatable,
        insertion,
        rainbow,
        gradient,
        transition,
        font,
        newline,
        selector,
        score,
        nbt,
        legacy,
        null
    )

    @Suppress("t")
    private fun parse(
        input: String,
        color: Boolean,
        bold: Boolean,
        italic: Boolean,
        underlined: Boolean,
        strikethrough: Boolean,
        obfuscated: Boolean,
        click: Boolean,
        hover: Boolean,
        keybind: Boolean,
        translatable: Boolean,
        insertion: Boolean,
        rainbow: Boolean,
        gradient: Boolean,
        transition: Boolean,
        font: Boolean,
        newline: Boolean,
        selector: Boolean,
        score: Boolean,
        nbt: Boolean,
        legacy: Char?,
        extraTags: Set<TagResolver>?
    ): Component {
        val resolvers = mutableSetOf<TagResolver>()
        extraTags?.also { resolvers.addAll(it) }
        if (color) resolvers.add(StandardTags.color())
        if (bold) resolvers.add(StandardTags.decorations(TextDecoration.BOLD))
        if (italic) resolvers.add(StandardTags.decorations(TextDecoration.ITALIC))
        if (underlined) resolvers.add(StandardTags.decorations(TextDecoration.UNDERLINED))
        if (strikethrough) resolvers.add(StandardTags.decorations(TextDecoration.STRIKETHROUGH))
        if (obfuscated) resolvers.add(StandardTags.decorations(TextDecoration.OBFUSCATED))
        if (click) resolvers.add(StandardTags.clickEvent())
        if (hover) resolvers.add(StandardTags.hoverEvent())
        if (keybind) resolvers.add(StandardTags.keybind())
        if (translatable) {
            resolvers.add(StandardTags.translatable())
            resolvers.add(StandardTags.translatableFallback())
        }
        if (insertion) resolvers.add(StandardTags.insertion())
        if (rainbow) resolvers.add(StandardTags.rainbow())
        if (gradient) resolvers.add(StandardTags.gradient())
        if (transition) resolvers.add(StandardTags.transition())
        if (font) resolvers.add(StandardTags.font())
        if (newline) resolvers.add(StandardTags.newline())
        if (selector) resolvers.add(StandardTags.selector())
        if (score) resolvers.add(StandardTags.score())
        if (nbt) resolvers.add(StandardTags.nbt())
        val miniMessage = MiniMessage.builder().tags(TagResolver.builder().resolvers(resolvers).build()).build()
        var output: String = input
        legacy.takeIf { color }?.also {
            val legacySerializer = LegacyComponentSerializer.builder().character(it).hexColors().build()
            output = miniMessage.serialize(legacySerializer.deserialize(input))
        }
        return miniMessage.deserialize(output)
    }
}