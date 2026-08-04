package com.github.matin2.mcore.services.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

internal typealias CommandLiteralBuilder = LiteralArgumentBuilder<CommandSourceStack>

internal typealias CommandSourcePredicate = @CommandDsl CommandSourceStack.() -> Boolean
