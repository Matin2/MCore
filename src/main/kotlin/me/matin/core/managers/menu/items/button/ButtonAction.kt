package me.matin.core.managers.menu.items.button

import org.bukkit.event.inventory.ClickType

enum class ButtonAction(val clickType: ClickType, val hotbar: Int?) {

    LEFT(ClickType.LEFT, null),
    SHIFT_LEFT(ClickType.SHIFT_LEFT, null),
    RIGHT(ClickType.RIGHT, null),
    SHIFT_RIGHT(ClickType.SHIFT_RIGHT, null),
    MIDDLE(ClickType.MIDDLE, null),
    NUMBER_KEY_1(ClickType.NUMBER_KEY, 0),
    NUMBER_KEY_2(ClickType.NUMBER_KEY, 1),
    NUMBER_KEY_3(ClickType.NUMBER_KEY, 2),
    NUMBER_KEY_4(ClickType.NUMBER_KEY, 3),
    NUMBER_KEY_5(ClickType.NUMBER_KEY, 4),
    NUMBER_KEY_6(ClickType.NUMBER_KEY, 5),
    NUMBER_KEY_7(ClickType.NUMBER_KEY, 6),
    NUMBER_KEY_8(ClickType.NUMBER_KEY, 7),
    NUMBER_KEY_9(ClickType.NUMBER_KEY, 8),
    DOUBLE_CLICK(ClickType.DOUBLE_CLICK, null),
    DROP(ClickType.DROP, null),
    CONTROL_DROP(ClickType.CONTROL_DROP, null),
    SWAP_OFFHAND(ClickType.SWAP_OFFHAND, null),
}