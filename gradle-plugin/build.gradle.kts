plugins {
    java
    `java-gradle-plugin`
}

dependencies {
    compileOnly(gradleApi())

    implementation(project(":annotation"))
    implementation(project(":annotation-processor"))

    compileOnly(deps.android.gradle)
    implementation(deps.google.gson)
    implementation(deps.javassist)
}

gradlePlugin {
    plugins {
        create("HiddenApiRefine") {
            id = project.group.toString()
            implementationClass = "$id.RefinePlugin"
        }
    }
}
