plugins {
    java
    `maven-publish`
    `java-gradle-plugin`
}

group = "dev.rikka.tools"
version = "1.0.0"

val artifact = "hidden-api-refine"
val pluginId = "$group.$artifact"
val pluginClass = "$group.HiddenApiRefinePlugin"

repositories {
    mavenCentral()
    google()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("com.android.tools.build:gradle:4.2.0")
    implementation("org.javassist:javassist:3.27.0-GA")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("HiddenApiRefine") {
            id = pluginId
            implementationClass = pluginClass
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = artifact
            version = project.version.toString()

            from(components["java"])
        }
    }
}
