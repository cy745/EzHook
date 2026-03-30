package foo.bar

import com.wulinpeng.ezhook.runtime.EzHook
import com.wulinpeng.ezhook.runtime.callOrigin
import com.wulinpeng.ezhook.runtime.setField
import com.wulinpeng.ezhook.runtime.getField

object App {
    private val prop: String = "App"
    fun getStr(): String = prop
}

//@EzHook("foo.bar.App.getStr")
//fun getStrOverride(): String {
//    return "OK"
//}

fun box(): String {
    return App.getStr()
}

@EzHook("foo.bar.App.prop")
public val newProp: String = "OK"