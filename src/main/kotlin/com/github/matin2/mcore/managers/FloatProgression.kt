@file:Suppress("unused")

package com.github.matin2.mcore.managers


interface FloatProgression : ClosedFloatingPointRange<Float>, Iterable<Float> {
	
	val step: Float
}

infix fun ClosedFloatingPointRange<Float>.step(step: Float) = object : FloatProgression {
	override val step: Float = step
	override fun lessThanOrEquals(a: Float, b: Float): Boolean = this@step.lessThanOrEquals(a, b)
	override val start: Float get() = this@step.start
	override val endInclusive: Float get() = this@step.endInclusive
	
	override fun iterator() = object : FloatIterator() {
		
		var hasNext = if (step > 0) start <= endInclusive else start >= endInclusive
		private var next = if (hasNext) start else endInclusive
		
		override fun nextFloat(): Float {
			val value = next
			if (value == endInclusive) {
				if (!hasNext) throw NoSuchElementException()
				hasNext = false
			} else next += step
			return value
		}
		
		override fun hasNext() = hasNext
		
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
	
	override fun iterator() = object : DoubleIterator() {
		
		var hasNext = if (step > 0) start <= endInclusive else start >= endInclusive
		private var next = if (hasNext) start else endInclusive
		
		override fun nextDouble(): Double {
			val value = next
			if (value == endInclusive) {
				if (!hasNext) throw NoSuchElementException()
				hasNext = false
			} else next += step
			return value
		}
		
		override fun hasNext() = hasNext
		
	}
}
