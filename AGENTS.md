# Agent-Konfiguration für Relicwrought

## Projektidentität
- **Projekt:** Relicwrought
- **Namespace:** `relicwrought`
- **Basispaket:** `io.github.bysenom.relicwrought`
- Minecraft-Fabric-Mod
- Java und Gradle
- Lootbasiertes ARPG-Overhaul mit Diablo-/PoE-Inspiration
- Bestehende Systeme dürfen nicht unnötig neu implementiert werden.

## Quellen der Wahrheit
- Vor Änderungen relevante Repository-Dateien lesen.
- `Roadmap.md` beachten.
- Minecraft-, Mojang- und Fabric-APIs anhand der tatsächlichen Version, Mappings, Abhängigkeiten und Compilerfehler verifizieren.
- Keine Klassen, Methoden, Events, Packages oder Config-Keys erfinden.
- Repository-Code, Compiler, Tests und Mappings haben Vorrang vor Modellwissen.

## Arbeitsweise
- Kleine, nachvollziehbare Änderungen.
- Keine ungefragten Refactorings.
- Früh kompilieren.
- Fehler beheben, bevor der Umfang erweitert wird.
- Regressionstests ergänzen.
- Domainlogik möglichst von Minecraft-Adaptern trennen.
- Keine übergroßen Manager-Klassen.
- Immutable Records oder Value Objects bevorzugen.
- Keine Clientklassen in Server- oder Common-Code.
- Tooltips niemals als Gameplay-Datenquelle verwenden.
- Lokale und globale Affixe niemals doppelt anwenden.
- Numerische Sicherheit beachten: negative Werte, `NaN`, Infinity und Überläufe behandeln.
- Für sichtbare Texte `Component` und Übersetzungsschlüssel verwenden.
- Keine rohen `§`-Farbcodes.

## Verifikation
Nutze abhängig von der Änderung:
- `.\gradlew.bat compileJava`
- `.\gradlew.bat compileClientJava`
- `.\gradlew.bat test`
- `.\gradlew.bat build`
- `.\gradlew.bat runServer`
- `.\gradlew.bat runClient`

Niemals behaupten, ein Befehl sei erfolgreich gewesen, wenn er nicht tatsächlich erfolgreich ausgeführt wurde.
Tests dürfen nicht entfernt oder abgeschwächt werden, nur um einen Build grün zu bekommen.

## Git-Regeln
Vor jedem lokalen Commit:
- `git status`
- `git diff --stat`
- `git diff`

Weitere Regeln:
- Kleine Conventional Commits sind erlaubt.
- Keine fremden oder themenfremden Änderungen committen.
- Niemals ohne ausdrückliche Freigabe `git push` ausführen.
- Kein Force-Push.
- Kein `git reset --hard`.
- Kein `git clean`.
- Keine History-Umschreibung.
- Keine destruktiven Dateioperationen ohne Freigabe.
- Benutzeränderungen nicht ungefragt zurücksetzen.

**Absolute Regel:**
> Lokale Commits sind erlaubt. Pushen ist ohne ausdrückliche Freigabe verboten.

## Kommunikationsstil
- Keine Floskeln.
- Direkt mit Plan, Ergebnis, Fehler oder nächstem Schritt beginnen.
- Bekannten Kontext nicht unnötig wiederholen.
- Kurze, überprüfbare Begründungen liefern.
- Keine private Gedankenkette ausgeben.
- Abschlussbericht auf Änderungen, Tests, Builds, Commits, Einschränkungen und die Bestätigung „kein Push“ begrenzen.
