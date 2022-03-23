plugins {
    java
    `maven-publish`
    signing
}

dependencies {
    implementation(project(":annotation"))

    annotationProcessor(libs.google.service.compiler)

    implementation(libs.google.service.annotation)
    implementation(libs.javassist)
}

publishing {
    publications {
        create(project.name, MavenPublication::class) {
            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}