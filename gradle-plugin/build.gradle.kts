plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
    signing
}

dependencies {
    compileOnly(gradleApi())

    implementation(project(":annotation"))
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
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
            }
        }
    }
}
