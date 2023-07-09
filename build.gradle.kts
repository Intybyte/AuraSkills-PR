import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.type

plugins {
    java
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {

}

allprojects {
    group = "com.archyx.aureliumskills"
    version = project.property("projectVersion") as String
    description = "Advanced skills, stats, and abilties plugin"
}

java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
