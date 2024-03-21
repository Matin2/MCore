package me.matin.core.function

import com.mojang.brigadier.tree.LiteralCommandNode
import me.lucko.commodore.CommodoreProvider
import me.lucko.commodore.file.CommodoreFileReader
import me.matin.core.command.Command
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.function.Predicate


class MCommodore {
    companion object {
        @JvmStatic
        fun register(plugin: Plugin, pluginCommand: PluginCommand, command: Command, commodoreFileName: String) {
            val commodore = CommodoreProvider.getCommodore(plugin)
            val commodoreName = if (commodoreFileName.endsWith(".commodore")) commodoreFileName else "$commodoreFileName.commodore"
            val file = plugin.getResource(commodoreName)
            if (file != null) {
                val commandNode: LiteralCommandNode<Any> = CommodoreFileReader.INSTANCE.parse(file)
                commodore.register(
                    pluginCommand, commandNode, Predicate {it: Player ->
                        return@Predicate command.canSee(it)
                    }
                )
            }
        }
    }
}