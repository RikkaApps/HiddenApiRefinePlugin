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
