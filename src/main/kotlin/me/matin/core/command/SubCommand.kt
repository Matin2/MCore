package me.matin.core.command

import org.bukkit.command.CommandSender

abstract class SubCommand {

    abstract val name: String
    abstract val aliases: Array<String>
    abstract val description: String
    abstract val syntax: String
    abstract fun requirements(sender: CommandSender): Boolean
    abstract fun command(sender: CommandSender, args: Array<String>)
    abstract fun tabComplete(sender: CommandSender?, args: Array<String>): List<String>
}