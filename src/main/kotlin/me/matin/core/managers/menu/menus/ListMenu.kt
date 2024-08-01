package me.matin.core.managers.menu.menus

import me.matin.core.managers.menu.handlers.ListMenuHandler
import me.matin.core.managers.menu.items.other.MenuList
import kotlin.properties.Delegates

@Suppress("MemberVisibilityCanBePrivate")
abstract class ListMenu<T>(page: Int = 0): Menu() {

    abstract val list: MenuList<T>
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