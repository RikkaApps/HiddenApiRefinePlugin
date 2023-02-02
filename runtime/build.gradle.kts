plugins {
    java
    `maven-publish`
    signing
}

publishing {
    publications {
        create(project.name, MavenPublication::class) {
            from(components["java"])
        }
    }
}
