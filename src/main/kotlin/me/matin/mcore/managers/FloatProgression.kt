@file:Suppress("unused")

package me.matin.mcore.managers


interface FloatProgression : ClosedFloatingPointRange<Float>, Iterable<Float> {
	
	val step: Float
}

infix fun ClosedFloatingPointRange<Float>.step(step: Float) = object : FloatProgression {
	override val step: Float = step
	override fun lessThanOrEquals(a: Float, b: Float): Boolean = this@step.lessThanOrEquals(a, b)
	override val start: Float get() = this@step.start
	override val endInclusive: Float get() = this@step.endInclusive
	
	override fun iterator() = object : Iterator<Float> {
		
		var current = start
		
		override fun next() = current.also { current += step }
		
		override fun hasNext() = current <= endInclusive
		
	}
}

interface DoubleProgression : ClosedFloatingPointRange<Double>, Iterable<Double> {
	
	val step: Double
}

infix fun ClosedFloatingPointRange<Double>.step(step: Double) = object : DoubleProgression {
	override val step = step
	override fun lessThanOrEquals(a: Double, b: Double) = this@step.lessThanOrEquals(a, b)
	override val start get() = this@step.start
	override val endInclusive get() = this@step.endInclusive
	
	override fun iterator() = object : Iterator<Double> {
		
		var current = start
		
		override fun next() = current.also { current += step }
		
		override fun hasNext() = current <= endInclusive
		
	}
}
