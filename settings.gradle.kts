plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Jenny Werewolf PaperMC Plugin"

// core: the werewolf game itself, no Minecraft dependencies (unit tested)
// app:  the Paper plugin that turns game state into Minecraft effects
include("core", "app")
