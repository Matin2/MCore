package me.matin.core.managers.packet

import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.HumanoidArm
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientSettings
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings
import me.matin.core.Core
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.*

class PacketListener : PacketListenerAbstract(PacketListenerPriority.NORMAL) {

    override fun onPacketReceive(event: PacketReceiveEvent) {
        when (event.packetType) {
            PacketType.Play.Client.CLIENT_SETTINGS -> {
                val hand = WrapperPlayClientSettings(event).mainHand
                val player = Bukkit.getServer().getPlayer(event.user.uuid) ?: return
                player.saveClientHand(hand)
            }
            PacketType.Configuration.Client.CLIENT_SETTINGS -> {
                val hand = WrapperConfigClientSettings(event).hand
                onJoin(event.user.uuid, hand)
            }
        }
    }

    private fun onJoin(playerID: UUID, hand: HumanoidArm) {
        Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, Runnable {
            var player = Bukkit.getServer().getPlayer(playerID)
            while (player == null) {
                Thread.sleep(1000)
                player = Bukkit.getServer().getPlayer(playerID)
            }
            player.saveClientHand(hand)
        })
    }

    private fun Player.saveClientHand(hand: HumanoidArm) {
        persistentDataContainer.set(
            NamespacedKey("mcore", "left_handed"),
            PersistentDataType.BOOLEAN,
            hand == HumanoidArm.LEFT
        )
    }
}