import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id(egt.plugins.multiversion.get().pluginId)
    id(egt.plugins.defaults.get().pluginId)
    id("com.github.johnrengelman.shadow")
}

val mod_version: String by project
val sharedSources = rootProject.file("../src/main")

version = mod_version
group = "com.serverify"
base.archivesName.set("serverify-forge-mc${platform.mcVersionStr}")

repositories {
    mavenCentral()
    maven("https://maven.dediamondpro.dev/releases")
    maven("https://repo.essential.gg/repository/maven-public/")
}

val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    shade("org.jetbrains.kotlin:kotlin-stdlib:2.0.20") { isTransitive = false }
    shade("gg.essential:universalcraft-$platform:536") { isTransitive = false }
    shade("gg.essential:elementa:DIAMOND-15") { isTransitive = false }
    shade("org.jsoup:jsoup:1.17.2") { isTransitive = false }
}


val generateLang by tasks.registering {
    val source = sharedSources.resolve("resources/assets/serverify/lang/en_us.json")
    val out = layout.buildDirectory.dir("generated/lang")
    val modern = platform.mcVersion >= 11200
    inputs.file(source)
    outputs.dir(out)
    doLast {
        @Suppress("UNCHECKED_CAST")
        val data = groovy.json.JsonSlurper().parse(source) as Map<String, Any?>
        val dir = out.get().asFile.resolve("assets/serverify/lang")
        dir.mkdirs()
        val text = data.entries.joinToString("\n") { "${it.key}=${it.value}" } + "\n"
        dir.resolve(if (modern) "en_us.lang" else "en_US.lang").writeText(text, Charsets.UTF_8)
    }
}


tasks {
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            apiVersion.set(KotlinVersion.KOTLIN_2_0)
        }
    }
    withType<JavaCompile>().configureEach {
    }
    processResources {
        dependsOn(generateLang)
        from(layout.buildDirectory.dir("generated/lang"))
    }
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("dev")
        configurations = listOf(shade)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        exclude("META-INF/versions/9/**", "pack.mcmeta", "fabric.mod.json", "module-info.class")
        mergeServiceFiles()
        relocate("kotlin", "com.serverify.libs.kotlin")
        relocate("gg.essential.universal", "com.serverify.libs.universal")
        relocate("gg.essential.elementa", "com.serverify.libs.elementa")
        relocate("org.jsoup", "com.serverify.libs.jsoup")
        from(rootProject.file("../LICENSE")) { rename { "${it}_serverify" } }
    }
    named<RemapJarTask>("remapJar") {
        input.set(named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
        archiveClassifier.set("")
    }
    jar {
        dependsOn(named("shadowJar"))
        archiveClassifier.set("")
        enabled = false
    }
}
