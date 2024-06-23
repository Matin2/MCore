package me.matin.core.managers

import io.papermc.paper.persistence.PersistentDataContainerView
import io.papermc.paper.persistence.PersistentDataViewHolder
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType

@Suppress("unused")
object PDCManager {

    val PersistentDataHolder.PDC: PersistentDataContainer
        get() = this.persistentDataContainer

    val PersistentDataViewHolder.PDC: PersistentDataContainerView
        get() = this.persistentDataContainer

    operator fun PersistentDataContainerView.unaryPlus(): PersistentDataContainer {
        return this.adapterContext.newPersistentDataContainer()
    }

    operator fun PersistentDataContainer.unaryMinus(): PersistentDataContainerView = this

    operator fun <P, C: Any> PersistentDataContainerView.get(key: String, type: PersistentDataType<P, C>, def: C? = null): C? {
        def ?: return this.get(NamespacedKey.fromString(key) ?: return null, type)
        return this.getOrDefault(NamespacedKey.fromString(key) ?: return null, type, def)
    }

    operator fun <P, C: Any> PersistentDataContainerView.get(key: NamespacedKey, type: PersistentDataType<P, C>, def: C? = null): C? {
        def ?: return this.get(key, type)
        return this.getOrDefault(key, type, def)
    }

    operator fun <P, C: Any> PersistentDataContainer.set(key: String, type: PersistentDataType<P, C>, value: C) {
        this.set(NamespacedKey.fromString(key) ?: return, type, value)
    }

    infix operator fun PersistentDataContainer.minus(key: String): PersistentDataContainer {
        this.remove(NamespacedKey.fromString(key) ?: return this)
        return this
    }

    infix operator fun PersistentDataContainer.minus(key: NamespacedKey) {
        this.remove(key)
    }

    infix operator fun PersistentDataContainer.plus(other: PersistentDataContainerView): PersistentDataContainer {
        other.copyTo(this, false)
        return this
    }

    infix operator fun PersistentDataContainer.times(other: PersistentDataContainerView): PersistentDataContainer {
        other.copyTo(this, true)
        return this
    }
}