package com.wulinpeng.ezhook.compiler

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.ir.InternalSymbolFinderAPI
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * @author wulinpeng
 * @since 2024/11/22 00:09
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrConstructorCall.defaultParamValue(index: Int): IrExpression? {
    return symbol.owner.parameters.filter { it.kind != IrParameterKind.DispatchReceiver }[1].defaultValue?.expression
}

fun IrDeclarationBase.isClassMember(): Boolean {
    return parent is IrClass
}

inline fun <reified T> T.copyDeclarationToParent(
    newName: String,
    newParent: IrDeclarationParent = parent
): T where T : IrDeclarationBase, T : IrDeclarationWithName {
    return deepCopyWithSymbols(newParent).apply {
        // For property accessors (e.g., <get-prop>), the newName contains the <>
        // which is invalid as a JVM method name. Convert to valid JVM name (e.g., getProp).
        name = Name.identifier(convertToJvmMethodName(newName))
        setDeclarationsParent(newParent)
        // Clear correspondingPropertySymbol to avoid "orphaned getter/setter" IR validation error.
        // The copied function is no longer part of the original property.
        (this as? IrSimpleFunction)?.correspondingPropertySymbol = null
        (newParent as IrDeclarationContainer).addChild(this)
    }
}

/**
 * Converts Kotlin IR property accessor names to valid JVM method names.
 * Handles both bare accessors (<get-prop>) and those with suffixes (<get-prop>_function_ez_hook).
 * Examples:
 *   <get-prop> -> getProp
 *   <get-prop>_function_ez_hook -> getProp_function_ez_hook
 *   <set-name> -> setName
 *   <is-active> -> isActive
 */
fun convertToJvmMethodName(name: String): String {
    // Check if it starts with a property accessor prefix (with optional suffix after the closing >)
    // The pattern is: <get/set/is-xxx> optionally followed by suffix
    if (name.startsWith("<")) {
        val endOfAccessor = name.indexOf('>')
        if (endOfAccessor > 0) {
            val accessorPart = name.substring(0, endOfAccessor + 1) // e.g., "<get-prop>"
            val suffix = name.substring(endOfAccessor + 1) // e.g., "_function_ez_hook"

            val content = accessorPart.substring(1, accessorPart.length - 1) // Remove < and >
            val converted = when {
                content.startsWith("get-") -> "get" + capitalize(content.substring(4))
                content.startsWith("set-") -> "set" + capitalize(content.substring(4))
                content.startsWith("is-") -> "is" + capitalize(content.substring(3))
                else -> accessorPart // Fallback: keep original accessor part
            }
            return converted + suffix
        }
    }
    return name
}

private fun capitalize(str: String): String {
    return if (str.isEmpty()) str else str[0].uppercaseChar() + str.substring(1)
}

@OptIn(InternalSymbolFinderAPI::class, UnsafeDuringIrConstructionAPI::class)
private fun CommonBackendContext.getPairClass(): IrClass {
    val pairClassSymbol = irBuiltIns.symbolFinder.findClass(
        ClassId.topLevel(FqName("kotlin.Pair"))
    ) ?: error("Pair class not found")

    return pairClassSymbol.owner
}

fun CommonBackendContext.getPairType(firstType: IrType = irBuiltIns.anyType.makeNullable(), secondType: IrType = irBuiltIns.anyType.makeNullable()): IrType {
    val pairClass = getPairClass()
    return pairClass.typeWith(firstType, secondType)
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
private fun CommonBackendContext.getPairConstructor(): IrConstructor {
    val pairClass = getPairClass()
    return pairClass.constructors.first()
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun CommonBackendContext.createPair(
    builder: IrBuilder,
    value1: IrExpression?,
    value2: IrExpression?
): IrExpression {
    val constructor = getPairConstructor()

    return builder.run {
        irCallConstructor(constructor.symbol, listOf(value1?.type ?: irBuiltIns.anyType.makeNullable(), value2?.type ?: irBuiltIns.anyType.makeNullable())).apply {
            arguments[0] = value1
            arguments[1] = value2
        }
    }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
private fun CommonBackendContext.getPairComponent1(): IrSimpleFunction {
    val pairClass = getPairClass()
    return pairClass.functions.first { it.name.asString() == "component1" }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
private fun CommonBackendContext.getPairComponent2(): IrSimpleFunction {
    val pairClass = getPairClass()
    return pairClass.functions.first { it.name.asString() == "component2" }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrStatementsBuilder<*>.getPairFirst(
    pluginContext: CommonBackendContext,
    pairVar: IrVariable
): IrVariable {
    val comp1 = pluginContext.getPairComponent1()

    return irTemporary(
        irCall(comp1.symbol).apply {
            dispatchReceiver = irGet(pairVar)
        },
        nameHint = "pair_first"
    )
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrStatementsBuilder<*>.getPairSecond(
    pluginContext: CommonBackendContext,
    pairVar: IrVariable
): IrVariable {
    val comp2 = pluginContext.getPairComponent2()

    return irTemporary(
        irCall(comp2.symbol).apply {
            dispatchReceiver = irGet(pairVar)
        },
        nameHint = "pair_second"
    )
}