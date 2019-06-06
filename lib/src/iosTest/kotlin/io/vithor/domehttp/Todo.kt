package io.vithor.domehttp

import kotlinx.serialization.Serializable

@Serializable
data class Todo(
    var userId: Long,
    var id: Long,
    var title: String,
    var completed: Boolean
)

@Serializable
data class TodoCreate(
    var id: Long? = null,
    var userId: Long,
    var title: String,
    var body: String
)
