package me.matin.core.command

import org.bukkit.command.CommandSender

abstract class SubCommand {

    abstract val name: String
    open val aliases: ArrayList<String> = ArrayList()
    open fun requirements(sender: CommandSender): Boolean = true
    abstract fun command(sender: CommandSender, args: Array<String>)
    abstract fun tabComplete(sender: CommandSender, args: Array<String>): ArrayList<String>
}