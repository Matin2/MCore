package me.matin.core.managers.menu.menus

import me.matin.core.Core
import me.matin.core.managers.menu.InventoryMenu
import me.matin.core.managers.menu.items.Filler
import me.matin.core.managers.menu.items.Interacted
import me.matin.core.managers.menu.items.ListItem
import me.matin.core.managers.menu.items.MenuItem
import me.matin.core.managers.menu.items.button.Button
import me.matin.core.managers.menu.items.button.ButtonAction
import me.matin.core.managers.menu.items.button.ButtonManager
import me.matin.core.managers.menu.utils.MenuUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import kotlin.reflect.full.hasAnnotation
import kotlin.time.Duration

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class ListMenu<T>(private val player: Player, page: Int = 0): InventoryMenu() {

    abstract val item: ListItem<T>
    open val listFiller: Filler = Filler()
    private lateinit var inventory: Inventory
    private var opened: Boolean = false
    private var listMap: Map<Int, List<Pair<Int, Int>>>? = null
    private lateinit var fillerSlots: Set<Int>
    private val util = MenuUtils()
    var pages: Int = 0
    var page: Int = if (page in 0..<pages) page else 0
        set(value) {
            pages = (item.list.size / item.slots.size) + 1
            field = when {
                value < 0 -> (value % pages) + pages
                value >= pages -> value % pages
                else -> value
            }
            Core.scheduleTask(true) {
                privateUpdateItems(false)
            }
        }

    fun open() = Core.scheduleTask(true) {
        type.type?.also {
            inventory = Bukkit.createInventory(this, it, title)
        } ?: let {
            inventory = Bukkit.createInventory(this, maxOf(minOf(type.rows!!, 6), 1) * 9, title)
        }
        makeListMap()
        processItems()
        privateUpdateItems(true)
        Core.scheduleTask {
            player.openInventory(inventory)
            opened = true
        }
        while (!opened) Thread.sleep(10)
        util.scheduleOnOpen()
    }

    fun close(closeInventory: Boolean = true) {
        if (closeInventory) player.closeInventory()
        opened = false
        Core.scheduleTask(true) {
            util.removeTasks()
        }
    }

    fun scheduleTask(
        async: Boolean = false,
        delay: Duration = Duration.ZERO,
        interval: Duration = Duration.ZERO,
        task: () -> Unit
    ) = util.scheduleTask(async, delay, interval, task)

    fun updateItems() = Core.scheduleTask(true) {
        privateUpdateItems(true)
    }

    private fun privateUpdateItems(updatePages: Boolean) {
        if (updatePages) pages = (item.list.size / item.slots.size) + 1
        val fs = (0..<inventory.size).toMutableSet()
        ButtonManager(this).manageDisplay(fs)
        fillerSlots = fs
        filler.manageDisplay(this, fillerSlots)
        if (listMap != null) manageListDisplay()
    }

    @Suppress("DuplicatedCode")
    private fun processItems() =
        this::class.members.filter { it.hasAnnotation<MenuItem>() && it.parameters.size == 1 }
            .forEach { member ->
                when (val result = member.call(this)) {
                    is Button -> buttons.add(result)
                    is Iterable<*> -> {
                        result.forEach {
                            when (it) {
                                is Button -> buttons.add(it)
                            }
                        }
                    }
                }
            }

    private fun makeListMap() {
        listMap = item.run {
            list.indices.map {
                it to slots.elementAt(it % slots.size)
            }.groupBy {
                it.first / slots.size
            }
        }
    }

    private fun manageListDisplay() {
        val map = item.run {
            slots - listMap!!.getValue(page).map {
                inventory.setItem(it.second, display(list[it.first]).toItem())
                it.second
            }.toSet()
        }
        map.forEach { inventory.setItem(it, listFiller.display.toItem()) }
    }

    fun manageBehaviour(event: InventoryClickEvent) {
        filler.manageBehavior(fillerSlots, event)
        if (event.slot !in item.slots) return
        event.isCancelled = true
        if (ButtonAction.entries.none { it.clickType == event.click }) return
        if (event.currentItem?.type == filler.display.material) {
            filler.interactAction(Interacted(event))
            return
        }
        val (index) = listMap!!.getValue(page).first { it.second == event.slot }
        item.interactAction(Interacted(event), item.list[index])
    }

    override fun getInventory(): Inventory {
        return inventory
    }
}