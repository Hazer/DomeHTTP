package io.vithor.domehttp

import kotlin.test.Test
import kotlin.test.assertTrue

class SampleTestsNative {
    @Test
    fun testPlatform() {
        assertTrue("iOS" in Platform.name())
    }

    @Test
    fun testSome() {
        val serializer = KotlinxSerializer(strict = false).apply {
            register(Todo.serializer())
        }

        val todoString = serializer.stringify(Todo(
            userId = 5,
            id = 42,
            title = "Stringfied",
            completed = true
        ))

        println(todoString)

        assertTrue("Stringfied" in todoString)
    }
}