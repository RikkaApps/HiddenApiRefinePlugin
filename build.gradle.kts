buildscript {
    repositories {
        mavenCentral()
        google()
    }
}

allprojects {
    extra["group"] = "dev.rikka.tools"
    extra["version"] = "2.0.0"
    extra["artifactPrefix"] = "hidden-api-refine"
}

task("clean", type = Delete::class) {
    delete(buildDir)
}