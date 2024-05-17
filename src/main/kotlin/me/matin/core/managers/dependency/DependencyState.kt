package me.matin.core.managers.dependency

enum class DependencyState(val value: Boolean) {
    INSTALLED(true),
    NOT_INSTALLED(false),
    WRONG_VERSION(false),
}