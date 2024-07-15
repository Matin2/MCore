package me.matin.core.managers.menu

import org.bukkit.entity.Player

open class MenuUtil(open var player: Player) {
    companion object {

        private var menuUtilMap: HashMap<Player, MenuUtil> = HashMap()

        @JvmStatic
        operator fun get(player: Player): MenuUtil {
            if (player !in menuUtilMap) {
                val menuUtil = MenuUtil(player)
                menuUtilMap[player] = menuUtil
                return menuUtil
            }
            return menuUtilMap[player]!!
        }
    }
}