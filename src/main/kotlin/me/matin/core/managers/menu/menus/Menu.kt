package me.matin.core.managers.menu.menus

import me.matin.core.managers.TaskManager
import me.matin.core.managers.menu.InventoryMenu
import me.matin.core.managers.menu.items.button.ButtonManager
import me.matin.core.managers.menu.utils.MenuUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

@Suppress("unused")
abstract class Menu(private val player: Player): InventoryMenu() {

    private lateinit var inventory: Inventory
    private var opened: Boolean = false
    private val util = MenuUtils()

    fun open() = TaskManager.runTask(true) {
        type.type?.also {
            inventory = Bukkit.createInventory(this, it, title)
        } ?: let {
            inventory = Bukkit.createInventory(this, maxOf(minOf(type.rows!!, 6), 1) * 9, title)
        }
        util.processItems(this::class.members, buttons)
        privateUpdateItems()
        TaskManager.runTask {
            player.openInventory(inventory)
            opened = true
        }
        while (!opened) Thread.sleep(10)
        util.scheduleOnOpen()
    }

    fun close(closeInventory: Boolean = true) {
        if (closeInventory) player.closeInventory()
        opened = false
        TaskManager.runTask(true) {
            util.removeTasks()
        }
    }

    fun updateItems() = TaskManager.runTask(true) {
        privateUpdateItems()
    }

    private fun privateUpdateItems() {
        ButtonManager(this).manageDisplay()
    }

    //    fun scheduleTask(
//        delay: Duration = Duration.ZERO,
//        interval: Duration = Duration.ZERO,
//        async: Boolean = false,
//        task: () -> Unit
//    ) = util.scheduleTask(delay, interval, async, task)
    override fun getInventory(): Inventory {
        return inventory
    }
}