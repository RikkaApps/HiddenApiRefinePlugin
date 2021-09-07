plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":annotation"))

    annotationProcessor("com.google.auto.service:auto-service:1.0")

    implementation("com.google.auto.service:auto-service-annotations:1.0")
    implementation("org.javassist:javassist:3.28.0-GA")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
