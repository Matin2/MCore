package me.matin.core.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class Command(private val subCommands: ArrayList<SubCommand>): TabExecutor {

        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
            if (args != null) {
                if (args.isNotEmpty()) {
                    for (i in subCommands.indices) {
                        if (isSubCommand(args[0], i) && hasRequirements(sender, i)) {
                            subCommands[i].command(sender, args)
                        }
                    }
                }
            }
        return true
    }

    private fun isSubCommand(alias: String, i: Int): Boolean {
        if (subCommands[i].name.equals(alias, ignoreCase = true)) return true
        val aliases = subCommands[i].aliases
        for (s in aliases) {
            if (s.equals(alias, ignoreCase = true)) return true
        }
        return false
    }

    private fun hasRequirements(sender: CommandSender, i: Int): Boolean {
        return subCommands[i].requirements(sender)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): ArrayList<String> {
        var result: ArrayList<String> = ArrayList()
        if (args != null) {
            if (args.size == 1) {
                for (i in subCommands.indices) {
                    if (hasRequirements(sender, i)) {
                        result.add(subCommands[i].name)
                        result.addAll(subCommands[i].aliases)
                    }
                }
            } else if (args.size >= 2) {
                for (i in subCommands.indices) {
                    if (isSubCommand(args[0], i) && hasRequirements(sender, i)) {
                        result = subCommands[i].tabComplete(sender, args.copyOfRange(1,args.size))
                    }
                }
            }
            for (i in 2..args.size) {
                if (args.size == i && args[i - 2].isEmpty()) {
                    result = ArrayList()
                    break
                }
            }
        }
        return result
    }

}