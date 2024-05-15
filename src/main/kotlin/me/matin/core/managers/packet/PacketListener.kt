package me.matin.core.managers.packet

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.HumanoidArm
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings
import org.bukkit.entity.Player

class PacketListener: PacketListener {

    companion object {
        val clientHandMap = HashMap<Player, HumanoidArm>()
    }

    override fun onPacketReceive(event: PacketReceiveEvent) {
        when (event.packetType) {
            PacketType.Play.Client.CLIENT_SETTINGS -> WrapperPlayClientSettings(event).let {
                clientHandMap[event.player as Player] = it.mainHand ?: HumanoidArm.RIGHT
            }
        }
    }
}