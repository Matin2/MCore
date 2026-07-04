package com.github.matin2.mcore.managers.dialog

import com.github.matin2.mcore.managers.dialog.scope.*
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import net.kyori.adventure.text.Component

@JvmInline
@Suppress("unused", "UnstableApiUsage", "NOTHING_TO_INLINE")
value class VirtualDialog<Context : DialogContext> private constructor(private val context: Context) {
	
	fun modify(title: Component, block: Context.() -> Unit) = apply {
		context.initialTitle = title
		context.block()
	}
	
	fun modify(block: Context.() -> Unit) = apply { context.block() }
	
	infix fun applyTo(builder: DialogRegistryEntry.Builder): DialogRegistryEntry.Builder =
		builder.type(context.type).base(context.base)
	
	inline fun toDialog(): Dialog = Dialog.create { applyTo(it.empty()) }
	
	companion {
		fun notice(title: Component, block: NoticeDialogContext.() -> Unit) =
			VirtualDialog(NoticeDialogContext(title).apply(block))
		
		fun confirmation(title: Component, block: ConfirmationDialogContext.() -> Unit) =
			VirtualDialog(ConfirmationDialogContext(title).apply(block))
		
		fun serverLinks(title: Component, block: ServerLinksDialogContext.() -> Unit) =
			VirtualDialog(ServerLinksDialogContext(title).apply(block))
		
		fun dialogList(title: Component, block: ListDialogContext.() -> Unit) =
			VirtualDialog(ListDialogContext(title).apply(block))
		
		fun multiAction(title: Component, block: MultiActionDialogContext.() -> Unit) =
			VirtualDialog(MultiActionDialogContext(title).apply(block))
	}
}
