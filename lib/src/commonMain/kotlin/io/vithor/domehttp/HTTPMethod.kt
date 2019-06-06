package io.vithor.domehttp

enum class HTTPMethod(val methodName: String) {
//    Connect("CONNECT"),
    Delete("DELETE"),
    Get("GET"),
    Head("HEAD"),
//    Options("OPTIONS"),
    Patch("PATCH"),
//    PropPatch("PROPPATCH"),
    Post("POST"),
    Put("PUT")
//    Report("REPORT"),
//    Trace("TRACE")
}