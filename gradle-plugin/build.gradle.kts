plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
    signing
}

dependencies {
    compileOnly(gradleApi())

    implementation(project(":annotation"))
    implementation(project(":annotation-processor"))

    compileOnly(libs.android.gradle)
    implementation(libs.asm.all)
}

gradlePlugin {
    plugins {
        create("HiddenApiRefine") {
            id = project.group.toString()
            displayName = "HiddenApiRefine"
            description = "A Gradle plugin that improves the experience when developing Android apps, especially system tools, that use hidden APIs."
            implementationClass = "$id.RefinePlugin"
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            named("pluginMaven", MavenPublication::class) {
                artifactId = project.name
            }
        }
    }
}
