package me.matin.mcore.managers

interface FloatProgression<T : Comparable<T>> : ClosedFloatingPointRange<T> {
	
	val step: T
	
	companion object {
		
		@JvmStatic
		@Suppress("NOTHING_TO_INLINE", "unused")
		inline infix fun <T : Comparable<T>> ClosedFloatingPointRange<T>.step(step: T) = object : FloatProgression<T> {
			override val step: T = step
			override fun lessThanOrEquals(a: T, b: T): Boolean = this@step.lessThanOrEquals(a, b)
			
			override val start: T
				get() = this@step.start
			override val endInclusive: T
				get() = this@step.endInclusive
		}
		
	}
}
