plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    // Paper ships Adventure at runtime, so the core only compiles against it.
    compileOnlyApi(libs.adventure.api)

    testImplementation(libs.adventure.api)
    testImplementation(libs.adventure.text.serializer.plain)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
