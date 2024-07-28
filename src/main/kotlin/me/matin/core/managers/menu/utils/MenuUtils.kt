package me.matin.core.managers.menu.utils

import me.matin.core.managers.TaskManager
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

class MenuUtils {

    private val runningTasks = mutableSetOf<BukkitTask>()
    private val tasksToRun: MutableList<Triple<Pair<Duration, Duration>, Boolean, () -> Unit>> = mutableListOf()
    private var open = false

    fun scheduleOnOpen() {
        open = true
        for ((delayInterval, async, action) in tasksToRun) {
            val task = TaskManager.scheduleTask(delayInterval.first, delayInterval.second, async, action)
            runningTasks.add(task)
        }
        tasksToRun.removeAll { true }
    }

    fun removeTasks() {
        open = false
        runningTasks.forEach {
            it.cancel()
            runningTasks.remove(it)
        }
    }

    fun scheduleTask(
        delay: Duration = Duration.ZERO,
        interval: Duration = Duration.ZERO,
        async: Boolean = false,
        task: () -> Unit
    ) {
        if (open) {
            tasksToRun.add(Triple(delay to interval, async, task))
            return
        }
        val bukkitTask = TaskManager.scheduleTask(delay, interval, async, task)
        runningTasks.add(bukkitTask)
    }
}