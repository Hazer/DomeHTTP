/*package io.vithor.sample.domehttp

//import cocoapods.AFNetworking.AFHTTPResponseSerializer
//import cocoapods.AFNetworking.AFHTTPSessionManager
//import cocoapods.PromiseKit.AnyPromise
import kotlinx.coroutines.*
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSMutableData
//import cocoapods.iosInterop.MyTestProtocolProtocol
//import cocoapods.iosInterop.OnCompletedProtocol
import io.vithor.domehttp.*

//val a = registerMyTypes()

class ApiSample {
    private val dome = DomeClient(engine = NSUrlSessionEngine()) {
        timeouts {
            connect = 30.seconds
            read = 30.seconds
            write = 30.seconds
        }

        serialization(KotlinxSerializer(strict = false)) {
            register(Todo.serializer())
            register(TodoCreate.serializer())
        }
    }

    fun createTodo(success: (TodoCreate) -> Unit, error: (ThrowableError) -> Nothing) {
        GlobalScope.launch(MainLoopDispatcher) {
            try {
                val todo = dome.post<TodoCreate>("https://jsonplaceholder.typicode.com/todos") {
                    jsonOf(
                        TodoCreate(
                            userId = 1,
                            title = "Just a POST test",
                            body = "This is what is missing to do"
                        )
                    )
                }.await()

                println("Todo created $todo")
                success(todo)
            } catch (e: Throwable) {
                println("Todo not created $e")
                error(ThrowableError(e))
            }
        }
    }

    fun createTodo(callback: OnCompletedProtocol) {
        GlobalScope.launch(MainLoopDispatcher) {
            try {
                val todo = dome.post<TodoCreate>("https://jsonplaceholder.typicode.com/todos") {
                    jsonOf(
                        TodoCreate(
                            userId = 1,
                            title = "Just a POST test",
                            body = "This is what is missing to do"
                        )
                    )
                }.await()

                todo.title

                println("Todo created $todo")
                callback.onSuccess(todo)
            } catch (e: Throwable) {
                println("Todo not created $e")
                callback.onError(ThrowableError(e))
            }
        }
    }

    fun asyncTest(onResult: OnResult<Todo>) {
        GlobalScope.launch(MainLoopDispatcher) {
            try {
                val todo = dome.get<Todo>("https://jsonplaceholder.typicode.com/todos/1") {
                    addQueryParam("discarding", "param")

                    params = listOf(
                        "dunno" to "man"
                    )

                    addQueryParam("some", "param")

                    addHeader("Discarding-Header", "Some Value")

                    headers = mapOf(
                        "accept" to "your fate"
                    )

                    addHeader("Some-Header", "Some Value")
                }.await()


//                val todo = dome.get("https://jsonplaceholder.typicode.com/todos/1", Todo.serializer())

                onResult.emitted(ApiResult.success(todo))
            } catch (e: Throwable) {
                onResult.emitted(ApiResult.failure(e))
            }
        }
    }

//    fun promiseTest(): AnyPromise {
//        return AnyPromise.promiseWithResolverBlock { resolver ->
//            GlobalScope.launch(MainLoopDispatcher) {
//                supervisorScope {
//                    try {
//                        val todo = async { dome.get<Todo>("https://jsonplaceholder.typicode.com/todos/1") }.await()
//                        resolver?.invoke(todo)
//                    } catch (e: Throwable) {
//                        resolver?.invoke(NSError("${e.message}\n${e.getStackTrace().joinToString("\n")}", 2, null))
//                    }
//                }
//            }
//        }
//
//    }

//    fun asyncTest() {
//        val onResult = onResult.freeze()
//
//        GlobalScope.launch(MainLoopDispatcher) {
//            asyncRequest("https://jsonplaceholder.typicode.com/todos/1", mapper = TodoMapper)
//                .collect(object : FlowCollector<ApiResult<Todo>> {
//                    override suspend fun emit(value: ApiResult<Todo>) {
//                        println("Collection Todos")
////                        onResult.emitted(value.freeze())
//                        onResult.emitted(value)
//                    }
//                })
//
//        }
//    }

//    object TodoMapper : Mapper<NSData, Todo> {
//        override fun map(value: NSData): Todo {
//            println("Mapping")
//            val json = value.toMap()
//            println("Json")
//            println(json)
//            return Todo(
//                userId = json.longKey("userId"),
//                id = json.longKey("id"),
//                title = json["title"].toString(),
//                completed = json.boolKey("completed")
//            )
//        }
//    }
}

interface OnCompletedProtocol {
    fun onSuccess(value: Any?)
    fun onError(error: NSError)
}

abstract class AbsResult<T> {
    abstract fun on(result: T)
    abstract fun on(error: Throwable)
}

class SomeGenThing<T> {
    fun print(thing: T) {
        println("Printing thing $thing")
    }
}

interface OnResult<T> {
    fun emitted(result: ApiResult<T>)
}


//@UseExperimental(FlowPreview::class)
//fun <T> asyncRequest(url: String, mapper: Mapper<NSData, T>): Flow<ApiResult<T>> {
////    val url = url.freeze()
////    val mapper = mapper.freeze()
//    return flowViaChannel { channel ->
//        println("Flow via channel")
//
//
//
//        val session = NSURLSession.sessionWithConfiguration(
//            NSURLSessionConfiguration.defaultSessionConfiguration(),
//            delegate,
//            delegateQueue = NSOperationQueue.mainQueue()
//        )
//
//
//        println("URLSession lets resume")
//        try {
////            println("URLSession first")
////            session.dataTaskWithURL(NSURL(string = url), completionHandler = { nsData, nsurlResponse, nsError ->
////                print("On data task completion")
////            }).resume()
//
////            println("URLSession third")
//            session.dataTaskWithURL(NSURL(string = url)).resume()
//        } catch (e: Throwable) {
//            println(e)
//        }
//    }
//}


//fun Map<String, *>.intKey(key: String): Int {
//    val value = this[key]!!
//    return (value as Number).toInt()
//}
//
//fun Map<String, *>.longKey(key: String): Long {
//    val value = this[key]!!
//    return (value as Number).toLong()
//}
//
//fun Map<String, *>.boolKey(key: String): Boolean {
//    val value = this[key]!!
//    return (value as Boolean)
//}

//fun NSData.toMap(): Map<String, *> {
//    return (NSJSONSerialization.JSONObjectWithData(this, 0, null) as? Map<String, *>)
//        ?: throw Error("JSON parsing error")
//}

//private fun parseJsonResponse(data: NSData): Stats? {
//    val dict = data.toMap()
//    val myTeamIndex = dict.intKey("color") - 1
//
//    val myConribution = dict.intKey("contribution")
//    val status = dict.intKey("status")
//    val winner = dict.intKey("winner")
//    val colors = dict["colors"] as List<*>
//
//    return Stats(counts, myTeam, myConribution, status, winner != 0)
//}


*/