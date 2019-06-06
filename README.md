# DomeHTTP Client (WIP) [ ![Download](https://api.bintray.com/packages/hazer/maven/DomeHTTP/images/download.svg) ](https://bintray.com/hazer/maven/DomeHTTP/_latestVersion)

This project is a Kotlin Multiplatform HTTP Client, totally work-in-progress, implementation is not optimal, code not tested, API not final.

## Goals
* Coroutines-based
* Small dependencies and almost reflection-free, `inline` the most we can
    (small binary size and low methods count because "Android 64k method limit")
* Make it easy to replace the engine, if needed
* Someday iOS target will work flawlessly

## Installation

```
repositories {
    maven { url "https://dl.bintray.com/hazer/maven" }
}

// Common Module
implementation "io.vithor.dome-http:client-common:$domeVersion"

// Android Module
implementation "io.vithor.dome-http:client-android:$domeVersion"

// iOS Module
implementation "io.vithor.dome-http:client-ios:$domeVersion"

// JVM Module
implementation "io.vithor.dome-http:client-jvm:$domeVersion"
```

## Usages
First you need to declare the Client configuration using default provided engines (you can create your own engine):

```kotlin
val dome = DomeClient(engine = OkHttpEngine() /* or NSUrlSessionEngine() */) {
    timeouts {
        connect = 30.seconds
        read = 30.seconds
        write = 30.seconds
    }

    serialization(KotlinxSerializer(strict = true)) {
        // Unfortunately, right now to work also in iOS, we need to declare all Top Serializers manually.
        register(Todo.serializer())
        register(TodoCreate.serializer())
    }
}
```

Then we can make requests as:

```kotlin
// GET request
val todo = dome.get<Todo>("https://jsonplaceholder.typicode.com/todos/1").await(Dispatchers.IO)
```
```kotlin
// GET request with configurations
val todos = dome.get<List<Todo>>("https://jsonplaceholder.typicode.com/todos") {
    headers += mapOf(
        "X-Header-Name" to "header value"
    )

    addHeader("X-Another-Header", "Another value")

    params = listOf(
        "excerpt" to "yes",
        "user" to "Vithorio",
        "language" to "pt-BR"
    )

    addQueryParam("another-param", "Mage")

}.await()
```
```kotlin
// POST request with Json
val todoCreated = dome.post<TodoCreate>("https://jsonplaceholder.typicode.com/todos") {
    jsonOf(
        TodoCreate(
            userId = 1,
            title = "Just a POST test",
            body = "This is what is missing to do"
        )
    )
}.await()
```
```kotlin
// POST request with form url encoded
val todoCreated = dome.post<TodoCreate>("https://jsonplaceholder.typicode.com/todos") {
    formOf(
        "userId" to "1",
        "title" to "Just a POST test",
        "body" to "This is what is missing to do"
    )
    // or
    form(
        listOf(
            "userId" to "1",
            "title" to "Just a POST test",
            "body" to "This is what is missing to do"
        )
    )
}.await()
```

## Credits
This project takes a lot of inspiration from [ktorio/ktor](https://github.com/ktorio/ktor) and [rybalkinsd/kohttp](https://github.com/rybalkinsd/kohttp).
I started building this project because Ktor Client in Android was heavy, so I made this initially as a study project and as an alternative with lower footprint.
