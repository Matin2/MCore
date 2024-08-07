package me.matin.core.managers.menu.menus

import me.matin.core.Core
import me.matin.core.managers.PacketManager
import me.matin.core.managers.menu.MenuType
import me.matin.core.managers.menu.handlers.MenuHandler
import me.matin.core.managers.menu.items.MenuItem
import me.matin.core.managers.menu.items.button.Button
import me.matin.core.managers.menu.items.other.Filler
import me.matin.core.managers.menu.items.slot.Slot
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import kotlin.reflect.full.hasAnnotation
import kotlin.time.Duration

/**
 * Class for creating menus.
 *
 * @param title Title of the menu.
 * @property player Player witch the menu opens for.
 * @property type Type of the menu.
 * @property filler (Optional) Filler of the empty slots in the menu.
 * @property freezeBottomInv (Optional) Whether to freeze the
 *    bottom(player) inventory or not.
 * @property preventCursorLoss (Optional) Whether to prevent deletion of
 *    the item on the cursor or not.
 */
@Suppress("unused")
open class Menu(
    open val player: Player,
    title: Component,
    val type: MenuType,
    val filler: Filler = Filler(),
    val freezeBottomInv: Boolean = false,
    val preventCursorLoss: Boolean = true
) {

    /** Get or change the title of the menu. */
    var title: Component = title
        set(value) {
            field = value
            PacketManager.changeInvTitle(player, value)
        }
    internal open val handler by lazy { MenuHandler(this) }

    /** Opens the menu for the player. */
    fun open() {
        Core.scheduleTask(true) {
            processItems()
            handler.open()
        }
    }

    /** Closes the menu for the player. */
    fun close() = handler.close()

    /**
     * Schedules a task to run while the menu is open.
     *
     * @param async Whether to run the task async or not.
     * @param delay The task will run after this duration has passed.
     * @param interval The task will be repeated by this interval.
     * @param task The task to be scheduled.
     */
    fun scheduleTask(
        async: Boolean = false,
        delay: Duration = Duration.ZERO,
        interval: Duration = Duration.ZERO,
        task: () -> Unit
    ) = handler.scheduleTask(async, delay, interval, task)

    /** Updates all the items on the menu in case they have changed. */
    fun updateItems() = handler.updateItems(false, (0..<handler.inventory.size).toMutableSet())

    private fun processItems() {
        this::class.members.filter { it.hasAnnotation<MenuItem>() && it.parameters.size == 1 }.forEach { member ->
            when (val result = member.call(this)) {
                is Button -> handler.addButton(result)
                is Slot -> handler.addSlot(result)
                is Iterable<*> -> {
                    result.forEach {
                        when (it) {
                            is Button -> handler.addButton(it)
                            is Slot -> handler.addSlot(it)
                        }
                    }
                }
            }
        }
    }
}