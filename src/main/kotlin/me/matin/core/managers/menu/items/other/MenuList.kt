package me.matin.core.managers.menu.items.other

import me.matin.core.managers.menu.utils.DisplayItem

class MenuList<T>(
    val slots: Set<Int>,
    val list: List<T>,
    val display: (T) -> DisplayItem,
    val interactAction: Interacted.(T) -> Unit = {}
)
