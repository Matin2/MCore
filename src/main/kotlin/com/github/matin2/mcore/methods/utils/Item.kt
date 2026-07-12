@file:Suppress("UnstableApiUsage", "NOTHING_TO_INLINE")

package com.github.matin2.mcore.methods.utils

import io.papermc.paper.datacomponent.DataComponentBuilder
import io.papermc.paper.datacomponent.DataComponentType
import org.bukkit.inventory.ItemStack

/**
 * Operator version of [setData].
 *
 * @param V value type
 * @param data data to change
 * @param value new value
 * @see setData
 */
inline operator fun <V : Any> ItemStack.set(data: DataComponentType.Valued<V>, value: V) = setData(data, value)

/**
 * Operator version of [setData].
 *
 * @param V value type
 * @param data data to change
 * @param builder new value
 * @see setData
 */
inline operator fun <V : Any> ItemStack.set(data: DataComponentType.Valued<V>, builder: DataComponentBuilder<V>) =
	setData(data, builder)

/**
 * Operator version of [getData].
 *
 * @param V value type
 * @param data data to get
 * @see getData
 */
inline operator fun <V : Any> ItemStack.get(data: DataComponentType.Valued<V>) = getData(data)
