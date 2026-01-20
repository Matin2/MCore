package me.matin.mcore.managers.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import me.matin.mcore.managers.dialog.scope.*
import net.kyori.adventure.text.Component

@JvmInline
@Suppress("unused", "UnstableApiUsage")
value class DialogTemplate<S : DialogScope> private constructor(private val scope: S) {
	
	fun modify(title: Component? = null, block: S.() -> Unit) = apply {
		title?.let { scope.titleProperty = title }
		scope.block()
	}
	
	infix fun applyTo(builder: DialogRegistryEntry.Builder): DialogRegistryEntry.Builder =
		builder.type(scope.type).base(scope.base)
	
	inline val dialog: Dialog get() = Dialog.create { applyTo(it.empty()) }
	
	companion object {
		
		@JvmStatic
		fun notice(title: Component, block: NoticeDialogScope.() -> Unit) =
			DialogTemplate(NoticeDialogScope(title).apply(block))
		
		@JvmStatic
		fun confirmation(title: Component, block: ConfirmationDialogScope.() -> Unit) =
			DialogTemplate(ConfirmationDialogScope(title).apply(block))
		
		@JvmStatic
		fun serverLinks(title: Component, block: ServerLinksDialogScope.() -> Unit) =
			DialogTemplate(ServerLinksDialogScope(title).apply(block))
		
		@JvmStatic
		fun dialogList(title: Component, block: ListDialogScope.() -> Unit) =
			DialogTemplate(ListDialogScope(title).apply(block))
		
		@JvmStatic
		fun multiAction(title: Component, block: MultiActionDialogScope.() -> Unit) =
			DialogTemplate(MultiActionDialogScope(title).apply(block))
		
		@JvmStatic
		@Suppress("NOTHING_TO_INLINE")
		inline infix fun DialogRegistryEntry.Builder.applyTemplate(template: DialogTemplate<*>) =
			template.applyTo(this)
	}
}
