package me.matin.core.managers.menu.menus

import me.matin.core.managers.menu.handlers.ListMenuHandler
import me.matin.core.managers.menu.items.other.MenuList
import kotlin.properties.Delegates

@Suppress("MemberVisibilityCanBePrivate")
abstract class ListMenu<T>(page: Int): Menu() {

    abstract val list: MenuList<T>
    override val handler: ListMenuHandler<T> by lazy { ListMenuHandler(this) }
    val pages get() = handler.pages
    var page: Int by Delegates.vetoable(page) { _, _, newValue ->
        if (newValue in 0..<pages) {
            handler.page = newValue
            true
        } else false
    }
}