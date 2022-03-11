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
        create("deps") {
            val agp = "7.1.2"
            val gson = "2.9.0"
            val service = "1.0.1"
            val javassist = "3.28.0-GA"

            library("android-gradle", "com.android.tools.build", "gradle").version(agp)
            library("google-gson", "com.google.code.gson", "gson").version(gson)
            library("google-service-compiler", "com.google.auto.service", "auto-service").version(service)
            library("google-service-annotation", "com.google.auto.service", "auto-service-annotations").version(service)
            library("javassist", "org.javassist", "javassist").version(javassist)
        }
    }
}