package me.matin.core.managers.menu.menus

import me.matin.core.managers.menu.handlers.ListMenuHandler
import me.matin.core.managers.menu.items.other.MenuList
import net.kyori.adventure.text.Component
import kotlin.properties.Delegates

/**
 * Class for creating paged menus containing a list.
 *
 * @param T Type of the list of the menu.
 * @param title Title of the menu.
 * @param page (Optional) Page that the menu opens on.
 * @param freezeBottomInv (Optional) Whether to freeze the bottom(player)
 *    inventory or not.
 * @param preventCursorLoss (Optional) Whether to prevent deletion of the
 *    item on the cursor or not.
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class ListMenu<T>(
    title: Component,
    page: Int = 0,
    freezeBottomInv: Boolean = false,
    preventCursorLoss: Boolean = true
): Menu(title, freezeBottomInv, preventCursorLoss) {

    /** List of the menu. */
    abstract val list: MenuList<T>
    override val handler: ListMenuHandler<T> by lazy { ListMenuHandler(this) }
    private var _pages: Int = 0

    /** Number of pages this menu has. */
    val pages get() = _pages

    /** Get or change the current page of the menu */
    var page: Int by Delegates.vetoable(page) { _, _, newValue ->
        updatePages()
        if (newValue in 0..<pages) {
            handler.updatePage()
            true
        } else false
    }

    internal fun updatePages() {
        _pages = (list.list.size / list.slots.size) + 1
    }
}