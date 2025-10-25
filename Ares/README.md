# Ares (Azurite-HCF)

Ares is a modern **Hardcore Factions (HCF)** core for Spigot servers. It bundles many modules such as teams, PvP classes, events, timers, scoreboards and more. The code under `src/main/java` is organized by module to make maintenance easier.

## Project layout

```
src/main/java/me/keano/azurite/       # Plugin source code
├── HCF.java                          # Main plugin class
├── modules/                          # Feature modules (teams, timers, kits...)
└── utils/                            # Utility classes shared between modules
src/main/resources/                   # Default configuration files
```

The `how-to.txt` file contains examples and command references for setting up game features such as spawn, koths and holograms.

## Building

### Requirements
- **JDK 17** (plugin runs with Java 17)
- Gradle **8** (use 9.0 locally if 8 fails)
- Language level **11**

### Local development

```bash
./gradlew clean buildCopyRestart
```
This task builds the plugin and copies the JAR to the development server defined in `build.gradle.kts`.

### GitHub Actions / CI

```bash
./gradlew build
```
Produces a shaded JAR in `build/libs` ready for distribution.

## Authors
- Keano
- RodriDevs
- C0munidad
- Yair

See `LICENSE.txt` for usage restrictions. This project is proprietary and redistribution is not permitted.
