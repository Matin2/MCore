package me.matin.core.menu

import org.bukkit.entity.Player

class PlayerMenuUtility {

    open class MenuUtility(var owner: Player)

    companion object {

        private var playerMenuUtilityMap: HashMap<Player, MenuUtility> = HashMap()

        @JvmStatic
        fun get(player: Player): MenuUtility {
            if (player in playerMenuUtilityMap) {
                var playerMenuUtility = playerMenuUtilityMap[player]
                if (playerMenuUtility == null) {
                    playerMenuUtility = MenuUtility(player)
                    playerMenuUtilityMap[player] = playerMenuUtility
                }
                return playerMenuUtility
            } else {
                val playerMenuUtility = MenuUtility(player)
                playerMenuUtilityMap[player] = playerMenuUtility
                return playerMenuUtility
            }
        }
    }
}