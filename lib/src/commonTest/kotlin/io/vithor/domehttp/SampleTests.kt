package io.vithor.domehttp

import kotlin.test.Test
import kotlin.test.assertTrue

class SampleTests {
    @Test
    fun testMe() {
        println("Platform name: ${Platform.name()}")
        assertTrue(Platform.name().isNotBlank())
    }
}