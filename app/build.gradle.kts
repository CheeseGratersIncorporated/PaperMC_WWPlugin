plugins {
    java
}

repositories {
    mavenCentral()

    maven {
        name = "maxhenkel"
        url = uri("https://maven.maxhenkel.de/repository/public")
    }
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }
}

dependencies {
    implementation(project(":core"))

    // Provided at runtime by the server and the plugins listed in plugin.yml.
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly(libs.packetevents.spigot)
    compileOnly(libs.voicechat.api)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.jar {
    // Bundle the game core into the plugin jar.
    dependsOn(configurations.runtimeClasspath)
    from({ configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
