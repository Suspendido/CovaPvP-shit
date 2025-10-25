import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.StringWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.gradle.api.tasks.Exec
import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    java
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val currentDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

group = "me.keano.azurite"
version = currentDate
description = "AzuriteHCF Gradle Version made by Yair Soto for Cyrus (THIS PROJECT CAN'T BE DISTRIBUTED DUE TO AZURITE DEVELOPMENT RIGHTS!)"

repositories {
    mavenCentral()
    maven("https://repo1.maven.org/maven2/")
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.lunarclient.dev")
    maven("https://repo.lucko.me")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {

    implementation("com.github.cryptomorin:XSeries:13.0.0")

    // Misc
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.mongodb:mongo-java-driver:3.12.10")

    // Vault
    compileOnly(files("libs/Vault.jar"))

    // XSeries and ViaVersion
    compileOnly(files("libs/ViaVersion.jar"))
    compileOnly(files("libs/ViaVersionLatest.jar"))

    // ProtocolSupport
    compileOnly(files("libs/ProtocolSupport.jar"))

    // Holograms
    compileOnly(files("libs/holograms/HolographicDisplays.jar"))

    // Clients
    compileOnly("com.lunarclient:apollo-api:1.1.8")
    compileOnly(files("libs/clients/CheatBreakerAPI.jar"))

    // Abilities
    compileOnly(files("libs/abilities/PandaAbilityAPI.jar"))
    compileOnly(files("libs/abilities/SladeAPI.jar"))

    // Pearls
    compileOnly(files("libs/pearls/VortexPearlsAPI.jar"))

    // Placeholders
    compileOnly(files("libs/placeholders/PlaceholderAPI.jar"))

    // Spigots
    compileOnly(files("libs/spigots/1.8.8.jar"))
    compileOnly(files("libs/spigots/1.16.jar"))
    compileOnly(files("libs/spigots/1.7.10.jar"))
    compileOnly(files("libs/spigots/1.17.jar"))

    // Rank Cores
    compileOnly(files("libs/ranks/AquaCoreAPI.jar"))
    compileOnly(files("libs/ranks/Zoot.jar"))
    compileOnly(files("libs/ranks/VolcanoAPI.jar"))
    compileOnly(files("libs/ranks/ZoomAPI.jar"))
    compileOnly(files("libs/ranks/Vault.jar"))
    compileOnly(files("libs/ranks/MizuAPI.jar"))
    compileOnly(files("libs/ranks/AtomAPI.jar"))
    compileOnly(files("libs/ranks/CoreAPI.jar"))
    compileOnly(files("libs/ranks/zPermissions.jar"))
    compileOnly(files("libs/ranks/HestiaAPI.jar"))
    compileOnly(files("libs/ranks/pxAPI.jar"))
    compileOnly(files("libs/ranks/Alchemist.jar"))
    compileOnly(files("libs/ranks/Holiday.jar"))
    compileOnly(files("libs/ranks/Akuma.jar"))
    compileOnly(files("libs/ranks/Helium.jar"))

    // LuckPerms
    compileOnly("net.luckperms:api:5.4")

    // Tags
    compileOnly(files("libs/ranks/DeluxeTags.jar"))

    // NBT
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.14.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    archiveBaseName.set("Ares")
    archiveVersion.set("${project.version}")

    doFirst {
        val writer = StringWriter()
        val pluginData = mapOf(
            "name" to "Ares",
            "main" to "me.keano.azurite.HCF",
            "version" to "${project.version}",
            "author" to listOf("Keqno", "RodriDevs"),
            "description" to "Made for (Azurite) CyrusPvP Network",
            "api-version" to "1.13",
            "loadbefore" to listOf("ShopGUIPlus", "EconomyShopGUI"),
            "softdepend" to listOf(
                "Azurite-Core", "Apollo-Bukkit", "ViaVersion", "ProtocolSupport", "Vault",
                "HolographicDisplays", "VortexPearls", "PlaceholderAPI", "CheatBreakerAPI",
                "Alchemist", "Holiday", "AquaCore", "Zoot", "Zoom", "Mizu", "Atom", "Basic",
                "ZPermissions", "HestiaCore", "Phoenix", "pxLoader", "Volcano", "Kup"
            )
        )

        pluginData.toYaml(writer)

        val pluginYamlFile = file("$buildDir/generated/plugin.yml")
        pluginYamlFile.parentFile.mkdirs()
        pluginYamlFile.writeText(writer.toString())
    }

    from("$buildDir/generated") {
        include("plugin.yml")
    }
}

publishing {
    repositories {
        mavenLocal()
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("Ares")
    archiveVersion.set("${project.version}")
    archiveClassifier.set("all")
    minimize()

    doFirst {
        val libsDir = file("$buildDir/libs")
        if (libsDir.exists()) {
            val oldJars = libsDir.listFiles { _, name ->
                name.startsWith("Ares-") && name.endsWith("-all.jar")
            }

            if (oldJars != null && oldJars.isNotEmpty()) {
                println("🧹 Limpiando JARs antiguos del directorio build:")
                oldJars.forEach { jar ->
                    println("   - ${jar.name}")
                    jar.delete()
                }
            }
        }
    }

    outputs.upToDateWhen { false }
}

tasks.register<Copy>("copyToServer") {
    dependsOn("shadowJar")

    val serverPluginsDir = file("C:/dev/mc/covapvp/sv/plugins")

    outputs.upToDateWhen { false }

    doFirst {
        if (serverPluginsDir.exists()) {
            val oldJars = serverPluginsDir.listFiles { _, name ->
                name.startsWith("Ares-") && name.endsWith("-all.jar")
            }

            if (oldJars != null && oldJars.isNotEmpty()) {
                println("🗑️ Limpiando TODOS los archivos Ares anteriores:")
                oldJars.forEach { jar ->
                    println("   - ${jar.name}")
                    jar.delete()
                }
            }
        } else {
            serverPluginsDir.mkdirs()
        }
    }

    from("$buildDir/libs") {
        include("*-all.jar")
    }
    into(serverPluginsDir)

    doLast {
        val copiedFiles = serverPluginsDir.listFiles { _, name ->
            name.startsWith("Ares-") && name.endsWith("-all.jar")
        }

        if (copiedFiles != null && copiedFiles.isNotEmpty()) {
            println("✅ JAR copiado exitosamente:")
            copiedFiles.forEach { file ->
                println("   📁 ${file.name} (${file.length()} bytes)")
            }
            println("   🎯 Destino: $serverPluginsDir")
        }
    }
}

tasks.build {
    finalizedBy("publishToMavenLocal")
}

tasks.register("buildAndCopy") {
    dependsOn("shadowJar")
    finalizedBy("copyToServer")

    group = "build"
    description = "Builds the plugin and copies it to the local server"
}

tasks.named("publishToMavenLocal") {
    dependsOn("jar")
}

fun Map<String, Any>.toYaml(writer: StringWriter, indent: String = "") {
    for ((key, value) in this) {
        when (value) {
            is String -> writer.write("$indent$key: $value\n")
            is List<*> -> {
                writer.write("$indent$key:\n")
                value.forEach { item -> writer.write("$indent  - $item\n") }
            }
            is Map<*, *> -> {
                writer.write("$indent$key:\n")
                (value as Map<String, Any>).toYaml(writer, "$indent  ")
            }
        }
    }
}



        tasks.register("stopLocalServer") {
            group = "server"
            description = "Detiene el servidor local de Minecraft (si está corriendo)"
            doLast {
                println("🛑 Intentando detener el servidor de Minecraft...")

                // Ejecutar wmic para buscar procesos java.exe con la ruta del sv
                val wmic = ProcessBuilder(
                    "cmd", "/c", "wmic process where \"CommandLine like '%covapvp\\\\sv%' and Name='java.exe'\" get ProcessId"
                ).redirectErrorStream(true).start()
                val output = BufferedReader(InputStreamReader(wmic.inputStream)).readText()
                val pids = Regex("""\d+""").findAll(output).map { it.value }.toList()
                if (pids.isNotEmpty()) {
                    pids.forEach { pid ->
                        println("   Matando proceso java con PID $pid")
                        ProcessBuilder("cmd", "/c", "taskkill /PID $pid /F").start().waitFor()
                    }
                } else {
                    println("   No se encontró proceso java.exe para el servidor.")
                }
            }
        }

tasks.register<Exec>("startLocalServer") {
    group = "server"
    description = "Inicia el servidor local con start.bat"
    commandLine("cmd", "/c", "start", "\"MCServer\"", "C:\\dev\\mc\\covapvp\\sv\\start.bat")
}


tasks.register("buildCopyRestart") {
    group = "build"
    description = "Clean, build, copy, stop e inicia el servidor local"


    dependsOn("clean")
    dependsOn("shadowJar")
    dependsOn("copyToServer")
    finalizedBy("stopLocalServer")

    doLast {
        println("🕑 Esperando 2 segundos para reiniciar el servidor...")
        Thread.sleep(2000)
    }


    finalizedBy("startLocalServer")
}
