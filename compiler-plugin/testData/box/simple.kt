package foo.bar

object App {
    fun getStr(): String = "app"
}

@com.wulinpeng.ezhook.runtime.EzHook("foo.bar.App.getStr")
fun getStrOverride(): String {
    return "override"
}

fun box(): String {
    return App.getStr()
}