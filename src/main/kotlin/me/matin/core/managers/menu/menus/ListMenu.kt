package me.matin.core.managers.menu.menus

import me.matin.core.Core
import me.matin.core.managers.menu.InventoryMenu
import me.matin.core.managers.menu.items.MenuItem
import me.matin.core.managers.menu.items.button.Button
import me.matin.core.managers.menu.items.button.ButtonManager
import me.matin.core.managers.menu.items.other.Filler
import me.matin.core.managers.menu.items.other.ListMap
import me.matin.core.managers.menu.items.other.MenuList
import me.matin.core.managers.menu.items.slot.Slot
import me.matin.core.managers.menu.items.slot.SlotManager
import me.matin.core.managers.menu.utils.MenuUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.Inventory
import kotlin.reflect.full.hasAnnotation
import kotlin.time.Duration

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class ListMenu<T>(private val player: Player, page: Int = 0): InventoryMenu() {

    abstract val list: MenuList<T>
    open val listFiller: Filler = Filler()
    private lateinit var inventory: Inventory
    private var opened: Boolean = false
    private var listMap: ListMap? = null
    private lateinit var fillerSlots: Set<Int>
    private lateinit var buttonManager: ButtonManager
    private lateinit var slotManager: SlotManager
    private lateinit var fillerManager: Filler.Manager
    private lateinit var listManager: MenuList.Manager<T>
    private val util = MenuUtils()
    var pages: Int = 0
    var page: Int = if (page in 0..<pages) page else 0
        set(value) {
            pages = (list.list.size / list.slots.size) + 1
            field = when {
                value < 0 -> (value % pages) + pages
                value >= pages -> value % pages
                else -> value
            }
            Core.scheduleTask(true) {
                privateUpdateItems(false, useDefaultItem = false)
            }
        }

    fun open() = Core.scheduleTask(true) {
        type.type?.also {
            inventory = Bukkit.createInventory(this, it, title)
        } ?: let {
            inventory = Bukkit.createInventory(this, maxOf(minOf(type.rows!!, 6), 1) * 9, title)
        }
        makeListMap()
        buttonManager = ButtonManager(inventory)
        slotManager = SlotManager(inventory)
        listManager = MenuList.Manager(this, listMap!!)
        fillerManager = Filler.Manager(this, fillerSlots)
        processItems()
        privateUpdateItems(true, useDefaultItem = true)
        Core.scheduleTask {
            player.openInventory(inventory)
            opened = true
        }
        while (!opened) Thread.sleep(10)
        util.scheduleOnOpen()
    }

    override fun close(closeInventory: Boolean) {
        if (closeInventory) player.closeInventory()
        opened = false
        Core.scheduleTask(true) {
            util.removeTasks()
        }
    }

    fun scheduleTask(
        async: Boolean = false, delay: Duration = Duration.ZERO, interval: Duration = Duration.ZERO, task: () -> Unit
    ) = util.scheduleTask(async, delay, interval, task)

    override fun manageBehaviour(event: InventoryInteractEvent) {
        slotManager.manageBehavior(event)
        if (event !is InventoryClickEvent) return
        buttonManager.manageBehavior(event)
        fillerManager.manageBehavior(event)
        listManager.manageBehaviour(event)
    }

    fun updateItems() = Core.scheduleTask(true) {
        privateUpdateItems(true, useDefaultItem = false)
    }

    private fun privateUpdateItems(updatePages: Boolean, useDefaultItem: Boolean) {
        if (updatePages) pages = (list.list.size / list.slots.size) + 1
        val fs = (0..<inventory.size).toMutableSet()
        buttonManager.manageDisplay(fs)
        slotManager.manageDisplay(fs, useDefaultItem)
        fillerSlots = fs
        fillerManager.manageDisplay()
        if (listMap != null) listManager.manageDisplay()
    }

    @Suppress("DuplicatedCode")
    private fun processItems() =
        this::class.members.filter { it.hasAnnotation<MenuItem>() && it.parameters.size == 1 }.forEach { member ->
            when (val result = member.call(this)) {
                is Button -> buttonManager.buttons.add(result)
                is Slot -> slotManager.slots.add(result)
                is Iterable<*> -> {
                    result.forEach {
                        when (it) {
                            is Button -> buttonManager.buttons.add(it)
                            is Slot -> slotManager.slots.add(it)
                        }
                    }
                }
            }
        }

    private fun makeListMap() {
        listMap = list.run {
            list.indices.map {
                it to slots.elementAt(it % slots.size)
            }.groupBy {
                it.first / slots.size
            }
        }
    }

    override fun getInventory(): Inventory {
        return inventory
    }
}