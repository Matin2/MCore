package me.matin.core.managers

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation
import me.matin.core.Core
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class HandMovementManager(private val player: Player) {

    fun swingHand(mainHand: Boolean) {
        val animationType = if (mainHand) WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM else WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_OFF_HAND
        val animation = WrapperPlayServerEntityAnimation(player.entityId, animationType)
        for (target in getTargetPlayers()) {
            PacketEvents.getAPI().playerManager.sendPacket(target, animation)
        }
    }

    private fun getTargetPlayers(): Collection<Player> {
        val players: MutableCollection<Player> = HashSet()
        players.add(player)
        var range: Int = Core.corePlayerTrackingRange.getOrDefault(player.location.world, 64)
        range *= range
        for (p in Bukkit.getOnlinePlayers()) {
            if (p.location.world == player.location.world && (p.location.distanceSquared(player.location) <= range))
                players.add(p)
        }
        return players
    }
}