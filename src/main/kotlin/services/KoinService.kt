package com.github.matin2.mcore.services

import com.github.matin2.mcore.services.plugin.Hook
import com.github.matin2.mcore.services.plugin.KotlinPlugin
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.binds
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.reflect.KClass

internal object KoinService {
	
	val koins: Map<String, KoinApplication>
		field = mutableMapOf()
	
	fun new(plugin: KotlinPlugin, clazz: KClass<out KotlinPlugin>, config: KoinConfiguration): Koin {
		val app = koinApplication {
			modules(module { single { plugin } binds [KotlinPlugin::class, clazz] })
			config()()
		}
		koins[requireNotNull(clazz::class.qualifiedName) { "The plugin class is invalid" }] = app
		return app.koin
	}
	
	fun close(clazz: KClass<out KotlinPlugin>): Unit = koins.remove(clazz.qualifiedName ?: return)?.close() ?: return
}

fun koinOf(pluginMainClassReference: String): Koin = requireNotNull(KoinService.koins[pluginMainClassReference]?.koin) {
	"There is no koin registered for plugin with $pluginMainClassReference main class"
}

inline fun <reified Plugin : KotlinPlugin> koinOf(): Koin =
	koinOf(requireNotNull(Plugin::class.qualifiedName) { "The plugin class is invalid" })

inline fun <reified T : Hook> Module.hook(
	qualifier: Qualifier? = null,
	createdAtStart: Boolean = false,
	crossinline definition: Scope.(ParametersHolder) -> T
) = single(qualifier, createdAtStart) { definition(it).apply { hookTo(get()) } }
