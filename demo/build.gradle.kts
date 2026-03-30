plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("io.github.dreammooncai.ez-hook-gradle-plugin")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm("desktop")

    js(IR) {
        nodejs()
        browser()
        binaries.library()
    }

    macosArm64 {
        binaries.executable {
            baseName = "Shared"
            entryPoint = "com.wulinpeng.ezhook.demo.main"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":demo-v2"))
        }
    }
}