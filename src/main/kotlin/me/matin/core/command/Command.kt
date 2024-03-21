package me.matin.core.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

open class Command(private val subCommands: ArrayList<SubCommand>): TabExecutor {

        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>?): Boolean {
            if (!args.isNullOrEmpty()) {
                for (i in args.indices) {
                    if (args[i] == null) args[i] = ""
                }
                for (i in subCommands.indices) {
                    if (isSubCommand(args[0], i) && hasRequirements(sender, i)) {
                        val subArgs = args.copyOfRange(1,args.size)
                        subCommands[i].command(sender, label, args[0], subArgs)
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

    open fun canSee(sender: CommandSender): Boolean = subCommands.stream().anyMatch { it.requirements(sender) }

    private fun hasRequirements(sender: CommandSender, i: Int): Boolean {
        return subCommands[i].requirements(sender)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>?
    ): ArrayList<String> {
        var result: ArrayList<String> = ArrayList()
        if (!args.isNullOrEmpty()) {
            for (i in args.indices) {
                if (args[i] == null) args[i] = ""
            }
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
                        result = subCommands[i].tabComplete(sender, label, args[0], args.copyOfRange(1,args.size))
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