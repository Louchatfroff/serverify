plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.20" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    alias(egt.plugins.multiversionRoot)
}

preprocess {
    val forge10809 = createNode("1.8.9-forge", 10809, "srg")
    val forge11202 = createNode("1.12.2-forge", 11202, "srg")
    forge11202.link(forge10809)
}
