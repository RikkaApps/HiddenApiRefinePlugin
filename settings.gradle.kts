@file:Suppress("UnstableApiUsage")

include(":gradle-plugin")
include(":annotation-processor")
include(":annotation")
include(":runtime")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
    versionCatalogs {
        create("libs") {
            val agp = "7.4.1"
            val service = "1.0.1"
            val asm = "5.2"

            library("android-gradle", "com.android.tools.build:gradle:$agp")
            library("google-service-compiler", "com.google.auto.service:auto-service:$service")
            library("google-service-annotation", "com.google.auto.service:auto-service-annotations:$service")
            library("asm-all", "org.ow2.asm:asm-all:$asm")
        }
    }
}
