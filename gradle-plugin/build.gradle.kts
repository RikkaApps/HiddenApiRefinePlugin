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
    implementation(libs.google.gson)
    implementation(libs.javassist)
}

gradlePlugin {
    plugins {
        create("HiddenApiRefine") {
            id = project.group.toString()
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
