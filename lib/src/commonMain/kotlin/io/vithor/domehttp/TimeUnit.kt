@file:Suppress("NOTHING_TO_INLINE")

package io.vithor.domehttp

sealed class TimeUnit {
    companion object {
        internal const val C0 = 1L
        internal const val C1 = C0 * 1000L
        internal const val C2 = C1 * 1000L
        internal const val C3 = C2 * 1000L
        internal const val C4 = C3 * 60L
        internal const val C5 = C4 * 60L
        internal const val C6 = C5 * 24L

        internal const val MAX = Long.MAX_VALUE

        /**
         * Scale d by m, checking for overflow.
         * This has a short name to make above code more readable.
         */
        internal fun x(d: Long, m: Long, over: Long): Long {
            if (d > +over) return Long.MAX_VALUE
            return if (d < -over) Long.MIN_VALUE else d * m
        }
    }


    object NANOSECONDS : TimeUnit() {
        override fun toNanos(duration: Long) = duration
        override fun toMicros(duration: Long) = duration / (C1 / C0)
        override fun toMillis(duration: Long) = duration / (C2 / C0)
        override fun toSeconds(duration: Long) = duration / (C3 / C0)
        override fun toMinutes(duration: Long) = duration / (C4 / C0)
        override fun toHours(duration: Long) = duration / (C5 / C0)
        override fun toDays(duration: Long) = duration / (C6 / C0)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit) = sourceUnit.toNanos(sourceDuration)
        override fun excessNanos(d: Long, m: Long) = (d - m * C2).toInt()
    }

    object MICROSECONDS : TimeUnit() {
        override fun toNanos(duration: Long) = x(duration, C1 / C0, MAX / (C1 / C0))
        override fun toMicros(duration: Long) = duration
        override fun toMillis(duration: Long) = duration / (C2 / C1)
        override fun toSeconds(duration: Long) = duration / (C3 / C1)
        override fun toMinutes(duration: Long) = duration / (C4 / C1)
        override fun toHours(duration: Long) = duration / (C5 / C1)
        override fun toDays(duration: Long) = duration / (C6 / C1)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit) = sourceUnit.toMicros(sourceDuration)
        override fun excessNanos(d: Long, m: Long): Int = (d * C1 - m * C2).toInt()
    }

    object MILLISECONDS : TimeUnit() {
        override fun toNanos(duration: Long): Long = x(duration, C2 / C0, MAX / (C2 / C0))
        override fun toMicros(duration: Long): Long = x(duration, C2 / C1, MAX / (C2 / C1))
        override fun toMillis(duration: Long): Long = duration
        override fun toSeconds(duration: Long): Long = duration / (C3 / C2)
        override fun toMinutes(duration: Long): Long = duration / (C4 / C2)
        override fun toHours(duration: Long): Long = duration / (C5 / C2)
        override fun toDays(duration: Long): Long = duration / (C6 / C2)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.toMillis(sourceDuration)
        override fun excessNanos(d: Long, m: Long) = 0
    }

    object SECONDS : TimeUnit() {
        override fun toNanos(duration: Long): Long = x(duration, C3 / C0, MAX / (C3 / C0))
        override fun toMicros(duration: Long): Long = x(duration, C3 / C1, MAX / (C3 / C1))
        override fun toMillis(duration: Long): Long = x(duration, C3 / C2, MAX / (C3 / C2))
        override fun toSeconds(duration: Long): Long = duration
        override fun toMinutes(duration: Long): Long = duration / (C4 / C3)
        override fun toHours(duration: Long): Long = duration / (C5 / C3)
        override fun toDays(duration: Long): Long = duration / (C6 / C3)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.toSeconds(sourceDuration)
        override fun excessNanos(d: Long, m: Long): Int = 0
    }

    object MINUTES : TimeUnit() {
        override fun toNanos(duration: Long): Long = x(duration, C4 / C0, MAX / (C4 / C0))
        override fun toMicros(duration: Long): Long = x(duration, C4 / C1, MAX / (C4 / C1))
        override fun toMillis(duration: Long): Long = x(duration, C4 / C2, MAX / (C4 / C2))
        override fun toSeconds(duration: Long): Long = x(duration, C4 / C3, MAX / (C4 / C3))
        override fun toMinutes(duration: Long): Long = duration
        override fun toHours(duration: Long): Long = duration / (C5 / C4)
        override fun toDays(duration: Long): Long = duration / (C6 / C4)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long = sourceUnit.toMinutes(sourceDuration)
        override fun excessNanos(d: Long, m: Long): Int = 0
    }

    object HOURS : TimeUnit() {
        override fun toNanos(duration: Long) = x(duration, C5 / C0, MAX / (C5 / C0))
        override fun toMicros(duration: Long) = x(duration, C5 / C1, MAX / (C5 / C1))
        override fun toMillis(duration: Long) = x(duration, C5 / C2, MAX / (C5 / C2))
        override fun toSeconds(duration: Long) = x(duration, C5 / C3, MAX / (C5 / C3))
        override fun toMinutes(duration: Long) = x(duration, C5 / C4, MAX / (C5 / C4))
        override fun toHours(duration: Long) = duration
        override fun toDays(duration: Long) = duration / (C6 / C5)
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit) = sourceUnit.toHours(sourceDuration)
        override fun excessNanos(d: Long, m: Long): Int = 0
    }

    object DAYS : TimeUnit() {
        override fun toNanos(duration: Long) = x(duration, C6 / C0, MAX / (C6 / C0))
        override fun toMicros(duration: Long) = x(duration, C6 / C1, MAX / (C6 / C1))
        override fun toMillis(duration: Long) = x(duration, C6 / C2, MAX / (C6 / C2))
        override fun toSeconds(duration: Long) = x(duration, C6 / C3, MAX / (C6 / C3))
        override fun toMinutes(duration: Long) = x(duration, C6 / C4, MAX / (C6 / C4))
        override fun toHours(duration: Long) = x(duration, C6 / C5, MAX / (C6 / C5))
        override fun toDays(duration: Long) = duration
        override fun convert(sourceDuration: Long, sourceUnit: TimeUnit) = sourceUnit.toDays(sourceDuration)
        override fun excessNanos(d: Long, m: Long) = 0
    }

    // To maintain full signature compatibility with 1.5, and to improve the
    // clarity of the generated javadoc (see 6287639: Abstract methods in
    // enum classes should not be listed as abstract), method convert
    // etc. are not declared abstract but otherwise act as abstract methods.

    /**
     * Converts the given time duration in the given unit to this unit.
     * Conversions from finer to coarser granularities truncate, so
     * lose precision. For example, converting `999` milliseconds
     * to seconds results in `0`. Conversions from coarser to
     * finer granularities with arguments that would numerically
     * overflow saturate to `Long.MIN_VALUE` if negative or
     * `Long.MAX_VALUE` if positive.
     *
     *
     * For example, to convert 10 minutes to milliseconds, use:
     * `TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES)`
     *
     * @param sourceDuration the time duration in the given `sourceUnit`
     * @param sourceUnit the unit of the `sourceDuration` argument
     * @return the converted duration in this unit,
     * or `Long.MIN_VALUE` if conversion would negatively
     * overflow, or `Long.MAX_VALUE` if it would positively overflow.
     */
    abstract fun convert(sourceDuration: Long, sourceUnit: TimeUnit): Long

    /**
     * Equivalent to
     * [NANOSECONDS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively
     * overflow, or `Long.MAX_VALUE` if it would positively overflow.
     */
    abstract fun toNanos(duration: Long): Long

    /**
     * Equivalent to
     * [MICROSECONDS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively
     * overflow, or `Long.MAX_VALUE` if it would positively overflow.
     */
    abstract fun toMicros(duration: Long): Long

    /**
     * Equivalent to
     * [MILLISECONDS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively
     * overflow, or `Long.MAX_VALUE` if it would positively overflow.
     */
    abstract fun toMillis(duration: Long): Long

    /**
     * Equivalent to
     * [SECONDS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively
     * overflow, or `Long.MAX_VALUE` if it would positively overflow.
     */
    abstract fun toSeconds(duration: Long): Long

    /**
     * Equivalent to
     * [MINUTES.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively
     * overflow, or `Long.MAX_VALUE` if it would positively overflow.
     * @since 1.6
     */
    abstract fun toMinutes(duration: Long): Long

    /**
     * Equivalent to
     * [HOURS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration,
     * or `Long.MIN_VALUE` if conversion would negatively
     * overflow, or `Long.MAX_VALUE` if it would positively overflow.
     * @since 1.6
     */
    abstract fun toHours(duration: Long): Long

    /**
     * Equivalent to
     * [DAYS.convert(duration, this)][.convert].
     * @param duration the duration
     * @return the converted duration
     * @since 1.6
     */
    abstract fun toDays(duration: Long): Long

    /**
     * Utility to compute the excess-nanosecond argument to wait,
     * sleep, join.
     * @param d the duration
     * @param m the number of milliseconds
     * @return the number of nanoseconds
     */
    internal abstract fun excessNanos(d: Long, m: Long): Int
}