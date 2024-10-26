import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
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
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":api-sponge"))
    implementation("org.bstats:bstats-sponge:3.0.2")
    implementation("co.aikar:acf-sponge:0.5.1-SNAPSHOT")
    implementation("de.tr7zw:item-nbt-api:2.13.2")
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

tasks {
    withType<ShadowJar> {
        val projectVersion: String by project
        archiveFileName.set("AuraSkills-${projectVersion}.jar")

        relocate("co.aikar.commands", "dev.aurelium.auraskills.acf")
        relocate("co.aikar.locales", "dev.aurelium.auraskills.locales")
        relocate("de.tr7zw.changeme.nbtapi", "dev.aurelium.auraskills.nbtapi")
        relocate("org.bstats", "dev.aurelium.auraskills.bstats")
        exclude("acf-*.properties")

        finalizedBy("copyJar")
    }

    register<Copy>("copyJar") {
        val projectVersion : String by project
        from("build/libs/AuraSkills-${projectVersion}.jar")
        into("../build/libs")
    }

    build {
        dependsOn(shadowJar)
    }

    javadoc {
        options {
            (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.forkOptions.executable = "javac"
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("projectVersion" to project.version)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
