plugins {
    alias(libs.plugins.kotlinMultiplatform)
    `maven-publish`
    id("com.vanniktech.maven.publish") version "0.35.0"
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.dreammooncai", "ez-hook-library", "0.0.4")

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

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
    }
}

kotlin {
    explicitApi()

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    js().nodejs()

    jvm()

    linuxArm64()
    linuxX64()

    macosArm64()
    macosX64()

    mingwX64()

    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()

    wasmJs().nodejs()
    wasmWasi().nodejs()

    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    applyDefaultHierarchyTemplate()
}