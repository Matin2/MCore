package me.matin.core.managers.menu.handlers

import me.matin.core.managers.schedule
import me.matin.core.managers.menu.items.button.Button
import me.matin.core.managers.menu.items.button.ButtonManager
import me.matin.core.managers.menu.items.other.Filler
import me.matin.core.managers.menu.items.slot.ItemDeleteReason
import me.matin.core.managers.menu.items.slot.Slot
import me.matin.core.managers.menu.items.slot.SlotManager
import me.matin.core.managers.menu.menus.Menu
import me.matin.core.managers.menu.utils.MenuScheduler
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import kotlin.time.Duration

@Suppress("unused")
open class MenuHandler(open val menu: Menu): InventoryHolder {

    private lateinit var inventory: Inventory
    private val buttonManager: ButtonManager = ButtonManager()
    private val slotManager: SlotManager = SlotManager()
    private val fillerManager: Filler.Manager = Filler.Manager()
    private val scheduler = MenuScheduler()

    open fun open() {
        createInventory()
        updateItems(true, (0..<inventory.size).toMutableSet())
        var open = false
        schedule {
            menu.player.openInventory(inventory)
            open = true
        }
        while (!open) runCatching { Thread.sleep(10) }.onFailure { return }
        scheduler.onOpen()
    }

    private fun createInventory() {
        inventory = menu.type.type?.let { Bukkit.createInventory(this, it, menu.title) } ?: let {
            Bukkit.createInventory(this, maxOf(minOf(menu.type.rows!!, 6), 1) * 9, menu.title)
        }
    }

    fun close(closeInventory: Boolean = true) {
        if (closeInventory) menu.player.closeInventory()
        schedule(true) {
            scheduler.onClose()
            slotManager.slots.forEach { slot ->
                val item = slot.item ?: return@forEach
                slot.itemDeleteAction?.also { action ->
                    schedule { action(item, ItemDeleteReason.MENU_CLOSED) }
                } ?: run { menu.player.inventory.addItem(item) }
            }
        }
    }

    fun scheduleTask(async: Boolean, delay: Duration, interval: Duration, task: () -> Unit) {
        scheduler.schedule(async, delay, interval, task)
    }

    fun publicUpdateItems() {
        updateItems(false, (0..<inventory.size).toMutableSet())
    }

    open fun manageBehavior(event: InventoryInteractEvent) {
        slotManager.manageBehavior(event)
        if (event !is InventoryClickEvent) return
        buttonManager.manageBehavior(event)
        fillerManager.manageBehavior(event, menu.filler)
    }

    open fun updateItems(useDefaultItem: Boolean, fillerSlots: MutableSet<Int>) {
        buttonManager.manageDisplay(inventory, fillerSlots)
        slotManager.manageDisplay(inventory, fillerSlots, useDefaultItem)
        fillerManager.apply {
            slots = fillerSlots
            manageDisplay(inventory, menu.filler)
        }
    }

    fun addButton(button: Button) = buttonManager.buttons.add(button)
    fun addSlot(slot: Slot) = slotManager.slots.add(slot)

    override fun getInventory(): Inventory {
        return inventory
    }
}