plugins {
    java
    `java-gradle-plugin`
}

val pluginId = "$group.$name"
val pluginClass = "$group.RefinePlugin"

repositories {
    mavenCentral()
    google()
}

dependencies {
    compileOnly(gradleApi())

    implementation(project(":annotation"))
    implementation(project(":annotation-processor"))

    compileOnly("com.android.tools.build:gradle:7.0.1")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("org.javassist:javassist:3.28.0-GA")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
    plugins {
        create("HiddenApiRefine") {
            id = pluginId
            implementationClass = pluginClass
        }
    }
}