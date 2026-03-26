package com.wulinpeng.ezhook

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

class EzHookGradlePlugin : KotlinCompilerPluginSupportPlugin {

    private lateinit var project: Project

    override fun apply(target: Project) {
        super.apply(target)
        project = target
        target.extensions.configure(KotlinMultiplatformExtension::class.java) {
            it.sourceSets.getByName("commonMain").dependencies {
                implementation("io.github.dreammooncai:ez-hook-library:0.0.4")
            }
        }
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        return kotlinCompilation.target.project.provider {
            emptyList()
        }
    }

    override fun getCompilerPluginId() = Constants.KOTLIN_PLUGIN_ID

    override fun getPluginArtifact() = SubpluginArtifact(
        groupId = Constants.KOTLIN_PLUGIN_GROUP,
        artifactId = Constants.KOTLIN_PLUGIN_NAME,
        version = Constants.KOTLIN_PLUGIN_VERSION
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation.target is KotlinNativeTarget
                || kotlinCompilation.target is KotlinJsIrTarget
                || kotlinCompilation.target is KotlinJvmTarget
    }
}