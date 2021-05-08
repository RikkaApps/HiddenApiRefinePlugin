plugins {
    java
    `maven-publish`
    `java-gradle-plugin`
}

// TODO: rename group
group = "org.example"
version = "4.2.0-01"

repositories {
    mavenCentral()
    google()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("com.android.tools.build:gradle:4.2.0") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }
    implementation("org.javassist:javassist:3.27.0-GA")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("class-rename") {
            id = "class-rename"
            implementationClass = "$group.ClassRenamePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "class-rename"
            version = project.version.toString()

            from(components["java"])
        }
    }
}