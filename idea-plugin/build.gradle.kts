plugins {
    id("org.jetbrains.intellij") version "1.4.0"
    java
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2021.3.1")
    plugins.set(listOf("java"))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":annotation"))
}
