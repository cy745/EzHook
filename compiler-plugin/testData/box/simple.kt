package foo.bar

import com.wulinpeng.ezhook.runtime.EzHook

object App {
    fun getStr(): String = "App"
}

@EzHook("foo.bar.App.getStr")
fun getStrOverride(): String {
    return "OK"
}

fun box(): String {
    return App.getStr()
}