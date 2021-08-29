import java.net.URI

plugins {
    java
    signing
    `maven-publish`
}

group = extra["group"]!!
version = extra["version"]!!

val artifactName = "annotation"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
