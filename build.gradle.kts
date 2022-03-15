task("clean", type = Delete::class) {
    delete(buildDir)
}

subprojects {
    group = "dev.rikka.tools.refine"
    version = "3.1.0"

    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "java")

    println("- Add publishing to module '${this.name}'")

    val sourcesJar = tasks.register("sourcesJar", type = Jar::class) {
        archiveClassifier.set("sources")
        from(project.extensions.getByType(SourceSetContainer::class).named("main").get().allSource)
    }
    val javadocJar = tasks.register("javadocJar", type = Jar::class) {
        archiveClassifier.set("javadoc")
        from(tasks["javadoc"])
    }
    val publishing = extensions.getByType(PublishingExtension::class.java).apply {
        publications {
            create("maven", type = MavenPublication::class) {
                artifactId = project.name
                version = project.version.toString()

                from(components["java"])

                artifact(javadocJar)
                artifact(sourcesJar)

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
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                credentials(PasswordCredentials::class.java)
            }
        }
    }
    extensions.configure(SigningExtension::class) {
        val signingKey = findProperty("signingKey") as? String
        val signingPassword = findProperty("signingPassword") as? String
        val secretKeyRingFile = findProperty("signing.secretKeyRingFile") as? String

        if ((secretKeyRingFile != null && file(secretKeyRingFile).exists()) || signingKey != null) {
            if (signingKey != null) {
                useInMemoryPgpKeys(signingKey, signingPassword)
            }

            val task = sign(publishing.publications)

            tasks["publishMavenPublicationToOssrhRepository"].dependsOn(task)
        }
    }
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
