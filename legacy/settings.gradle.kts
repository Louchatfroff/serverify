pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.dediamondpro.dev/releases")
        maven("https://repo.essential.gg/repository/maven-public/")
    }
    val egtVersion = "0.6.2+diamond.loom1.6.17"
    plugins {
        id("gg.essential.multi-version.root") version egtVersion
    }
    dependencyResolutionManagement {
        versionCatalogs {
            create("egt") {
                plugin("multiversion", "gg.essential.multi-version").version(egtVersion)
                plugin("multiversionRoot", "gg.essential.multi-version.root").version(egtVersion)
                plugin("defaults", "gg.essential.defaults").version(egtVersion)
            }
        }
    }
}

rootProject.name = "serverify-legacy"
rootProject.buildFileName = "root.gradle.kts"

listOf("1.8.9-forge", "1.12.2-forge").forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}

val sharedMain = file("../src/main")
val legacyMain = file("src/main")

fun syncShared(from: File, to: File) {
    to.parentFile.mkdirs()
    from.copyRecursively(to, overwrite = true)
}

legacyMain.resolve("kotlin/com/serverify/gui").deleteRecursively()
syncShared(sharedMain.resolve("java/com/serverify"), legacyMain.resolve("java/com/serverify"))
syncShared(sharedMain.resolve("kotlin/com/serverify/gui"), legacyMain.resolve("kotlin/com/serverify/gui"))

val modVersion = providers.gradleProperty("mod_version").getOrElse("0.0.0")
legacyMain.resolve("java/com/serverify/legacy").mkdirs()
legacyMain.resolve("java/com/serverify/legacy/BuildInfo.java").writeText(
    "package com.serverify.legacy;\n\npublic final class BuildInfo {\n    public static final String VERSION = \"$modVersion\";\n\n    private BuildInfo() {\n    }\n}\n"
)

val smokeTarget = legacyMain.resolve("java/com/serverify/smoke/Smoke.java")
if (providers.gradleProperty("smoke").isPresent) {
    smokeTarget.parentFile.mkdirs()
    file("smoke/Smoke.java").copyTo(smokeTarget, overwrite = true)
} else {
    smokeTarget.delete()
}
