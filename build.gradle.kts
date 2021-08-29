buildscript {
    repositories {
        mavenCentral()
        google()
    }
}

allprojects {
    extra["group"] = "dev.rikka.tools.refine"
    extra["version"] = "2.0.0"
}

task("clean", type = Delete::class) {
    delete(buildDir)
}