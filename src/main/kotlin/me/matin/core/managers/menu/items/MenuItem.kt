package me.matin.core.managers.menu.items

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class MenuItem(vararg val pages: Int = [])