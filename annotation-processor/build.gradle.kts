plugins {
    java
}

dependencies {
    implementation(project(":annotation"))

    annotationProcessor(deps.google.service.compiler)

    implementation(deps.google.service.annotation)
    implementation(deps.javassist)
}
