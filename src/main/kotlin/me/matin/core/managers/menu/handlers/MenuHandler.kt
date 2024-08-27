package me.matin.core.managers.menu.handlers

import me.matin.core.managers.menu.items.button.Button
import me.matin.core.managers.menu.items.button.ButtonManager
import me.matin.core.managers.menu.items.other.Filler
import me.matin.core.managers.menu.items.slot.ItemDeleteReason
import me.matin.core.managers.menu.items.slot.Slot
import me.matin.core.managers.menu.items.slot.SlotManager
import me.matin.core.managers.menu.menus.Menu
import me.matin.core.managers.menu.utils.MenuScheduler
import me.matin.core.methods.schedule
import me.matin.mlib.lazyApply
import me.matin.mlib.nullable
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import kotlin.time.Duration

@Suppress("unused")
open class MenuHandler(open val menu: Menu): InventoryHolder {

    private lateinit var inventory: Inventory
    private val buttonManager = lazy { ButtonManager() }
    private val slotManager = lazy { SlotManager() }
    private val fillerManager by lazy { Filler.Manager() }
    private val scheduler = lazy { MenuScheduler() }

    open fun open() {
        createInventory()
        updateItems(true, (0..<inventory.size).toMutableSet())
        var open = false
        schedule {
            menu.player.openInventory(inventory)
            open = true
        }
        while (!open) runCatching { Thread.sleep(10) }.onFailure { return }
        scheduler.lazyApply { onOpen() }
    }

    private fun createInventory() {
        inventory = menu.type.type?.let { Bukkit.createInventory(this, it, menu.title) } ?: let {
            Bukkit.createInventory(this, menu.type.rows!!.coerceIn(1, 6) * 9, menu.title)
        }
    }

    fun close(closeInventory: Boolean = true) {
        if (closeInventory) menu.player.closeInventory()
        schedule(true) {
            scheduler.lazyApply { onClose() }
            slotManager.nullable?.slots?.forEach { slot ->
                val item = slot.item ?: return@forEach
                slot.itemDeleteAction?.also { action ->
                    schedule { action(item, ItemDeleteReason.MENU_CLOSED) }
                } ?: run { menu.player.inventory.addItem(item) }
            }
        }
    }

    fun scheduleTask(async: Boolean, delay: Duration, interval: Duration, task: () -> Unit) {
        scheduler.value.schedule(async, delay, interval, task)
    }

    open fun manageBehavior(event: InventoryInteractEvent) {
        slotManager.lazyApply { manageBehavior(event) }
        if (event !is InventoryClickEvent) return
        buttonManager.lazyApply { manageBehavior(event) }
        fillerManager.manageBehavior(event, menu.filler)
    }

    open fun updateItems(useDefaultItem: Boolean, fillerSlots: MutableSet<Int>) {
        buttonManager.lazyApply { manageDisplay(inventory, fillerSlots) }
        slotManager.lazyApply { manageDisplay(inventory, fillerSlots, useDefaultItem) }
        fillerManager.apply {
            slots = fillerSlots
            manageDisplay(inventory, menu.filler)
        }
    }

    fun addButton(button: Button) = buttonManager.value.buttons.add(button)
    fun addSlot(slot: Slot) = slotManager.value.slots.add(slot)

    override fun getInventory(): Inventory {
        return inventory
    }
}