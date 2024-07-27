package me.matin.core.managers.menu.menus

import me.matin.core.managers.TaskManager
import me.matin.core.managers.menu.InventoryMenu
import me.matin.core.managers.menu.items.MenuItem
import me.matin.core.managers.menu.items.button.Button
import me.matin.core.managers.menu.items.button.ButtonAction
import me.matin.core.managers.menu.items.button.ButtonManager
import me.matin.core.managers.menu.utils.DisplayItem
import me.matin.core.managers.menu.utils.MenuUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.reflect.full.hasAnnotation

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class ListMenu<T>(private val player: Player, page: Int = 0): InventoryMenu() {

    private lateinit var inventory: Inventory
    val buttonsMap = mutableMapOf<Button, Set<Int>?>()
    override val buttons get() = buttonsMap.keys
    private var opened: Boolean = false
    private val util = MenuUtils()
    abstract val listSlots: Set<Int>
    abstract val list: List<T>
    abstract val listDisplay: (T) -> DisplayItem
    abstract val listInteractAction: Interacted.(T) -> Unit
    abstract val listFiller: Pair<DisplayItem, Interacted.() -> Unit>
    private var listMap: Map<Int, List<Pair<Int, Int>>>? = null
    private var pages: Int = 0
    var page: Int = page
        set(value) {
            field = when {
                value < 0 -> (value % pages) + pages
                value >= pages -> value % pages
                else -> value
            }
            TaskManager.runTask(true) {
                ButtonManager(this).manageDisplay()
                if (listMap != null) manageListDisplay()
            }
        }

    private fun makeListMap() {
        listMap = list.indices.map {
            it to listSlots.elementAt(it % listSlots.size)
        }.groupBy {
            it.first / listSlots.size
        }
    }

    fun open() = TaskManager.runTask(true) {
        type.type?.also {
            inventory = Bukkit.createInventory(this, it, title)
        } ?: run {
            inventory = Bukkit.createInventory(this, type.rows!! * 9, title)
        }
        pages = list.size / listSlots.size
        makeListMap()
        processItems()
        ButtonManager(this).manageDisplay()
        if (listMap != null) manageListDisplay()
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

    private fun processItems() {
        this::class.members.filter { it.hasAnnotation<MenuItem>() && it.parameters.size == 1 }
            .forEach { member ->
                val pages =
                    member.annotations.filterIsInstance<MenuItem>().first().pages.takeIf { it.isNotEmpty() }?.toSet()
                when (val result = member.call(this)) {
                    is Button -> buttonsMap[result] = pages
                    is Iterable<*> -> {
                        result.forEach {
                            when (it) {
                                is Button -> buttonsMap[it] = pages
                            }
                        }
                    }
                }
            }
    }

    private fun manageListDisplay() {
        val map = listSlots - listMap!!.getValue(page).map {
            inventory.setItem(it.second, listDisplay(list[it.first]).toItem())
            it.second
        }.toSet()
        map.forEach { inventory.setItem(it, listFiller.first.toItem()) }
    }

    fun manageBehaviour(event: InventoryClickEvent) {
        if (event.slot !in listSlots) return
        event.isCancelled = true
        if (ButtonAction.entries.none { it.clickType == event.click }) return
        if (event.currentItem?.type == listFiller.first.material) {
            listFiller.second(Interacted(event))
            return
        }
        val (index) = listMap!!.getValue(page).first { it.second == event.slot }
        listInteractAction(Interacted(event), list[index])
    }

    override fun getInventory(): Inventory {
        return inventory
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate", "DEPRECATION")
    class Interacted(private val event: InventoryClickEvent) {

        val view = event.view
        val slot = event.slot
        val action: ButtonAction by lazy {
            if (event.click == ClickType.NUMBER_KEY) ButtonAction.entries.first { it.hotbar == event.hotbarButton }
            else ButtonAction.entries.first { it.clickType == event.click }
        }
        var cursor: ItemStack = event.cursor
            set(value) {
                event.setCursor(value)
            }
    }
}