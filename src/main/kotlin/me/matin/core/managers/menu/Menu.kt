package me.matin.core.managers.menu

import me.matin.core.Core
import me.matin.core.managers.menu.items.MenuItem
import me.matin.core.managers.menu.MenuItem as MenuItemAnnotation
import me.matin.core.managers.menu.items.button.ButtonManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.scheduler.BukkitTask
import kotlin.reflect.full.hasAnnotation

@Suppress("unused")
abstract class Menu(private val player: Player): InventoryHolder {

    private lateinit var inventory: Inventory
    abstract val title: Component
    abstract val type: MenuType
    val items = mutableSetOf<MenuItem>()
    open val freezeBottomInv: Boolean = false
    open val preventCursorLoss: Boolean = true
    private var opened: Boolean = false
    private val runningTasks = mutableSetOf<BukkitTask>()
    private val tasksToRun: MutableSet<Triple<Pair<Long, Long>, Boolean, () -> Unit>> = mutableSetOf()

    fun open() {
        type.type?.also {
            inventory = Bukkit.createInventory(this, it, title)
        } ?: run {
            inventory = Bukkit.createInventory(this, type.rows!! * 9, title)
        }
        processItems()
        ButtonManager(this).manageDisplay()
        player.openInventory(inventory)
        opened = true
        for ((intervalDelay, async, action) in tasksToRun) {
            val task = Bukkit.getScheduler().run {
                if (async) runTaskTimerAsynchronously(Core.plugin, action, intervalDelay.first, intervalDelay.second)
                else runTaskTimer(Core.plugin, action, intervalDelay.first, intervalDelay.second)
            }
            runningTasks.add(task)
        }
        tasksToRun.removeAll { true }
    }

    fun close(closeInventory: Boolean = true) {
        opened = false
        if (closeInventory) player.closeInventory()
        runningTasks.forEach {
            it.cancel()
            runningTasks.remove(it)
        }
    }

    private fun processItems() = this::class.members.forEach { member ->
        if (member.hasAnnotation<MenuItemAnnotation>()) {
            if (member.parameters.size == 1) {
                val result = member.call(this) as? MenuItem ?: return@forEach
                items.add(result)
            }
        }
    }

    fun scheduleTask(delay: Long = 0, interval: Long = 0, async: Boolean, task: () -> Unit) {
        if (opened) {
            tasksToRun.add(Triple(delay to interval, async, task))
            return
        }
        val bukkitTask = if (async) Bukkit.getScheduler().runTaskTimerAsynchronously(Core.plugin, task, delay, interval)
        else Bukkit.getScheduler().runTaskTimer(Core.plugin, task, delay, interval)
        runningTasks.add(bukkitTask)
    }

    override fun getInventory(): Inventory {
        return inventory
    }
}