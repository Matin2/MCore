package me.matin.core.function.adventure

import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class Adventure {

    val miniMessage: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.color())
                .resolver(StandardTags.decorations())
                .resolver(StandardTags.rainbow())
                .resolver(StandardTags.gradient())
                .resolver(StandardTags.font())
                .resolver(StandardTags.translatable())
                .resolver(StandardTags.reset())
                .build()
        )
        .build()

    val color: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.color())
                .build()
        )
        .build()

    val formatting: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.decorations(TextDecoration.BOLD))
                .resolver(StandardTags.decorations(TextDecoration.ITALIC))
                .resolver(StandardTags.decorations(TextDecoration.STRIKETHROUGH))
                .resolver(StandardTags.decorations(TextDecoration.UNDERLINED))
                .build()
        )
        .build()

    val magic: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.decorations(TextDecoration.OBFUSCATED))
                .build()
        )
        .build()

    val rainbow: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.rainbow())
                .build()
        )
        .build()

    val gradient: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.gradient())
                .build()
        )
        .build()

    val font: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.font())
                .build()
        )
        .build()

    val translatable: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.translatable())
                .build()
        )
        .build()

    val reset: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.reset())
                .build()
        )
        .build()

    val serializer: LegacyComponentSerializer = LegacyComponentSerializer.builder()
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build()

    fun minimessage(parsed: String): String {
        return serializer.serialize(miniMessage.deserialize(parsed))
    }
}