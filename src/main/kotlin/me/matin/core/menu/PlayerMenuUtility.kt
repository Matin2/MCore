package me.matin.core.menu

import org.bukkit.entity.Player

open class PlayerMenuUtility(private var owner: Player) {

    fun getOwner(): Player {
        return owner
    }

    fun setOwner(owner: Player) {
        this.owner = owner
    }
}

class GetPlayerMenuUtility {

    private var playerMenuUtilityMap : HashMap<Player, PlayerMenuUtility> = HashMap()

    fun get(player: Player): PlayerMenuUtility {
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