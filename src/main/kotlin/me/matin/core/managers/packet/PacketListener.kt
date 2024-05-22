package me.matin.core.managers.packet

import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.HumanoidArm
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientSettings
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class PacketListener : PacketListenerAbstract(PacketListenerPriority.NORMAL), Listener {

    private val playerHand = HashMap<UUID, HumanoidArm>()

    override fun onPacketReceive(event: PacketReceiveEvent) {
        when (event.packetType) {
            PacketType.Play.Client.CLIENT_SETTINGS -> {
                val hand = WrapperPlayClientSettings(event).mainHand
                val player = Bukkit.getServer().getPlayer(event.user.uuid) ?: return
                player.saveClientHand(hand)
            }
            PacketType.Configuration.Client.CLIENT_SETTINGS -> {
                playerHand[event.user.uuid] = WrapperConfigClientSettings(event).hand
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val id = event.player.uniqueId
        if (playerHand.containsKey(id)) {
            event.player.saveClientHand(playerHand[id]!!)
            playerHand.remove(id)
        }
    }

    private fun Player.saveClientHand(hand: HumanoidArm) {
        persistentDataContainer.set(
            NamespacedKey("mcore", "left_handed"),
            PersistentDataType.BOOLEAN,
            hand == HumanoidArm.LEFT
        )
    }
}