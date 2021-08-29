import java.net.URI

plugins {
    java
    signing
    `maven-publish`
    `java-gradle-plugin`
}

group = extra["group"]!!
version = extra["version"]!!

val artifactName = "gradle-plugin"
val pluginId = "$group.$artifactName"
val pluginClass = "$group.RefinePlugin"

repositories {
    mavenCentral()
    google()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("com.android.tools.build:gradle:7.0.1")
    implementation(project(":annotation"))
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("org.javassist:javassist:3.28.0-GA")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
    plugins {
        create("HiddenApiRefine") {
            id = pluginId
            implementationClass = pluginClass
        }
    }
}

task("javadocJar", type = Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks["javadoc"])
}

task("sourcesJar", type = Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create("maven", type = MavenPublication::class) {
            group = project.group.toString()
            artifactId = artifactName
            version = project.version.toString()

            from(components["java"])

            artifact(tasks["javadocJar"])
            artifact(tasks["sourcesJar"])

            pom {
                name.set("HiddenApiRefine")
                description.set("HiddenApiRefine")
                url.set("https://github.com/RikkaApps/HiddenApiRefinePlugin")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/RikkaApps/HiddenApiRefinePlugin/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        name.set("Kr328 & RikkaW")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/RikkaApps/HiddenApiRefinePlugin.git")
                    url.set("https://github.com/RikkaApps/HiddenApiRefinePlugin")
                }
            }
        }
    }
    repositories {
        mavenLocal()
        maven {
            name = "ossrh"
            url = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials(PasswordCredentials::class.java)
        }
    }
}

signing {
    val signingKey = findProperty("signingKey") as? String
    val signingPassword = findProperty("signingPassword") as? String
    val secretKeyRingFile = findProperty("signing.secretKeyRingFile") as? String

    if (secretKeyRingFile != null && file(secretKeyRingFile).exists()) {
        sign(publishing.publications)
    } else if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}
