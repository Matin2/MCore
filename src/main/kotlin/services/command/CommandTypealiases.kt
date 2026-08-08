package com.github.matin2.mcore.services.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope

internal typealias CommandLiteralBuilder = LiteralArgumentBuilder<CommandSourceStack>
internal typealias CommandArgumentBuilder<T> = RequiredArgumentBuilder<CommandSourceStack, T>

internal typealias CommandCoroutineScope = () -> CoroutineScope?

internal typealias CommandSourcePredicate = @CommandDsl CommandSourceStack.() -> Boolean
