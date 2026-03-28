package com.wulinpeng.ezhook.compiler.hook

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.ModuleLoweringPass
import org.jetbrains.kotlin.backend.common.phaser.LoweringPhase
import org.jetbrains.kotlin.config.phaser.NamedCompilerPhase
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

/**
 * desc: Hook ir lowering phase to add custom lowering logic
 *
 * @author qiuqiu
 * @since 2026/03/28 22:55
 */
object JvmIrLoweringHook {
    private const val NATIVE_LOWERING_PHASES_CLASS = "org.jetbrains.kotlin.backend.jvm.JvmLoweringPhasesKt"

    fun runHook(
        traverser: (CommonBackendContext, IrModuleFragment) -> Unit,
        transformer: (CommonBackendContext, IrModuleFragment) -> Unit
    ) {
        runCatching {
            val allModules = mutableListOf<IrModuleFragment>()
            hookValidateIrBeforeLowering(allModules, traverser)
            hookJsCodeOutliningPhase {
                allModules.forEach { module ->
                    transformer(it, module)
                }
            }
        }.getOrElse { println(it.stackTraceToString()) }
    }

    private fun hookValidateIrBeforeLowering(
        allModules: MutableList<IrModuleFragment>,
        transformer: (CommonBackendContext, IrModuleFragment) -> Unit
    ) {
        hookLoweringPhase("JvmK1IrValidationBeforeLoweringPhase") { context, irModuleFragment ->
            allModules.add(irModuleFragment)
            transformer(context, irModuleFragment)
        }
    }

    private fun hookJsCodeOutliningPhase(
        onStart: (context: CommonBackendContext) -> Unit
    ) {
        var hasStart = false
        hookLoweringPhase("RepeatedAnnotationLowering") { context, irModuleFragment ->
            if (!hasStart) {
                onStart(context)
                hasStart = true
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun hookLoweringPhase(phaseName: String, transformer: (CommonBackendContext, IrModuleFragment) -> Unit) {
        val clazz = Class.forName(NATIVE_LOWERING_PHASES_CLASS)

        val lowerListField = clazz.declaredFields
            .firstOrNull { it.name == "jvmLoweringPhases" }
            ?.apply { isAccessible = true }!!

        val lowerList = lowerListField
            .get(null)

        val lower = (lowerList as List<Any>)
            .firstOrNull { (it as? NamedCompilerPhase<*, *, *>)?.name == phaseName }!!

        val passField = LoweringPhase::class.java
            .getDeclaredField("createLoweringPass")
            .apply { isAccessible = true }

        val originPass = passField.get(lower)
//        if (!originPass.javaClass.name.startsWith("org.jetbrains.kotlin.ir.backend.js.JsLoweringPhasesKt\$jsLowerings\$1")) {
//            // already hooked
//            return
//        }

        val newPass: (CommonBackendContext) -> OverrideLoweringPass = { context: CommonBackendContext ->
            object : OverrideLoweringPass() {
                val originPass = (originPass as (CommonBackendContext) -> ModuleLoweringPass).invoke(context)
                override fun lower(irModule: IrModuleFragment) {
                    transformer(context, irModule)
                    this.originPass.lower(irModule)
                }
            }
        }

        passField.set(lower, newPass)
    }

    abstract class OverrideLoweringPass : ModuleLoweringPass
}