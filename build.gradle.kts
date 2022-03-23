task("clean", type = Delete::class) {
    delete(buildDir)
}

subprojects {
    group = "dev.rikka.tools.refine"
    version = "3.1.0"

    plugins.withId("java") {
        println("- Configure java for module ${project.name}")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        tasks.register("sourcesJar", type = Jar::class) {
            archiveClassifier.set("sources")
            from(project.extensions.getByType<SourceSetContainer>().getByName("main").allSource)
        }
        tasks.register("javadocJar", type = Jar::class) {
            archiveClassifier.set("javadoc")
            from(tasks["javadoc"])
        }
        tasks.withType(Javadoc::class) {
            isFailOnError = false
        }
    }
    plugins.withId("maven-publish") {
        println("- Configure publishing for module '${project.name}'")

        afterEvaluate {
            extensions.configure<PublishingExtension> {
                publications {
                    withType(MavenPublication::class) {
                        version = project.version.toString()
                        group = project.group.toString()

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

                    repositories {
                        mavenLocal()
                        maven {
                            name = "ossrh"
                            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                            credentials(PasswordCredentials::class.java)
                        }
                    }
                }
            }
        }
        plugins.withId("signing") {
            println("- Configure signing for module '${project.name}'")

            afterEvaluate {
                extensions.configure<SigningExtension> {
                    if (findProperty("signing.gnupg.keyName") != null) {
                        useGpgCmd()

                        val signingTasks = sign(extensions.getByType<PublishingExtension>().publications)
                        tasks.withType(AbstractPublishToMaven::class) {
                            dependsOn(signingTasks)
                        }
                    }
                }
            }
        }
    }
}

