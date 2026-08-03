package com.github.matin2.mcore.services.command

import io.papermc.paper.command.brigadier.CommandSourceStack

typealias CommandSourcePredicate = CommandSourceStack.() -> Boolean
