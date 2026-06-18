# Minecraft ARPG Mod

Fabric-Projekt fuer Minecraft `26.2`.

## Voraussetzungen

- Java 25 oder neuer
- IntelliJ IDEA oder VS Code mit Java-Erweiterungen
- Internetzugriff fuer Gradle-Abhaengigkeiten

## Wichtige Befehle

- `./gradlew build` baut die Mod-JAR.
- `./gradlew runClient` startet Minecraft im Entwicklungsclient.
- `./gradlew runServer` startet einen Entwicklungsserver.
- `./gradlew genSources` erzeugt Quellen fuer besseres Navigieren in der IDE.

Unter Windows funktionieren die Befehle auch mit `gradlew.bat`, sobald der Wrapper generiert ist.

## Projektstruktur

- `src/main/java` enthaelt gemeinsamen Server-/Client-Code.
- `src/client/java` enthaelt reinen Client-Code.
- `src/main/resources/fabric.mod.json` enthaelt Fabric-Metadaten und Abhaengigkeiten.
- `docs/ARPG_PLAN.md` enthaelt die Gameplay-Richtung und erste Arbeitspakete.

## Technische Basis

- Minecraft: `26.2`
- Fabric Loader: `0.19.3`
- Fabric API: `0.152.1+26.2`
- Fabric Loom: `1.17.11`
- Mappings: keine separate Mapping-Dependency. `26.2` nutzt bereits lesbare Minecraft-Klassennamen, deshalb wird Loom im No-Remap-Modus verwendet.

Hinweis: Fabric Loader loggt beim Start `Mappings not present!`. Das ist in diesem Setup erwartet, weil fuer `26.2` keine Yarn-/Intermediary-Mappings benoetigt werden.
