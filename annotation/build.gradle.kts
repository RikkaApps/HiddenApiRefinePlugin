plugins {
    java
    `maven-publish`
    signing
}

publishing {
    publications {
        create(project.name, MavenPublication::class) {
            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}
