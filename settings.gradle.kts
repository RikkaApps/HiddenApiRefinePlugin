@file:Suppress("UnstableApiUsage")

include(":compiler-plugin")
include(":gradle-plugin")
include(":idea-plugin")
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
            val service = "1.0.1"

            library("google-service-compiler", "com.google.auto.service", "auto-service").version(service)
            library("google-service-annotation", "com.google.auto.service", "auto-service-annotations").version(service)
        }
    }
}
