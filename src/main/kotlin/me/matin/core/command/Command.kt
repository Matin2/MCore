package me.matin.core.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class Command(private var subCommands: List<SubCommand>): TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty()) {
            for (i in getSubCommands().indices) {
                if (isSubCommand(args[0], i) && hasRequirements(sender, i)) {
                    getSubCommands()[i].command(sender, args)
                }
            }
        }
        return true
    }

    private fun getSubCommands(): List<SubCommand> {
        return subCommands
    }

    private fun isSubCommand(alias: String, i: Int): Boolean {
        if (getSubCommands()[i].name.equals(alias, ignoreCase = true)) return true
        val aliases = getSubCommands()[i].aliases
        for (s in aliases) {
            if (s.equals(alias, ignoreCase = true)) return true
        }
        return false
    }

    private fun hasRequirements(sender: CommandSender, i: Int): Boolean {
        return getSubCommands()[i].requirements(sender)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        var result: List<String>? = null
        if (args.size == 1) {
            val allSubCommands: MutableList<String> = ArrayList()
            for (i in getSubCommands().indices) {
                if (hasRequirements(sender, i)) allSubCommands.add(getSubCommands()[i].name)
                allSubCommands.addAll(getSubCommands()[i].aliases)
            }
            result = allSubCommands
        } else if (args.size >= 2) {
            for (i in getSubCommands().indices) {
                if (isSubCommand(args[0], i) && hasRequirements(sender, i)) {
                    result = getSubCommands()[i].tabComplete(sender, args)
                }
            }
        }
        for (i in 2..args.size) {
            if (args.size == i && args[i - 2].isEmpty()) {
                result = ArrayList()
                break
            }
        }
        if (result == null) result = ArrayList()
        return result
    }

}