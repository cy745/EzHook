plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("io.github.dreammooncai.ez-hook-gradle-plugin")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "demo-v2"
            isStatic = true
        }
    }

    jvm("desktop")

    js(IR) {
        nodejs()
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            // put your Multiplatform dependencies here
        }
    }

    compilerOptions {
        verbose.value(true)
    }
}