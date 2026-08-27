
plugins {

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
   
    // compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")

    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")

    
    implementation("de.maxhenkel.voicechat:voicechat-api:2.6.0")

    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.guava)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
