@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.wasm.d8.D8EnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.d8.D8Plugin

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.gradle.java.test.fixtures)
    alias(libs.plugins.node.gradle)
    alias(libs.plugins.gradle.idea)
    id("com.vanniktech.maven.publish") version "0.35.0"
}

project.plugins.apply(D8Plugin::class.java)

sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
        resources.setSrcDirs(listOf("resources"))
    }
    testFixtures {
        java.setSrcDirs(listOf("test-fixtures"))
    }
    test {
        java.setSrcDirs(listOf("test", "test-gen"))
        resources.setSrcDirs(listOf("testData"))
    }
}

idea {
    module.generatedSourceDirs.add(projectDir.resolve("test-gen"))
}

val annotationsRuntimeClasspath: Configuration by configurations.creating { isTransitive = false }
val testArtifacts: Configuration by configurations.creating

dependencies {
    // gradle plugin
    implementation(libs.kotlin.gradle.plugin.api)

    // compiler plugin
    compileOnly(libs.kotlin.compiler)
    compileOnly(libs.kotlin.compiler.embeddable)

    testFixturesApi(libs.kotlin.test.junit5)
    testFixturesApi(libs.kotlin.test.framework)
    testFixturesApi(libs.kotlin.compiler)
    testFixturesRuntimeOnly(libs.junit)
    testFixturesApi(project(":library"))

    // 引入注解相关类
    annotationsRuntimeClasspath(project(":library"))

    // Dependencies required to run the internal test framework.
    testArtifacts(libs.kotlin.stdlib)
    testArtifacts(libs.kotlin.stdlib.jdk8)
    testArtifacts(libs.kotlin.reflect)
    testArtifacts(libs.kotlin.test)
    testArtifacts(libs.kotlin.script.runtime)
    testArtifacts(libs.kotlin.annotations.jvm)

    testArtifacts(libs.kotlin.stdlib.js)
    testArtifacts(libs.kotlin.test.js)
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
    }

    packageName(group.toString())
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")
}

tasks.test {
    dependsOn(annotationsRuntimeClasspath)

    useJUnitPlatform()
    workingDir = rootDir

    systemProperty("annotationsRuntime.classpath", annotationsRuntimeClasspath.asPath)

    // Properties required to run the internal test framework.
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")

    systemProperty("idea.ignore.disabled.plugins", "true")
    systemProperty("idea.home.path", rootDir)

    // Properties required to run JS tests from the internal test framework.
    val d8EnvSpec = project.the<D8EnvSpec>()
    with(d8EnvSpec) { dependsOn(project.d8SetupTaskProvider) }

    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-js", "kotlin-stdlib-js")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test-js", "kotlin-test-js")

    systemProperty("javascript.engine.path.V8", d8EnvSpec.executable.get())
    systemProperty("javascript.engine.path.repl", "${layout.projectDirectory.file("repl.js").asFile}")
    systemProperty("kotlin.js.test.root.out.dir", "${layout.buildDirectory.get().asFile}/js-test-output")
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
    }
}

val generateTests by tasks.registering(JavaExec::class) {
    inputs.dir(layout.projectDirectory.dir("testData"))
        .withPropertyName("testData")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(layout.projectDirectory.dir("test-gen"))
        .withPropertyName("generatedTests")

    classpath = sourceSets.testFixtures.get().runtimeClasspath
    mainClass.set("org.jetbrains.kotlin.compiler.plugin.template.GenerateTestsKt")
    workingDir = rootDir
}

tasks.compileTestKotlin {
    dependsOn(generateTests)
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
    val path = testArtifacts.files
        .find { """$jarName-\d.*""".toRegex().matches(it.name) }
        ?.absolutePath
        ?: return
    systemProperty(propName, path)
}

mavenPublishing {
    publishToMavenCentral()
//    signAllPublications()
    coordinates("io.github.dreammooncai", "ez-hook-compiler-plugin", "0.0.5-test")

    pom {
        name.set("EzHook")
        description.set("An AOP framework for KotlinMultiplatform, supporting Kotlin/Native and Kotlin/JS")
        url.set("https://github.com/DreamMoonCai/EzHook/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("DreamMoonCai")
                name.set("dreammoon")
                url.set("https://github.com/DreamMoonCai/")
            }
        }
        scm {
            url.set("https://github.com/DreamMoonCai/EzHook")
            connection.set("scm:git:git://github.com/DreamMoonCai/EzHook.git")
            developerConnection.set("scm:git:ssh://git@github.com/DreamMoonCai/EzHook.git")
        }
    }
}