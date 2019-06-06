package io.vithor.domehttp

import java.util.concurrent.TimeUnit as JavaTimeUnit

fun TimeUnit.toJavaUnit(): JavaTimeUnit {
    return when (this) {
        TimeUnit.SECONDS -> JavaTimeUnit.SECONDS
        TimeUnit.NANOSECONDS -> JavaTimeUnit.NANOSECONDS
        TimeUnit.MICROSECONDS -> JavaTimeUnit.MICROSECONDS
        TimeUnit.MILLISECONDS -> JavaTimeUnit.MILLISECONDS
        TimeUnit.MINUTES -> JavaTimeUnit.MINUTES
        TimeUnit.HOURS -> JavaTimeUnit.HOURS
        TimeUnit.DAYS -> JavaTimeUnit.DAYS
    }
}