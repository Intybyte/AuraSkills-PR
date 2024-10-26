import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.2.0"
}

group = "me.vaan"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.spongepowered.org/maven/") {
        name = "spongepowered-repo"
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":api-sponge"))
    implementation("org.bstats:bstats-sponge:3.0.2")
    implementation("co.aikar:acf-sponge:0.5.1-SNAPSHOT")
}

sponge {
    apiVersion("10.1.0-SNAPSHOT")
    license("unlicense")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("auraskills") {
        displayName("AuraSkills")
        entrypoint("dev.aurelium.auraskills.sponge.AuraSkills")
        description("My plugin description")
        links {
            // homepage("https://spongepowered.org")
            // source("https://spongepowered.org/source")
            // issues("https://spongepowered.org/issues")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val javaTarget = 17 // Sponge targets a minimum of Java 17
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaTarget))
}

tasks.withType<JavaCompile>().configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
