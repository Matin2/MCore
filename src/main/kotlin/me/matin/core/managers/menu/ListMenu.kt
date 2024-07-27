package me.matin.core.managers.menu

import me.matin.core.managers.TaskManager
import me.matin.core.managers.menu.items.MenuItem
import me.matin.core.managers.menu.items.button.Button
import me.matin.core.managers.menu.items.button.ButtonAction
import me.matin.core.managers.menu.items.button.ButtonManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.reflect.full.hasAnnotation

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class ListMenu<T>(private val player: Player, page: Int = 0): InventoryMenu {

    private lateinit var inventory: Inventory
    val buttonsMap = mutableMapOf<Button, Set<Int>?>()
    override val buttons get() = buttonsMap.keys
    override val freezeBottomInv: Boolean = false
    override val preventCursorLoss: Boolean = true
    private var opened: Boolean = false
    private val util = MenuUtils()
    abstract val listSlots: Set<Int>
    abstract val list: Iterable<T>
    abstract val listDisplay: (T) -> DisplayItem
    abstract val listInteractAction: Interacted.(T) -> Unit
    private val listMap: Map<Int, List<Pair<Int, Int>>> by lazy {
        list.toList().indices.map {
            it to listSlots.elementAt(it % listSlots.size)
        }.groupBy {
            it.first / listSlots.size
        }
    }
    val pages = listMap.keys
    var page: Int = page
        set(value) {
            field = when {
                value < 0 -> (value % pages.size) + pages.size
                value > pages.last() -> value % pages.size
                else -> value
            }
            TaskManager.runTask(true) {
                ButtonManager(this).manageDisplay()
                listMap.getValue(field).forEach {
                    inventory.setItem(it.second, listDisplay(list.elementAt(it.first)).toItem())
                }
            }
        }

    fun open() = TaskManager.runTask(true) {
        type.type?.also {
            inventory = Bukkit.createInventory(this, it, title)
        } ?: run {
            inventory = Bukkit.createInventory(this, type.rows!! * 9, title)
        }
        processItems()
        manageListDisplay()
        ButtonManager(this).manageDisplay()
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

    private fun manageListDisplay() = listMap.getValue(page).forEach {
        inventory.setItem(it.second, listDisplay(list.elementAt(it.first)).toItem())
    }

    fun manageBehaviour(event: InventoryClickEvent) = TaskManager.runTask(true) {
        if (event.slot !in listSlots) return@runTask
        event.isCancelled = true
        if (ButtonAction.entries.none { it.clickType == event.click }) return@runTask
        val (index) = listMap.getValue(page).first { it.second == event.slot }
        TaskManager.runTask {
            listInteractAction(Interacted(event), list.elementAt(index))
        }
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