package me.matin.core.managers.menu.menus

import me.matin.core.Core
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

@Suppress("unused")
abstract class Menu {

    abstract val player: Player
    abstract val title: Component
    abstract val type: MenuType
    open val filler: Filler = Filler()
    open val freezeBottomInv: Boolean = false
    open val preventCursorLoss: Boolean = true
    internal open val handler by lazy { MenuHandler(this) }

    fun open() {
        Core.scheduleTask(true) {
            processItems()
            handler.open()
        }
    }

    fun close() = handler.close()

    fun scheduleTask(
        async: Boolean = false,
        delay: Duration = Duration.ZERO,
        interval: Duration = Duration.ZERO,
        task: () -> Unit
    ) = handler.scheduleTask(async, delay, interval, task)

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