package io.vithor.sample.domehttp.ui.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.vithor.domehttp.DomeClient
import io.vithor.domehttp.KotlinxSerializer
import io.vithor.domehttp.OkHttpEngine
import io.vithor.sample.domehttp.Todo
import io.vithor.sample.domehttp.TodoCreate
import io.vithor.sample.domehttp.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val dome = DomeClient(engine = OkHttpEngine()) {
        timeouts {
            connect = 30.seconds
            read = 30.seconds
            write = 30.seconds
        }

        serialization(KotlinxSerializer(strict = false)) {
            register(Todo.serializer())
            register(TodoCreate.serializer())
        }
//    addInterceptor { chain ->
//        chain.proceed(chain.request())
//    }
    }

    val todo = MutableLiveData<Todo>()

    init {

    }

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.e("Aehoo", "Deu ruim ${throwable.message}", throwable)
    }

    fun fetchTodo() = viewModelScope.launch(handler) {
        todo.postValue(
            dome.get<Todo>("https://jsonplaceholder.typicode.com/todos/1")
                .await(Dispatchers.IO)
        )

        val todos: List<Todo> = dome.get<Todo>("https://jsonplaceholder.typicode.com/todos") {
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

        }.asList<Todo>().await(Dispatchers.IO)

        Log.d("Aehoo", "We got todos: ${todos.size}\n$todos")

        val resp = dome.post<TodoCreate>("https://jsonplaceholder.typicode.com/todos") {
            jsonOf(
                TodoCreate(
                    userId = 1,
                    title = "Just a POST test",
                    body = "This is what is missing to do"
                )
            )
        }.await()

        Log.d("Aehoo", "We got TodoCreate: $resp")
    }
}