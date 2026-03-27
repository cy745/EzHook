package foo.bar

import com.wulinpeng.ezhook.runtime.EzHook

object App {
    fun getStr(): String = "app"
}

@EzHook("foo.bar.App.getStr")
fun getStrOverride(): String {
    return "override"
}

fun box(): String {
    return App.getStr()
}