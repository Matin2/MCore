package me.matin.core.managers.menu

import me.matin.core.managers.TaskManager
import me.matin.core.managers.menu.items.MenuItem
import me.matin.core.managers.menu.items.button.ButtonManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.scheduler.BukkitTask
import kotlin.reflect.full.hasAnnotation
import kotlin.time.Duration
import me.matin.core.managers.menu.MenuItem as MenuItemAnnotation

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
    private val tasksToRun: MutableList<Triple<Pair<Duration, Duration>, Boolean, () -> Unit>> = mutableListOf()

    fun open() = TaskManager.runTask(true) {
        type.type?.also {
            inventory = Bukkit.createInventory(this, it, title)
        } ?: run {
            inventory = Bukkit.createInventory(this, type.rows!! * 9, title)
        }
        processItems()
        ButtonManager(this).manageDisplay()
        TaskManager.runTask {
            player.openInventory(inventory)
            opened = true
        }
        while (!opened) Thread.sleep(10)
        for ((delayInterval, async, action) in tasksToRun) {
            val task = TaskManager.scheduleTask(delayInterval.first, delayInterval.second, async, action)
            runningTasks.add(task)
        }
        tasksToRun.removeAll { true }
    }

    fun close(closeInventory: Boolean = true) {
        if (closeInventory) player.closeInventory()
        opened = false
        TaskManager.runTask(true) {
            runningTasks.forEach {
                it.cancel()
                runningTasks.remove(it)
            }
        }
    }

    private fun processItems() =
        this::class.members.filter { it.hasAnnotation<MenuItemAnnotation>() && it.parameters.size == 1 }
            .forEach { member ->
                when (val result = member.call(this)) {
                    is MenuItem -> items.add(result)
                    is Iterable<*> -> result.mapTo(items) {
                        if (it !is MenuItem) return@forEach
                        it
                    }
                }
            }

    fun scheduleTask(
        delay: Duration = Duration.ZERO,
        interval: Duration = Duration.ZERO,
        async: Boolean = false,
        task: () -> Unit
    ) {
        if (opened) {
            tasksToRun.add(Triple(delay to interval, async, task))
            return
        }
        val bukkitTask = TaskManager.scheduleTask(delay, interval, async, task)
        runningTasks.add(bukkitTask)
    }

    override fun getInventory(): Inventory {
        return inventory
    }
}