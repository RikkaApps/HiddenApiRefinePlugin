task("clean", type = Delete::class) {
    delete(layout.buildDirectory)
}

subprojects {
    group = "dev.rikka.tools.refine"
    version = "4.4.0"

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11

            withSourcesJar()
            withJavadocJar()
        }
    }
    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            publications {
                withType(MavenPublication::class) {
                    version = project.version.toString()
                    group = project.group.toString()

                    pom {
                        name.set("HiddenApiRefine")
                        description.set("A Gradle plugin that improves the experience when developing Android apps, especially system tools, that use hidden APIs.")
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
        plugins.withId("signing") {
            extensions.configure<SigningExtension> {
                if (findProperty("signing.gnupg.keyName") != null) {
                    useGpgCmd()

                    plugins.withId("maven-publish") {
                        extensions.configure<PublishingExtension> {
                            publications {
                                withType(MavenPublication::class) {
                                    val signingTasks = sign(this)
                                    tasks.withType(AbstractPublishToMaven::class).matching { it.publication == this }.all {
                                        dependsOn(signingTasks)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
