package com.github.matin2.mcore.services

import com.github.matin2.mcore.services.plugin.Hook
import com.github.matin2.mcore.services.plugin.KotlinPlugin
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.binds
import org.koin.dsl.koinApplication
import org.koin.dsl.module

object KoinService {
	
	val koins: Map<String, KoinApplication>
		field = mutableMapOf()
	
	internal inline fun <reified Plugin : KotlinPlugin> new(
		plugin: Plugin,
		config: KoinConfiguration
	): Koin {
		val app = koinApplication {
			loadKoinModules(module { single { plugin } binds [KotlinPlugin::class, Plugin::class] })
			config()()
		}
		koins[requireNotNull(Plugin::class.qualifiedName) { "The plugin class is invalid" }] = app
		return app.koin
	}
	
	internal inline fun <reified Plugin : KotlinPlugin> close(): Unit =
		koins.remove(Plugin::class.qualifiedName ?: return)?.close() ?: return
}

inline fun <reified Plugin : KotlinPlugin> koinOf(): Koin = requireNotNull(
	KoinService.koins[requireNotNull(Plugin::class.qualifiedName) { "The plugin class is invalid" }]?.koin
) { "There is no koin registered for ${Plugin::class.simpleName}" }

inline fun <reified T : Hook> Module.hook(
	qualifier: Qualifier? = null,
	createdAtStart: Boolean = false,
	crossinline definition: Scope.(ParametersHolder) -> T
) = single(qualifier, createdAtStart) { definition(it).apply { hookTo(get()) } }
