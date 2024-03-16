package me.matin.core.menu

import org.bukkit.entity.Player

class PlayerMenuUtility(private var owner: Player) {

    private var playerMenuUtilityMap : HashMap<Player, PlayerMenuUtility> = HashMap()

    fun getOwner(): Player {
        return owner
    }

    fun setOwner(owner: Player) {
        this.owner = owner
    }

    fun getPlayerMenuUtility(player: Player): PlayerMenuUtility {
        if (player in playerMenuUtilityMap) {
            var playerMenuUtility = playerMenuUtilityMap[player]
            if (playerMenuUtility == null) {
                playerMenuUtility = PlayerMenuUtility(player)
                playerMenuUtilityMap[player] = playerMenuUtility
            }
            return playerMenuUtility
        } else {
            val playerMenuUtility = PlayerMenuUtility(player)
            playerMenuUtilityMap[player] = playerMenuUtility
            return playerMenuUtility
        }
    }
}