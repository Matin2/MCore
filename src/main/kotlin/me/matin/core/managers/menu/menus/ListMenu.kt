package me.matin.core.managers.menu.menus

import me.matin.core.managers.menu.MenuType
import me.matin.core.managers.menu.handlers.ListMenuHandler
import me.matin.core.managers.menu.items.other.Filler
import me.matin.core.managers.menu.items.other.MenuList
import net.kyori.adventure.text.Component
import kotlin.properties.Delegates

@Suppress("MemberVisibilityCanBePrivate")
open class ListMenu<T>(
    title: Component,
    type: MenuType,
    val list: MenuList<T>,
    page: Int = 0,
    filler: Filler = Filler(),
    freezeBottomInv: Boolean = false,
    preventCursorLoss: Boolean = true
): Menu(title, type, filler, freezeBottomInv, preventCursorLoss) {

    override val handler: ListMenuHandler<T> by lazy { ListMenuHandler(this) }
    private var _pages: Int = 0
    val pages get() = _pages
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