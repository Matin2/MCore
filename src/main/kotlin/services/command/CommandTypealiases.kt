package com.github.matin2.mcore.services.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope

typealias CommandScopeSetter = (CoroutineScope) -> Unit

typealias CommandSourcePredicate = CommandSourceStack.() -> Boolean
