package foo.bar

fun box(): String {
    val result = "Hello world"
    return if (result == "Hello world") { "OK" } else { "Fail: $result" }
}