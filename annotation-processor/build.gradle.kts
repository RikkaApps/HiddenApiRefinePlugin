plugins {
    java
    `maven-publish`
    signing
}

dependencies {
    implementation(project(":annotation"))

    annotationProcessor(libs.google.service.compiler)

    implementation(libs.google.service.annotation)
    implementation(libs.asm.all)
}

publishing {
    publications {
        create(project.name, MavenPublication::class) {
            from(components["java"])
        }
    }
}
