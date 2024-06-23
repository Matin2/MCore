package me.matin.core.managers.menu

import org.bukkit.entity.Player

open class PlayerMenuUtil(open var player: Player) {
    companion object {

        private var playerMenuUtilMap: HashMap<Player, PlayerMenuUtil> = HashMap()

        @JvmStatic
        operator fun get(player: Player): PlayerMenuUtil {
            if (player !in playerMenuUtilMap) {
                val playerMenuUtil = PlayerMenuUtil(player)
                playerMenuUtilMap[player] = playerMenuUtil
                return playerMenuUtil
            }
            return playerMenuUtilMap[player]!!
        }
    }
}