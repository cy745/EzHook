package com.wulinpeng.ezhook.demo

import com.wulinpeng.ezhook.demov2.NormalTest
import com.wulinpeng.ezhook.demov2.getStr
import com.wulinpeng.ezhook.demov2.topLevelFunctionTest
import com.wulinpeng.ezhook.demov2.topLevelPropertyTest
import com.wulinpeng.ezhook.runtime.EzHook
import com.wulinpeng.ezhook.runtime.callOrigin
import com.wulinpeng.ezhook.runtime.getField
import com.wulinpeng.ezhook.runtime.getThisProperty
import com.wulinpeng.ezhook.runtime.getThisRef
import com.wulinpeng.ezhook.runtime.setField
import com.wulinpeng.ezhook.runtime.setThisProperty
import kotlin.experimental.ExperimentalNativeApi
import kotlin.time.Duration
import kotlin.time.DurationUnit

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

@EzHook("com.wulinpeng.ezhook.demo.App.prop")
public val newProp: String = "OK"

@OptIn(ExperimentalNativeApi::class)
fun main() {
    println(box())
}

