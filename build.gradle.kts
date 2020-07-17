plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
    maven {
        name = "Paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "husk"
        url = uri("https://maven.husk.pro/repository/maven-public/")
    }
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.1.0.RC4")
    compileOnly("com.destroystokyo.paper:paper-api:1.16.1-R0.1-SNAPSHOT")
    compileOnly("pw.biome:BiomeChat:3.1.0")
    compileOnly("org.projectlombok:lombok:1.18.12")
    annotationProcessor("org.projectlombok:lombok:1.18.12")
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveFileName.set("BiomeChatRelay-" + project.version + ".jar")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "pw.biome"
            artifactId = "BiomeChatRelay"
            version = project.property("version").toString()
            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("https://maven.husk.pro/repository/maven-public/")

            credentials {
                username = "slave"
                password = if (project.hasProperty("repoPass")) {
                    project.property("repoPass").toString()
                } else {
                    ""
                }
            }
        }
    }
}