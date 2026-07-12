@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.github.matin2.mcore.methods.utils

import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.NamespacedKey
import org.bukkit.block.TileState
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.annotations.ApiStatus
import kotlin.reflect.KType
import kotlin.reflect.typeOf

val persistentDataTypeMapper: Map<KType, PersistentDataType<out Any, out Any>>
	field : MutableMap<KType, PersistentDataType<out Any, out Any>> = mutableMapOf(
		typeOf<Byte>() to PersistentDataType.BYTE,
		typeOf<Short>() to PersistentDataType.SHORT,
		typeOf<Int>() to PersistentDataType.INTEGER,
		typeOf<Long>() to PersistentDataType.LONG,
		typeOf<Float>() to PersistentDataType.FLOAT,
		typeOf<Double>() to PersistentDataType.DOUBLE,
		typeOf<Boolean>() to PersistentDataType.BOOLEAN,
		typeOf<String>() to PersistentDataType.STRING,
		typeOf<ByteArray>() to PersistentDataType.BYTE_ARRAY,
		typeOf<IntArray>() to PersistentDataType.INTEGER_ARRAY,
		typeOf<LongArray>() to PersistentDataType.LONG_ARRAY,
		typeOf<PersistentDataContainer>() to PersistentDataType.TAG_CONTAINER,
		typeOf<List<Byte>>() to PersistentDataType.LIST.bytes(),
		typeOf<List<Short>>() to PersistentDataType.LIST.shorts(),
		typeOf<List<Int>>() to PersistentDataType.LIST.integers(),
		typeOf<List<Long>>() to PersistentDataType.LIST.longs(),
		typeOf<List<Float>>() to PersistentDataType.LIST.floats(),
		typeOf<List<Double>>() to PersistentDataType.LIST.doubles(),
		typeOf<List<Boolean>>() to PersistentDataType.LIST.booleans(),
		typeOf<List<String>>() to PersistentDataType.LIST.strings(),
		typeOf<List<ByteArray>>() to PersistentDataType.LIST.byteArrays(),
		typeOf<List<IntArray>>() to PersistentDataType.LIST.integerArrays(),
		typeOf<List<LongArray>>() to PersistentDataType.LIST.longArrays(),
		typeOf<List<PersistentDataContainer>>() to PersistentDataType.LIST.dataContainers(),
	)

@ApiStatus.Internal
fun registerPersistentDataType(type: PersistentDataType<out Any, out Any>, kType: KType) {
	persistentDataTypeMapper[kType] = type
}

inline fun <reified T : Any> registerPersistentDataType(type: PersistentDataType<out Any, T>) =
	registerPersistentDataType(type, typeOf<T>())

inline fun <reified P : Any, reified C : Any> registerPersistentDataType(
	crossinline wrap: PersistentDataAdapterContext.(C) -> P,
	crossinline unwrap: PersistentDataAdapterContext.(P) -> C
) = object : PersistentDataType<P, C> {
	override fun getPrimitiveType() = P::class.java
	override fun getComplexType() = C::class.java
	override fun toPrimitive(complex: C, context: PersistentDataAdapterContext) = wrap(context, complex)
	override fun fromPrimitive(primitive: P, context: PersistentDataAdapterContext) = unwrap(context, primitive)
}.also { registerPersistentDataType(it) }

inline fun ItemStack.container(crossinline block: PersistentDataContainer.() -> Unit) =
	editPersistentDataContainer { block(it) }

inline fun PersistentDataHolder.container(block: PersistentDataContainer.() -> Unit) =
	persistentDataContainer.run(block)

inline fun TileState.container(block: PersistentDataContainer.() -> Unit) {
	persistentDataContainer.run(block)
	update()
}

inline fun PersistentDataContainer.addContainer(key: NamespacedKey, block: PersistentDataContainer.() -> Unit) {
	set(key, PersistentDataType.TAG_CONTAINER, adapterContext.newPersistentDataContainer().apply(block))
}

inline fun PersistentDataContainerView.getContainer(key: NamespacedKey) = get(key, PersistentDataType.TAG_CONTAINER)

@Suppress("UNCHECKED_CAST")
inline operator fun <reified V : Any> PersistentDataContainer.set(key: NamespacedKey, value: V) =
	set(key, persistentDataTypeMapper[typeOf<V>()] as PersistentDataType<Any, V>, value)

@Suppress("UNCHECKED_CAST")
inline operator fun <reified V : Any> PersistentDataContainerView.get(key: NamespacedKey) =
	get(key, persistentDataTypeMapper[typeOf<V>()] as PersistentDataType<Any, V>)

@Suppress("UNCHECKED_CAST")
inline fun <reified V : Any> PersistentDataContainerView.getOrDefault(key: NamespacedKey, default: V) =
	getOrDefault(key, persistentDataTypeMapper[typeOf<V>()] as PersistentDataType<Any, V>, default)

@Suppress("UNCHECKED_CAST")
inline fun <reified V : Any> PersistentDataContainerView.has(key: NamespacedKey) =
	has(key, persistentDataTypeMapper[typeOf<V>()] as PersistentDataType<Any, V>)

inline operator fun PersistentDataContainerView.contains(key: NamespacedKey) = has(key)
