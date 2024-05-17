package me.matin.core.managers.packet

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.HumanoidArm
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings
import de.tr7zw.changeme.nbtapi.NBTEntity
import org.bukkit.entity.Player

class PacketListener: PacketListener {

    override fun onPacketReceive(event: PacketReceiveEvent) {
        when (event.packetType) {
            PacketType.Play.Client.CLIENT_SETTINGS -> event.saveClientHand()
        }
    }

    private fun PacketReceiveEvent.saveClientHand() {
        return WrapperPlayClientSettings(this).let { packet ->
            NBTEntity(this.player as Player).setBoolean("left_handed", packet.mainHand == HumanoidArm.LEFT)
        }
    }
}