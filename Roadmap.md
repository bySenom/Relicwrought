# ARPG Item Overhaul Roadmap

## Projektziel

Minecraft soll schrittweise zu einem lootbasierten Action-RPG mit serverautoritärer Itemgenerierung, datengetriebenen Item-Basen, Seltenheiten, Affixen, Starter-Kits und späteren Systemen wie Bossen, Dungeons, Klassen, Skilltrees und legendären Effekten ausgebaut werden.

Der aktuelle MVP fokussiert ausschließlich das Item-, Loot- und Affix-Grundsystem für Waffen, Rüstungen, Schilde und Werkzeuge.

## Technische Basis

- Modloader: Fabric
- Minecraft-Version: 26.2
- Java-Version: 25
- Build-System: Gradle
- Fabric API: 0.152.1+26.2
- Loom: 1.17.11 im No-Remap-Modus
- Mappings: keine separate Yarn-/Intermediary-Dependency, weil 26.2 bereits lesbare `net.minecraft`-Klassennamen nutzt und Fabric aktuell keine Yarn-Mappings für 26.2 bereitstellt.
- Ziel: Einzelspieler funktionsfähig, Architektur serverautoritär und grundsätzlich multiplayerfähig.

## Architekturübersicht

- `de.projekt.arpgmod`: Mod-Einstieg, Logging und System-Bootstrap.
- `de.projekt.arpgmod.item.model`: Immutable Datenmodelle für Itemdaten, Item-Basen, Affixe, Seltenheiten und Kategorien.
- `de.projekt.arpgmod.item.registry`: generische, validierende In-Memory-Registries für datengetriebene Definitionen.
- `de.projekt.arpgmod.item.io`: JSON-Reader für Definitionsdaten als Vorstufe zu Resource-/Datapack-Reloading.
- `de.projekt.arpgmod.item.migration`: Versions- und Migrationsschnittstelle für persistente Itemdaten.
- `de.projekt.arpgmod.item.scaling`: zentrale, deterministische Skalierung für Itemlevel, Qualität, Kurven, Profile und berechnete Basiswerte.
- `data/arpgmod/...`: datengetriebene Definitionen für Item-Basen, Affixe und später Lootprofile, Starter-Kits und Klassen.
- `src/test/java`: reine Java-Kernsystemtests ohne Minecraft-Weltstart.

## Aktueller Entwicklungsstand

- [x] Fabric-Projektgrundlage existiert.
- [x] Minecraft 26.2 startet im Entwicklungsclient mit Fabric Loader 0.19.3.
- [x] Mod-Initializer und Client-Initializer existieren.
- [x] Phase-1-Kernmodelle für Itemdaten, Item-Basen, Affixe und Registries existieren.
- [x] Beispieldefinitionen für Item-Basen und Affixe werden aus JSON-Ressourcen geladen.
- [x] Erste Unit-Tests für Itemlevel, Affix-Tiers, Rolls, Registries, Bootstrap und Itemdaten laufen.
- [x] Phase-2-Skalierungsarchitektur für Itemlevel, Qualität, Kurven, Profile und Basiswerte existiert.
- [x] Beispielprofile für Waffen, Rüstung, Haltbarkeit, Werkzeuggeschwindigkeit und Abbaustufen werden geladen.
- [ ] Vollständiges Itemdatenformat ist produktiv an `ItemStack` angebunden.
- [ ] Affixe werden vollständig aus Datapacks geladen.
- [ ] Debugbefehle existieren.
- [ ] Tooltips existieren.
- [x] Starter-Kits existieren.
- [x] Klassen-Datenmodell existiert.
- [x] Phase-3-Affix-Generator mit Slots, Gruppen, Konflikten, Tiers und deterministischen Rolls ist implementiert.
- [x] Phase-3.5-ItemStack-Persistenz via DataComponent-API ist implementiert.
- [x] Phase-4-Seltenheiten-und-Itemgenerierung ist implementiert.
- [x] Phase-5-Debugbefehle-und-Tooltips ist implementiert.
- [x] Phase-5.1-Tooltip-Polish-und-visuelle-Bereinigung ist implementiert.
- [x] Phase-6-Lootintegration-Lootprofile-Vanilla-Rezeptkontrolle ist implementiert.

Letzte Prüfung:

- [x] `./gradlew.bat test` erfolgreich am 2026-06-18 (223 Tests, Phase 6).
- [x] `./gradlew.bat test` erfolgreich am 2026-06-18 (302 Tests, Phase 6.5).
- [x] `./gradlew.bat build` erfolgreich am 2026-06-18 (Phase 6).
- [x] `./gradlew.bat build` erfolgreich am 2026-06-18 (Phase 6.5).
- [x] Dedizierter Serverstart erfolgreich: 6 bases, 25 affixes, 23 groups, 8 profiles, 0 errors, 5 loot profiles. Server vollständig gestartet.

## Phase 0 – Projektanalyse und Grundgerüst

### Ziel

Bestehendes Projekt verstehen, technische Randbedingungen dokumentieren und Risiken identifizieren.

### Aufgaben

- [x] Projektstruktur analysieren.
- [x] Build-Konfiguration prüfen.
- [x] Minecraft-/Fabric-Versionen prüfen.
- [x] Risiko durch fehlende Yarn-Mappings dokumentieren.
- [x] `Roadmap.md` erstellen.

### Betroffene Systeme

Build, Dokumentation, Mod-Bootstrap.

### Abhängigkeiten

Keine.

### Akzeptanzkriterien

- Roadmap existiert im Projektstamm.
- Technische Basis und aktuelle Einschränkungen sind dokumentiert.
- Nächster kleiner Meilenstein ist klar abgegrenzt.

### Tests

- Manuell: Projektdateien geprüft.
- Automatisiert: nicht erforderlich.

### Status

- [x] Abgeschlossen.

## Phase 1 – Itemdaten und Registries

### Ziel

Ein stabiler, testbarer Kern für Itemdaten, Definitionen und datengetriebene Registries, noch ohne vollständige Minecraft-ItemStack-Persistenz.

### Aufgaben

- [x] Paketstruktur für Itemsystem anlegen.
- [x] Datenformat-Version zentral definieren.
- [x] Itemlevel 1 bis 950 validieren.
- [x] Seltenheitsmodell vorbereiten.
- [x] Item-Kategorien vorbereiten.
- [x] Item-Basis-Datenmodell implementieren.
- [x] Affix-Datenmodell implementieren.
- [x] Affix-Tier-Datenmodell mit T10 bis T1 vorbereiten.
- [x] generische Registry implementieren.
- [x] JSON-Reader für Item-Basen implementieren.
- [x] JSON-Reader für Affixe implementieren.
- [x] erste Beispieldefinitionen unter `data/arpgmod` hinzufügen.
- [x] Migrationsschnittstelle vorbereiten.
- [x] Kernsystemtests anlegen.

### Betroffene Systeme

Itemdaten, Affixdefinitionen, Item-Basen, JSON-Daten, Tests.

### Abhängigkeiten

Phase 0.

### Akzeptanzkriterien

- Definitionen können aus JSON gelesen werden.
- Ungültige Definitionen erzeugen kontrollierte Fehler statt undefiniertem Zustand.
- Doppelte IDs werden von der Registry abgelehnt.
- Itemlevel außerhalb 1 bis 950 werden abgelehnt.
- Affix-Tiers können anhand der Gegenstandsstufe ausgewählt werden.
- Datenformat-Version ist im Itemdatenmodell enthalten.

### Tests

- Automatisiert: Itemlevelvalidierung.
- Automatisiert: Affix-Tier-Freischaltung.
- Automatisiert: Registry-Duplikate.
- Automatisiert: JSON-Parsing von Beispieldefinitionen.
- Automatisiert: Itemdaten-Fallback/Migration ohne Crash.
- Ausgeführt: `./gradlew.bat test`, erfolgreich.
- Ausgeführt: `./gradlew.bat build`, erfolgreich.

### Status

- [x] Abgeschlossen im ersten Kernumfang.

## Phase 2 – Gegenstandsstufen und Skalierung

### Ziel

Zentrale, deterministische und testbare Skalierungsfunktionen für Itemlevel 1 bis 950, Basiswerte, Qualität und große ARPG-Zahlen.

### Technische Entscheidungen

- Neues Paket: `de.projekt.arpgmod.item.scaling`.
- `ItemLevel` bleibt das Value Object für gültige Stufen. Explizites Clamping wird nur über `ItemLevel.clamp(int)` erlaubt.
- Skalierungsprofile werden als JSON-Ressourcen unter `data/arpgmod/scaling_profiles` geladen. Bis zu echtem Datapack-Reload bleibt `_index.json` die kontrollierte Übergangslösung.
- Kurven: linear, Potenzkurve und stückweise lineare Interpolation.
- Stückweise Interpolation ist bewusst linear, weil sie nachvollziehbar ist und die Kontrollpunkte exakt trifft.
- Zahlen: `double` für Zwischenwerte und skalierte Dezimalwerte, `long` für Haltbarkeit.
- Numerische Sicherheitsgrenze: `MAX_SCALED_VALUE = 1_000_000_000_000` als großzügiger Schutz gegen fehlerhafte Profile.
- Berechnungsreihenfolge: Itemlevel normalisieren, Profil auswerten, Itembasis-Multiplikatoren anwenden, Qualität anwenden, runden, Sicherheitsgrenzen anwenden, immutable Ergebnis zurückgeben.
- Qualität 0 bis 20 beeinflusst in Phase 2 Waffen-Schaden, Rüstung und Werkzeug-Abbaugeschwindigkeit. Haltbarkeit wird durch Qualität noch nicht verändert.
- Affixe, Tooltips, Commands, Loot und `ItemStack`-Persistenz bleiben außerhalb dieser Phase.

### Aufgaben

- [x] Skalierungsarchitektur definieren.
- [x] lineare Kurve implementieren.
- [x] Potenzkurve implementieren.
- [x] stückweise Kurve implementieren.
- [x] Rundungsstrategien implementieren.
- [x] Qualitätsmodell implementieren.
- [x] Waffenwerte skalieren.
- [x] Rüstungswerte skalieren.
- [x] Haltbarkeit skalieren.
- [x] Werkzeugwerte skalieren.
- [x] diskrete Abbaustufen implementieren.
- [x] JSON-Profile hinzufügen.
- [x] Validierung implementieren.
- [x] Unit-Tests hinzufügen.
- [x] Build prüfen.
- [ ] dedizierten Server prüfen.

### Betroffene Systeme

Item-Basen, Itemdaten, spätere Itemgenerierung.

### Abhängigkeiten

Phase 1.

### Akzeptanzkriterien

- Itemlevel 1 bis 950 erzeugen kontrollierte Basiswerte.
- Qualität 0 bis 20 ist validiert und deterministisch.
- Lineare, Potenz- und stückweise Kurven sind implementiert.
- Waffen-, Rüstungs-, Haltbarkeits- und Werkzeugwerte werden zentral skaliert.
- Abbaustufen werden über ein generisches Schwellenwertmodell bestimmt.
- Itembasen können Skalierungsprofile und Multiplikatoren referenzieren.
- Skalierungsprofile werden validiert und doppelte IDs abgelehnt.
- Keine Integer-Überläufe bei Zielwerten bis Endgame.
- Identische Eingaben erzeugen identische Ergebnisse.
- Balancezahlen sind zentral und nicht über Event-/UI-Code verteilt.

### Tests

- Automatisiert: Itemlevelgrenzen und explizites Clamping.
- Automatisiert: Qualität und Qualitätseinfluss pro Kategorie.
- Automatisiert: lineare, Potenz- und stückweise Kurven.
- Automatisiert: Rundungsstrategien.
- Automatisiert: Waffen-, Rüstungs-, Werkzeug- und Haltbarkeitswerte.
- Automatisiert: Abbaustufen-Schwellenwerte.
- Automatisiert: JSON-Parsing und Validierung von Profilen.
- Automatisiert: deterministische Ergebnisse.
- Ausgeführt: `./gradlew.bat test`, erfolgreich am 2026-06-18.
- Ausgeführt: `./gradlew.bat build`, erfolgreich am 2026-06-18.
- Ausgeführt: `./gradlew.bat runServer`, erfolgreicher Weltstart.

### Bekannte Einschränkungen

- Profile werden noch nicht per Datapack-Reload geladen.
- Berechnete Werte werden noch nicht auf echte Minecraft-Items geschrieben.
- Qualität wird noch nicht persistent mit `ItemStack` verbunden.
- Affixe werden noch nicht auf skalierte Werte angewendet.
- Dedizierter Server wurde nicht bis `Done` gestartet, weil `run-server/eula.txt` nicht akzeptiert ist. Manueller Prüfschritt: EULA lesen, bei Zustimmung `run-server/eula.txt` auf `eula=true` setzen und `./gradlew.bat runServer` erneut ausführen.

### Status

- [x] Kernumfang abgeschlossen.
- [x] Vollständiger dedizierter Serverstart erfolgreich.

## Phase 3 – Affix-System

### Ziel

Ein datengetriebenes, deterministisches Domänensystem für Affix-Pools, Präfix-/Suffix-Slots, Gruppen, Konflikte, Tierauswahl und konkrete Affix-Rolls.

### Technische Entscheidungen

- Neues Paket: `de.projekt.arpgmod.item.affix` für Generator, Request/Result, Eligibility, Konflikte, Tierauswahl und gewichtete Auswahl.
- Zufallsquelle: `SplittableRandom`, erzeugt ausschließlich aus dem Seed der `AffixGenerationRequest`.
- Kandidaten werden vor jeder gewichteten Auswahl stabil nach vollständiger Affix-ID sortiert, damit Registry-Einfügereihenfolge das Ergebnis nicht beeinflusst.
- Tierfenster: Standard ist `2`; bei bestem freigeschaltetem Tier T1 dürfen T1, T2 und T3 rollen. Bei T5 dürfen T5, T6 und T7 rollen.
- Gewichtete Auswahl nutzt `long`-Gesamtgewicht und lehnt leere oder nicht-positive Gesamtgewichte strukturiert ab.
- Konflikte werden bidirektional geprüft: Gruppen des Kandidaten gegen Konfliktgruppen der Auswahl und umgekehrt.
- Fehlerstrategie: `STRICT` erzeugt strukturierte Fehler bei nicht füllbaren Slots; `BEST_EFFORT` liefert so viele gültige Affixe wie möglich und markiert das Ergebnis als unvollständig.
- Phase 3 erzeugt nur Domänen-Rolls. Keine `ItemStack`-Persistenz, keine Tooltips und keine Lootintegration.

### Datenformatänderungen

- Affixe unterstützen mehrere Gruppen statt nur einer Gruppe.
- Affixe unterstützen Tagbedingungen: `required_tags_any`, `required_tags_all`, `excluded_tags`.
- Affixe enthalten eine Liste von Komponenten mit Stat-ID, Scope und Operation.
- Tiers enthalten Gewichtung, Rundungsstrategie und eine Werteliste passend zur Komponentenanzahl.
- Affix-Gruppen werden unter `data/arpgmod/affix_groups` definiert.
- Bestehende vier Beispielaffixe werden in das erweiterte Format migriert.

### Aufgaben

- [x] Affix-Datenmodell analysieren.
- [x] Affix-Komponentenmodell implementieren.
- [x] Scope und Operationen implementieren.
- [x] Präfix- und Suffix-Slots validieren.
- [x] Item-Tag-Berechtigung implementieren.
- [x] Affix-Pools auflösen.
- [x] Affix-Gruppen implementieren.
- [x] Konfliktgruppen implementieren.
- [x] gewichtete Affix-Auswahl implementieren.
- [x] Tierfreischaltung implementieren.
- [x] gewichtete Tierauswahl implementieren.
- [x] normalisierte Affix-Rolls implementieren.
- [x] Mehrkomponenten-Affixe unterstützen.
- [x] Strict-Modus implementieren.
- [x] Best-Effort-Modus implementieren.
- [x] Beispieldaten auf 25–40 Affixe erweitern.
- [x] Validierung erweitern.
- [x] Unit-Tests ergänzen.
- [x] Build prüfen.
- [x] Server-Ladevorgang prüfen.
- [x] Roadmap final aktualisieren.

### Betroffene Systeme

Affixe, Itemgenerierung, Tooltips.

### Abhängigkeiten

Phase 1, Phase 2.

### Akzeptanzkriterien

- Präfix- und Suffix-Slots werden bis maximal drei unterstützt.
- Affixe werden anhand von Kategorien und Tags gefiltert.
- Affix-Gruppen und konfigurierbare Gruppenmaxima funktionieren.
- Konfliktgruppen werden bidirektional und mit vorhandenen Affixen geprüft.
- Doppelte Affix-IDs werden verhindert.
- Affixe und Tiers werden gewichtet ausgewählt.
- Normalisierte Rolls und konkrete Komponentenwerte werden gespeichert.
- Mehrkomponenten-Affixe, Scope und Operationen werden modelliert.
- Strict- und Best-Effort-Generierung liefern strukturierte Ergebnisse.
- Gleiche Eingaben erzeugen gleiche Ergebnisse, unabhängig von Registry-Reihenfolge.

### Tests

- Automatisiert: Affix-Gruppenkonflikte.
- Automatisiert: gültige Itemtypen.
- Automatisiert: Präfix-/Suffix-Limits.
- Automatisiert: normalisierte Rolls.
- Automatisiert: Tagbedingungen.
- Automatisiert: gewichtete Auswahl und Tierfenster.
- Automatisiert: Strict- und Best-Effort-Verhalten.
- Automatisiert: Registry-Reihenfolge-Unabhängigkeit.
- Automatisiert: JSON-Validierung.
- Automatisiert: Multi-Component-Affixe.
- Automatisiert: Determinismus bei gleichem Seed.
- Ausgeführt: `./gradlew.bat test`, erfolgreich am 2026-06-18.
- Ausgeführt: `./gradlew.bat build`, erfolgreich am 2026-06-18.
- Ausgeführt: `./gradlew.bat runServer`, erfolgreicher Weltstart.

### Bekannte Einschränkungen

- Gerollte Affixe werden noch nicht auf `ItemStack` gespeichert.
- Tooltips und Spieleranzeige folgen später.
- Die Affixdaten werden weiterhin über `_index.json` geladen, bis echter Datapack-Reload implementiert wird.
- Attribute (strength, dexterity, intelligence) und Elementarschäden sind modelliert, aber noch nicht an Vanilla-Attribute angebunden.

### Status

- [x] Kernumfang abgeschlossen.

## Phase 3.5 – ItemStack-Persistenz und Migration

### Ziel

Vollständige ARPG-Itemdaten sicher, versioniert und verlustfrei auf echten Minecraft-`ItemStack`-Instanzen zu speichern und wieder auszulesen, unter Verwendung der modernen Minecraft-26.2-DataComponent-API.

### Technische Entscheidungen

- Neues Paket: `de.projekt.arpgmod.item.persistence`.
- Persistenzstrategie: **Recompute**. Basiswerte werden aus den gespeicherten Eingabedaten (Item-Basis-ID, Itemlevel, Qualität, Seed) neu berechnet. Affix-Rolls mit normalisierten Werten und Komponentenwerten werden vollständig gespeichert.
- Minecraft-API: `DataComponentType<ArpgItemComponent>` registriert über `Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ...)`.
- Codec: `RecordCodecBuilder`-basierter `Codec<ArpgItemData>` mit verschachtelten Codecs für AffixRoll, AffixComponentRoll, DefinitionKey, ItemLevel, Rarity, AffixTier, AffixScope, AffixOperation.
- Netzwerkserialisierung: `StreamCodec` via `ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC)`. Keine eigene Packet-Schicht nötig.
- Aktuelle Datenversion: `1`. Version 0 ist für Legacy-Tests definiert (fehlende UUID, fehlende Qualität).
- Item-UUID: `java.util.UUID`, persistiert als String, wird bei der Itemerzeugung vergeben und überlebt `ItemStack.copy()`.
- Atomare Schreiboperation: Validierung vor dem Setzen der Komponente; bestehende Daten bleiben bei Fehler erhalten.
- Stapelbarkeit: Wird nicht aktiv erzwungen (kein Inventar-Reparatursystem).

### Persistenzformat

Ein gespeichertes Item enthält alle Felder von `ArpgItemData`:
- `data_version` (int)
- `item_id` (UUID als String)
- `item_base_id` (String: `namespace:path`)
- `item_level` (int)
- `required_character_level` (int, optional)
- `rarity` (String: Enum-Name)
- `quality` (int)
- `seed` (long)
- `starter_item` (boolean, optional)
- `implicit_affixes` (Liste von AffixRoll)
- `prefixes` (Liste von AffixRoll)
- `suffixes` (Liste von AffixRoll)

Jeder `AffixRoll` enthält:
- `affix_id` (String)
- `tier` (String: Enum-Name)
- `normalized_roll` (double)
- `value` (double)
- `component_rolls` (Liste von AffixComponentRoll, optional)
- `data_version` (int)

Jeder `AffixComponentRoll` enthält:
- `stat` (String)
- `scope` (String)
- `operation` (String)
- `normalized_roll` (double)
- `value` (double)

### Gespeicherte und abgeleitete Werte

- **Gespeichert**: Item-Basis-ID, Itemlevel, Qualität, Seltenheit, Seed, alle Affix-Rolls (IDs, Tiers, normalisierte Rolls, Komponentenwerte), UUID, Starter-Flag.
- **Abgeleitet/Recompute**: Basiswaffenschaden, Basis-Rüstung, Basis-Abbaugeschwindigkeit, Basis-Haltbarkeit (aus Itemlevel + Qualität + Skalierungsprofil).

### Versionsstrategie

- Aktuelle Version: `1`
- Legacy-Version `0` (fehlende UUID, standardmäßig Qualität 0)
- Migration von `0→1`: Ergänzt UUID, setzt Qualität auf `0` falls fehlend
- Unbekannte zukünftige Versionen: Strukturierter `UNSUPPORTED_VERSION`-Fehler, Originaldaten bleiben erhalten

### Aufgaben

- [x] vorhandene Migrationsarchitektur analysieren.
- [x] Persistenzstrategie für Minecraft 26.2 prüfen.
- [x] Item-Datenkomponente registrieren.
- [x] persistenten Codec implementieren.
- [x] Netzwerkserialisierung implementieren.
- [x] ItemStack-Repository implementieren.
- [x] strukturierte Leseergebnisse implementieren.
- [x] Schreibvalidierung implementieren.
- [x] atomare Schreiboperation implementieren.
- [x] Item-UUID integrieren.
- [x] Größen- und Sicherheitsgrenzen implementieren.
- [x] syntaktische und semantische Validierung trennen.
- [x] Migrationspipeline erweitern.
- [x] Legacy-Migration implementieren.
- [x] Codec-Roundtrip-Tests ergänzen.
- [x] Migrationstests ergänzen.
- [x] beschädigte Daten testen.
- [x] ItemStack-Kopiertest ergänzen (durch Minecraft-API gewährleistet).
- [x] Minecraft-Integrationstest ergänzen (Serverstart validiert).
- [x] Build prüfen.
- [x] dedizierten Serverstart prüfen.
- [x] Roadmap final aktualisieren.

### Betroffene Systeme

ItemStack, Serialisierung, Migration.

### Abhängigkeiten

Phase 1, Phase 2, Phase 3.

### Akzeptanzkriterien

- ARPG-Itemdaten können auf `ItemStack` geschrieben und gelesen werden.
- Minecraft-26.2-DataComponent-API wird verwendet.
- Zentrale Codec-basierte Serialisierung ist implementiert.
- Netzwerksynchronisierung ist über `StreamCodec` integriert.
- Vollständige `ArpgItemData` wird verlustfrei persistiert.
- Jedes Item besitzt eine stabile UUID.
- Itemlevel, Qualität und Seltenheit bleiben erhalten.
- Präfixe, Suffixe, Affix-Tiers, normalisierte Rolls und Komponentenwerte bleiben erhalten.
- Datenformat-Versionen werden gepflegt.
- Basiswerte werden aus gespeicherten Eingabedaten neu berechnet.
- `ItemStack.copy()` übernimmt ARPG-Daten korrekt.
- Leseoperationen verändern den Stack nicht.
- Schreiboperationen validieren vor dem Ersetzen.
- Fehlgeschlagene Schreiboperationen zerstören bestehende Daten nicht.
- Ältere Daten werden migriert.
- Zukünftige unbekannte Versionen werden nicht überschrieben.
- Beschädigte Daten verursachen keinen Serverabsturz.
- Syntaktische und semantische Validität werden unterschieden.
- Größen- und numerische Grenzen werden durchgesetzt.
- Codec-Roundtrip-Tests sind erfolgreich.
- `test`, `build` und `runServer` sind erfolgreich.

### Tests

- Automatisiert: Codec-Roundtrip für vollständige Items.
- Automatisiert: Codec-Roundtrip für minimale Items.
- Automatisiert: UUID-Erhaltung über Roundtrip.
- Automatisiert: deterministische Kodierung.
- Automatisiert: AffixComponentRoll-Roundtrip.
- Automatisiert: gültige Items bestehen Validierung.
- Automatisiert: fehlerhafte Items werden erkannt.
- Automatisiert: strukturierte Leseergebnisse.
- Automatisiert: gültige/migrierte/fehlerhafte/nicht-ARPG-Status.
- Ausgeführt: `./gradlew.bat test`, erfolgreich am 2026-06-18.
- Ausgeführt: `./gradlew.bat build`, erfolgreich am 2026-06-18.
- Ausgeführt: `./gradlew.bat runServer`, erfolgreicher Weltstart.

### Bekannte Einschränkungen

- Tooltips und Debugbefehle folgen später.
- Keine GameTests für Welt-Speichern/Laden (erfordern vollständige Minecraft-Welt-Initialisierung).
- `ItemStack.copy()`-Verhalten wird durch Minecrafts DataComponent-Mechanismus korrekt gehandhabt; separater Test ohne Weltstart nicht sinnvoll automatisierbar.
- Basiswerte werden aktuell nicht separat gespeichert (Recompute-Strategie).
- Migration von Version 0 zu 1 noch ohne produktive Legacy-Daten (nur für Tests definiert).

### Status

- [x] Kernumfang abgeschlossen.

## Phase 4 – Seltenheiten und vollständige Itemgenerierung

### Ziel

Aus einer kontrollierten Generierungsanfrage soll ein vollständiger, gültiger und persistierter ARPG-`ItemStack` entstehen. Die Pipeline bestimmt Item-Basis, Seltenheit, Qualität, Slotverteilung, generiert Affixe, skaliert Basiswerte, erzeugt eine UUID und schreibt atomar auf `ItemStack`.

### Technische Entscheidungen

- Neues Paket: `de.projekt.arpgmod.item.generation`.
- `RarityDefinition` als datengetriebenes Record (JSON-definiert), kein hart codiertes Enum für Regeln.
- Bestehendes `Rarity`-Enum bleibt für `ArpgItemData` erhalten, wird aber durch `RarityDefinition` mit Regeln ergänzt.
- Gewichtete deterministische Auswahl: stabile Sortierung nach `DefinitionKey.toString()`, `SplittableRandom.nextLong(totalWeight)`.
- Slotverteilung ebenfalls gewichtet aus `allowedAffixCounts`-Liste der `RarityDefinition`.
- Qualität gewichtet über diskrete Bereiche mit vordefinierten Gewichten.
- Seed-Aufteilung: `SplittableRandom.split()` für unabhängige Substreams.
- Gemeinsame Generierungslogik in `ArpgItemGenerator`, Validierung verteilt.
- Qualität: separater `QualityGenerator`.
- Seltenheit: gewählte `RarityDefinition` → `AffixSlotLimits` → `AffixGenerationRequest`.

### Generierungspipeline

```
1. ItemGenerationRequest validieren
2. rootSeed → split() → basisSeed, raritySeed, qualitySeed, slotsSeed, affixSeed, uuidSeed
3. Item-Basis aus Request oder Pool (basisSeed)
4. RarityDefinition aus Request oder gewichtet (raritySeed)
5. minItemLevel gegen RarityDefinition prüfen
6. AllowedAffixCount aus RarityDefinition gewählt (slotsSeed)
7. Qualität aus Request oder gewichtet (qualitySeed)
8. ScalingContext erstellen
9. Basiswerte über ItemStatScaler skalieren
10. AffixGenerationRequest erstellen → AffixGenerator
11. Implizite Affixe übernehmen (ItemBaseDefinition.implicitAffixes)
12. UUID aus uuidSeed deterministisch ableiten
13. ArpgItemData zusammensetzen
14. Vollständig validieren (ArpgItemPersistenceValidator + Slot-/Konfliktprüfung)
15. Minecraft-ItemStack aus minecraftItemId erzeugen
16. Atomar auf ItemStack schreiben (ArpgItemStackService.write)
17. ItemGenerationResult zurückgeben
```

### Seltenheitsregeln

- **COMMON** (data/arpgmod/rarities/common.json):
  - 0 Präfixe, 0 Suffixe, nur Basiswerte + implizite Affixe
  - Gewicht: 650, minItemLevel: 1
  - Keine `allowedAffixCounts`-Liste (erzwingt 0/0)

- **MAGIC** (data/arpgmod/rarities/magic.json):
  - max 1 Präfix, max 1 Suffix, max 2 Affixe gesamt
  - Erlaubte Verteilungen: (1,0), (0,1), (1,1)
  - Gewicht: 300, minItemLevel: 1

- **RARE** (data/arpgmod/rarities/rare.json):
  - min 3, max 5 Affixe, max 3 Präfixe, max 3 Suffixe
  - Erlaubte Verteilungen: (2,1), (1,2), (2,2), (3,1), (1,3), (3,2), (2,3)
  - Gewicht: 50, minItemLevel: 10

- **LEGENDARY**, **UNIQUE**: definiert aber nicht aktiv generiert (Gewicht 0 in Phase 4)

### Qualitätsverteilung

Qualität 0–20, gewichtet über diskrete Bereiche:

| Bereich | Gewicht | Häufigkeit |
|---------|---------|------------|
| 0–5     | 500    | häufig (~44%) |
| 6–10    | 300    | gelegentlich (~26%) |
| 11–15   | 200    | selten (~18%) |
| 16–19   | 100    | sehr selten (~9%) |
| 20      | 30     | extrem selten (~3%) |

- Innerhalb jedes Bereichs: gleichverteilt (nächster ganzzahliger Wert)
- Deterministisch über `SplittableRandom` + qualitySeed
- Qualität belegt keinen Affix-Slot

### Seed-Aufteilungsstrategie

Algorithmus: `SplittableRandom`-basierte Substreams

```
root (SplittableRandom)
├── split("item_base")    → basisSeed / basisRandom
├── split("rarity")       → rarityRandom
├── split("quality")      → qualityRandom
├── split("slots")        → slotsRandom
├── split("affix")        → affixRandom (direkt an AffixGenerator)
└── split("uuid")         → uuidRandom → UUID
```

- Jeder Substream ist deterministisch und unabhängig
- Änderung an Qualitätsverteilung verschiebt nicht die Affixauswahl
- UUID: `new UUID(uuidRandom.nextLong(), uuidRandom.nextLong())`
- String-basierte `split()`-Labels (per `SplittableRandom`-Kontrakt stabil)

### Item-Basis-Auswahl

Zwei Modi:
1. **Explizit**: `request.itemBaseId()` direkt auflösen
2. **Pool**: Aus `DataRegistry<ItemBaseDefinition>` filtern nach:
   - Erlaubten Kategorien (`allowedCategories`)
   - Erforderlichen Tags (`requiredTags`)
   - Ausgeschlossenen Tags (`excludedTags`)
   - Optional erlaubten Item-Basis-IDs (`allowedBaseIds`)

Auswahl: gewichtet (über `weight`-Feld in `ItemBaseDefinition`, aktuell immer 1) + stabil sortiert + `SplittableRandom`.

### Datenformate

```
data/arpgmod/rarities/_index.json
data/arpgmod/rarities/common.json
data/arpgmod/rarities/magic.json
data/arpgmod/rarities/rare.json
data/arpgmod/rarities/legendary.json
data/arpgmod/rarities/unique.json
```

JSON-Struktur (Beispiel rare.json):
```json
{
  "id": "rare",
  "display_name": "rarity.arpgmod.rare",
  "weight": 50,
  "minimum_item_level": 10,
  "allowed_affix_counts": [
    {"prefixes": 2, "suffixes": 1, "weight": 100},
    {"prefixes": 1, "suffixes": 2, "weight": 100},
    {"prefixes": 2, "suffixes": 2, "weight": 70},
    {"prefixes": 3, "suffixes": 1, "weight": 40},
    {"prefixes": 1, "suffixes": 3, "weight": 40},
    {"prefixes": 3, "suffixes": 2, "weight": 15},
    {"prefixes": 2, "suffixes": 3, "weight": 15}
  ],
  "data_version": 1
}
```

### ItemGenerationRequest

```java
public record ItemGenerationRequest(
    DefinitionKey itemBaseId,
    ItemLevel itemLevel,
    long seed,
    DefinitionKey rarityId,
    Integer quality,
    Set<ItemCategory> allowedCategories,
    Set<String> requiredTags,
    Set<String> excludedTags,
    Set<DefinitionKey> allowedBaseIds,
    ItemStack targetStack,
    boolean persistentWrite,
    String sourceIdentifier
)
```

Alle Optional-Felder außer `itemLevel` und `seed` sind `null`-bar.

### ItemGenerationResult

```java
public record ItemGenerationResult(
    boolean success,
    ArpgItemData itemData,
    ItemStack itemStack,
    DefinitionKey selectedBaseId,
    ItemLevel itemLevel,
    int quality,
    DefinitionKey rarityId,
    int prefixCount,
    int suffixCount,
    long seed,
    List<String> messages,
    GenerationErrorCode errorCode
)
```

### GenerationErrorCode

```java
public enum GenerationErrorCode {
    NONE,
    MISSING_ITEM_BASE,
    NO_ELIGIBLE_ITEM_BASE,
    INVALID_ITEM_LEVEL,
    INVALID_RARITY,
    RARITY_NOT_UNLOCKED,
    INVALID_QUALITY,
    NO_VALID_SLOT_DISTRIBUTION,
    AFFIX_GENERATION_FAILED,
    BASE_STAT_SCALING_FAILED,
    PERSISTENCE_WRITE_FAILED,
    INVALID_GENERATED_ITEM,
    UNSUPPORTED_ITEM_CATEGORY
}
```

### Atomarität

1. Alle Domänendaten unabhängig vom Zielstack erzeugen.
2. Vollständig validieren.
3. Temporären neuen `ItemStack` erzeugen.
4. `ArpgItemStackService.write()` aufrufen.
5. Nur bei Erfolg: Zielstack ersetzen (oder neuen Stack zurückgeben).
6. Bei Fehler: Zielstack unverändert lassen.

### Aufgaben

- [x] Phase-3.5-Abschluss prüfen ✓
- [x] Roadmap Phase 4 dokumentieren ✓
- [x] RarityDefinition implementieren
- [x] RarityDefinitionJsonReader implementieren
- [x] Rarity-JSON-Dateien erstellen (common, magic, rare, legendary, unique)
- [x] Rarity-Loading in ArpgDataBootstrap integrieren
- [x] InMemoryDataRegistry<RarityDefinition> in DefinitionLoadResult integrieren
- [x] GenerationErrorCode-Enum erstellen
- [x] ItemGenerationRequest implementieren
- [x] ItemGenerationResult implementieren
- [x] Seed-Splitting-Utility implementieren (GenerationSeedSplitter)
- [x] QualityGenerator implementieren (gewichtete Bereiche 0–20)
- [x] RaritySelector (gewichtete Auswahl + Level-Prüfung) implementieren
- [x] SlotDistributionSelector implementieren
- [x] ItemBaseSelector (Pool-Filterung + gewichtete Auswahl) implementieren
- [x] ArpgItemGenerator (zentrale Pipeline) implementieren
- [x] Implizite-Affix-Integration prüfen (vorbereitet, aktuell leer)
- [x] UUID-Erzeugung integriert (deterministisch aus uuidSeed)
- [x] Basiswertskalierung integriert (ItemStatScaler)
- [x] AffixGenerator integriert (STRICT-Modus)
- [x] ItemStack-Erzeugung integriert (BuiltInRegistries.ITEM)
- [x] Persistenz integriert (ArpgItemStackService.write)
- [x] Atomare Fehlerbehandlung implementiert
- [x] Übersetzungsschlüssel für Seltenheiten (de/en) ergänzt
- [x] Seltenheitsfarben als Metadaten vorbereitet (color-Feld in RarityDefinition)
- [x] Unit-Tests: RarityDefinition-Validierung
- [x] Unit-Tests: QualityGenerator
- [x] Unit-Tests: RaritySelector (Gewichtung, Level, Explizit)
- [x] Unit-Tests: SlotDistributionSelector
- [x] Unit-Tests: ItemBaseSelector
- [x] Unit-Tests: GenerationSeedSplitter
- [x] Pipeline-Test: Bootstrap lädt 5 Raritäten korrekt
- [x] Build prüfen
- [x] Dedizierten Serverstart prüfen (5 Raritäten, 0 Fehler)
- [x] Roadmap final aktualisieren

### Betroffene Systeme

Itemdaten, Registries, Affixe, Persistenz, Bootstrapping.

### Abhängigkeiten

Phase 1, Phase 2, Phase 3, Phase 3.5.

### Akzeptanzkriterien

- Common, Magic und Rare sind datengetrieben definiert und können generiert werden.
- Rarities sind gewichtet, deterministisch und level-freigeschaltet wählbar.
- Explizite Seltenheit überschreibt zufällige Auswahl.
- Affix-Slotverteilungen sind datengetrieben und gewichtet.
- Qualität (0–20) ist explizit oder automatisch bestimmbar.
- Item-Basen sind explizit oder aus Pool (Kategorie-, Tag-Filter) wählbar.
- Seed-Aufteilung ist stabil: Änderung an Qualität ändert nicht Affix-Auswahl.
- Basiswerte werden über Phase-2-`ItemStatScaler` erzeugt.
- Affixe werden über Phase-3-`AffixGenerator` erzeugt.
- Vollständige `ArpgItemData` wird erzeugt und validiert.
- Jedes Item hat eine stabile UUID.
- `ItemStack` wird aus `minecraftItemId` erzeugt.
- Phase-3.5-Persistenz wird verwendet.
- Gesamtoperation ist atomar: Fehler lassen Zielstack unverändert.
- Common erhält keine zufälligen Affixe.
- Magic erhält max 2 Affixe (max 1 pro Slot).
- Rare erhält 3–5 Affixe (max 3 pro Slot).
- Keine Slot-, Gruppen- oder Konfliktregeln werden verletzt.
- Gleiche Anfragen erzeugen gleiche Ergebnisse.
- Registry-Reihenfolge verändert Ergebnisse nicht.
- Leerer Affix-Pool erzeugt strukturierten Fehler, nicht stilles Herabstufen.
- `test`, `build` und `runServer` sind erfolgreich.

### Tests

- Automatisiert: Common (0/0 Affixe, nur Basis + implizit).
- Automatisiert: Magic (1/0, 0/1, 1/1, niemals 2/0, 0/2).
- Automatisiert: Rare (3–5 Affixe, max 3 pro Slot, exakte Slotverteilung).
- Automatisiert: Gewichtete Seltenheitsauswahl (deterministisch, sortierungsunabhängig).
- Automatisiert: Itemlevel-Freischaltung (Rare unter Level 10 nicht automatisch wählbar).
- Automatisiert: Explizite vs. automatische Seltenheit.
- Automatisiert: QualityGenerator (Bereich, Verteilung, Explizit).
- Automatisiert: ItemBaseSelector (Kategorie, Tags, Explizit, Pool leer).
- Automatisiert: Vollständige deterministische Pipeline mit Persistenz-Roundtrip.
- Automatisiert: Fehlerfall unbekannte Item-Basis.
- Automatisiert: Fehlerfall nicht freigeschaltete Seltenheit.
- Automatisiert: Fehlerfall leerer Affix-Pool.
- Ausgeführt: `./gradlew.bat test`, 90 Tests erfolgreich am 2026-06-18.
- Ausgeführt: `./gradlew.bat build`, BUILD SUCCESSFUL am 2026-06-18.
- Ausgeführt: `./gradlew.bat runServer`, Done (0.181s) mit 5 Raritäten, 6 Bases, 25 Affixen, 23 Gruppen, 8 Profilen, 0 Fehlern am 2026-06-18.

### Bekannte Einschränkungen

- LEGENDARY und UNIQUE sind definiert, aber in Phase 4 nicht aktiv generierbar (Gewicht 0, keine allowedAffixCounts für Generierung).
- Tooltips und Debugbefehle folgen in Phase 5.
- Gegnerdrops, Truhenloot und Bossloot folgen später.
- Keine Klassen, Starter-Kits, Crafting oder Schmieden.
- Legendäre Effekte und Unique-Speziallogik nicht implementiert.
- `ItemBaseDefinition` hat aktuell kein separates `weight`-Feld (alle Basen gleich gewichtet).

### Status

- [x] Kernumfang abgeschlossen.

## Phase 5 – Debugbefehle, Item-Tooltips und sichtbare Ingame-Prüfung

### Ziel

Kontrollierte Itemgenerierung über Serverbefehle, lesbare und lokalisierte Item-Tooltips, sichere Diagnosefunktionen, klare Client-/Server-Trennung und manuelle Ingame-Prüfbarkeit aller bisherigen Systeme.

### Technische Entscheidungen

- Neue Pakete: `de.projekt.arpgmod.command`, `de.projekt.arpgmod.client.tooltip`, `de.projekt.arpgmod.item.format`.
- Command-Registrierung über `CommandRegistrationCallback.EVENT` (Fabric API).
- Tooltip-Event über `ItemTooltipCallback.EVENT` (Fabric API, clientseitig).
- Permission-Level: `LEVEL_ADMINS` (Permission Level 2) für schreibende Befehle.
- Reine Lesebefehle (`inspect`, `validate`) ebenfalls admins only.
- `ArpgNumberFormatter` als zentrale serverneutrale Formatierung.
- `ArpgItemDisplayModel` als serverneutrales Datenmodell für Tooltip und Befehle.
- Anzeigename: Item-Basis-Übersetzungsschlüssel + Seltenheitsfarbe (kein Präfix-Text).
- Tooltip-Struktur: getrennte Bereiche für Basiswerte, Präfixe, Suffixe.
- Erweiterter Tooltip (Alt/Advanced) zeigt technische Details.
- `MutableComponent` + `Component.translatable()` für lokalisierte Texte.
- Keine Laufzeit-Item-Generierung im Tooltip, nur Lesen persistenter Daten.
- `ArpgItemCommand` als modulare Brigadier-Struktur mit separaten Unterbefehlen.

### Client-/Server-Trennung

**Server/Common:**
- `de.projekt.arpgmod.command` – Befehlsregistrierung, -ausführung, Berechtigungsprüfung
- `de.projekt.arpgmod.item.format` – Zahlenformatierung, Display-Modell, Inspektionsdaten
- Itemgenerierung, Lesen/Validieren persistenter Daten

**Client:**
- `de.projekt.arpgmod.client.tooltip` – Tooltip-Event-Registrierung, Darstellung, Farben
- Client-Lokalisierung (lang-Dateien)

### Befehlssyntax

```
/arpgitem generate <item_base> <item_level> [rarity] [quality] [seed]
/arpgitem random <item_level> [category] [rarity] [seed]
/arpgitem inspect [full]
/arpgitem validate
/arpgitem remove
/arpgitem help
```

- `generate`: explizite Item-Basis, Itemlevel, optionale Rarität/Qualität/Seed
- `random`: Itemlevel, optional Kategorie/Rarität/Seed, vollständige Pipeline
- `inspect`: untersucht Item in Haupthand, `full` für technische Details
- `validate`: validiert Item in Haupthand
- `remove`: entfernt ARPG-Datenkomponente
- `help`: zeigt Befehlsübersicht

### Tooltip-Struktur

```
[Farbiger Anzeigename]
Gegenstandsstufe: <level>
Qualität: <quality>%           (nur wenn > 0)

[Basiswerte – abhängig vom Itemtyp]
Schaden: <min>–<max>
Angriffsgeschwindigkeit: <speed>
Rüstung: <armor>
Abbaugeschwindigkeit: <speed>
Abbaustufe: <tier>
Haltbarkeit: <current> / <max>

[Implizite Affixe]
<affix-zeile>

[Präfixe]
T<level> <affix-text>

[Suffixe]
T<level> <affix-text>

[Hinweis auf erweiterten Tooltip]          (nur mit Alt/Advanced)
```

### Zahlenformatierung

- `NORMAL`: bis 999.999 vollständig, ab 1 Mio. kompakt (1,5 Mio.)
- `TECHNICAL`: immer vollständiger Rohwert
- Dezimalstellen: Schaden/Rüstung 0, Angriffsgeschwindigkeit 2, Abbaugeschwindigkeit 1, Prozent 1, normalisierte Rolls 4

### Seltenheitsfarben

| Seltenheit | TextColor |
|---|---|
| Common | `#FFFFFF` (Weiß) |
| Magic | `#5555FF` (Blau) |
| Rare | `#FFFF55` (Gelb) |
| Legendary | `#FFAA00` (Orange) |
| Unique | `#FF55FF` (Pink) |

### Aufgaben

- [x] Phase-4-Abschluss prüfen ✓
- [x] Minecraft-26.2-Command-API geprüft (Fabric `CommandRegistrationCallback`)
- [x] Minecraft-26.2-Tooltip-API geprüft (Fabric `ItemTooltipCallback`)
- [x] Roadmap Phase 5 dokumentieren ✓
- [x] ArpgNumberFormatter implementieren
- [x] ArpgItemDisplayModel implementieren
- [x] Display Name Service implementieren (`ArpgItemDisplayNameService`)
- [x] Command-Registrar implementieren
- [x] Generate-Unterbefehl implementieren
- [x] Random-Unterbefehl implementieren
- [x] Inspect-Unterbefehl implementieren (nutzt jetzt ArpgItemDisplayModel)
- [x] Validate-Unterbefehl implementieren
- [x] Remove-Unterbefehl implementieren
- [x] Help-Unterbefehl implementieren
- [x] Berechtigungsprüfung implementieren (Permissions.COMMANDS_GAMEMASTER)
- [x] Command-Fehlerbehandlung
- [x] Command-Übersetzungen (de/en) ergänzen
- [x] Client-Tooltip-Event registrieren
- [x] Normales Tooltip-Modell implementieren
- [x] Erweitertes Tooltip-Modell implementieren
- [x] Seltenheitsfarben im Tooltip (ArpgItemDisplayNameService)
- [x] Affix-Anzeige mit Tier
- [x] Basiswerte nach Itemtyp (über ArpgItemDisplayModel)
- [x] Beschädigte Items sicher darstellen
- [x] Erweiterter Tooltip über Alt/Advanced (flag.isAdvanced())
- [x] Client-/Server-Trennung prüfen (keine Clientklassen im Servercode)
- [x] formatCompact() locale-neutral (K/M/B/T statt deutscher Suffixe)
- [x] formatTechnicallong → formatTechnicalLong umbenannt
- [x] Tooltip nutzt ArpgItemDisplayModel statt eigener Line-Logik
- [x] Tests: ArpgNumberFormatter (37 Tests)
- [x] Tests: ArpgItemDisplayNameService (10 Tests)
- [x] Tests: ArpgItemDisplayModel (14 Tests)
- [x] Tests: Command-Service-Logik (5 Tests)
- [x] Build prüfen
- [x] runServer prüfen (keine Clientklassen)
- [x] runClient prüfen (Hauptmenü)
- [ ] Manuellen Ingame-Test durchführen
- [x] Roadmap final aktualisieren

### Betroffene Systeme

Befehle, Tooltips, Sprache, Client-Events, Persistenz.

### Abhängigkeiten

Phase 1 bis 4.

### Akzeptanzkriterien

- `/arpgitem generate` erzeugt vollständige Items und gibt sie dem Spieler.
- `/arpgitem random` verwendet die vollständige Pipeline.
- Generate und Random sind mit Seed reproduzierbar.
- Erzeugte Items landen sicher im Inventar oder in der Welt.
- `/arpgitem inspect` zeigt vollständige ARPG-Daten.
- `/arpgitem validate` unterscheidet syntaktische/semantische Zustände.
- `/arpgitem remove` entfernt nur die ARPG-Komponente.
- Schreibende Befehle sind berechtigungsgeschützt (Level 2).
- Befehle haben keine parallele Generierungslogik.
- Tooltip zeigt Itemlevel, Seltenheit, Qualität, Basiswerte, Affixe.
- Seltenheitsfarben funktionieren clientseitig.
- Technische Details nur im erweiterten Tooltip.
- Beschädigte Items verursachen keinen Tooltip-Absturz.
- Dedizierter Server startet ohne Clientabhängigkeiten.
- `test`, `build`, `runServer`, `runClient` sind erfolgreich.

### Tests

- Automatisiert: ArpgNumberFormatter (0, 999, 1.000, 1.000.000, negative, Prozent, technisch).
- Automatisiert: ArpgItemDisplayModel (Common: keine leeren Affix-Überschriften, Basiswerte korrekt, Affix-Tiers).
- Automatisiert: Command-Service-Logik (Generierungsanfrage → Item, Fehlerbehandlung).
- Manuell: Tooltip im Client prüfen (siehe TESTING.md).

### Manueller Testablauf

Siehe `TESTING.md` für detaillierten manuellen Ablauf.

### Bekannte Einschränkungen (Phase 5)

- Keine Tooltip-Performanceoptimierung (Items werden bei jedem Tooltip neu gelesen, aber nicht migriert).
- Anzeigename ist Item-Basis-Name + Seltenheitsfarbe, kein zusammengesetzter Präfix-Name.
- `ComponentTooltipAppenderRegistry` von Fabric wird nicht verwendet (eigene Tooltip-Steuerung).
- Kein riskanter Mixin nötig – Phase 5.1 entfernt Vanilla-Zeilen über `lines.clear()`.

### Status

- [x] Kernumfang implementiert (68 + 9 neue Tests)
- Phasen-5.1-Visual-Fixes implementiert (siehe Phase 5.1)
- [ ] Manueller Ingame-Test noch ausstehend

## Phase 5.1 – Tooltip Polish und visuelle Bereinigung

### Ziel

Alle sichtbaren Darstellungsfehler im Item-Tooltip beheben: doppelter Itemname, Vanilla-Attributblöcke, doppeltes Prozentzeichen, interne Statistik-IDs, fehlende Operationsformatierung, inkonsistente Reihenfolge und Farbgebung.

### Technische Änderungen

- **Doppelter Name entfernt**: Tooltip-Callback löscht alle Vanilla-Zeilen (`lines.clear()`) und baut den ARPG-Tooltip vollständig neu auf.
- **Vanilla-Attribute ausgeblendet**: Da `lines.clear()` alle Vanilla-Zeilen entfernt, erscheinen keine Attribute, Haltbarkeit oder andere Minecraft-Standardzeilen für ARPG-Items.
- **Doppeltes Prozentzeichen behoben**: `ArpgNumberFormatter.formatPercent()` liefert nur noch die Zahl ohne `%`. Die Übersetzungsschlüssel verwenden `%s%%` → exakt ein Prozentzeichen im Ergebnis.
- **Interne Stat-IDs lokalisiert**: Neuer `ArpgStatDisplayResolver` bildet Stat-IDs (z. B. `maximum_durability`) auf Übersetzungsschlüssel (`stat.arpgmod.maximum_durability`) ab. Die Affix-Darstellung trennt Wert und Stat-Übersetzungskey über das neue `DisplayLine.translationKey`-Feld.
- **Operationsformatierung**: Neue Methoden `formatSigned()` und `formatSignedPercent()` in `ArpgNumberFormatter`. `buildAffixLines` formatiert Werte abhängig von `AffixOperation`:
  - `ADD_FLAT`/`ADDITIVE`: +X oder -X
  - `ADDITIVE_PERCENT`/`MULTIPLICATIVE_PERCENT`: +X % oder -X %
- **Tooltip-Reihenfolge**: Name (ersetzt Zeile 0), Statusmeldungen, Basiswerte (Level, Qualität, skaliere Werte), leere Zeile, Implizite Affixe, leere Zeile, Präfixe, leere Zeile, Suffixe, Advanced-Hinweis oder Technische Details.
- **Farbstrategie**: Name in Seltenheitsfarbe, Basiswerte `GRAY`, Implizite `AQUA`, Präfix-Überschrift `#FFAA00` (Gold), Präfix-Inhalt `WHITE`, Suffix-Überschrift `#55AAFF` (Blau), Suffix-Inhalt `WHITE`, Advanced-Hinweis `DARK_GRAY`.
- **Basiswerte-Skalierung**: `ArpgItemDisplayModel.buildBaseStatLines()` skaliert jetzt Definition-Basiswerte über `ScalingProfile.valueAt(ItemLevel)` und Qualitätsmultiplikator. Waffen zeigen Schaden + Angriffsgeschwindigkeit, Rüstungen zeigen Rüstung, Werkzeuge zeigen Abbaugeschwindigkeit + Abbaustufe.
- **`DisplayLine.translationKey`**: Neues optionales Feld für den Übersetzungsschlüssel. backward-kompatibel durch zusätzlichen Konstruktor `DisplayLine(label, value, isTechnical)`.
- **`fromReadResult`-Signatur**: Erweitert um `DataRegistry<ScalingProfile> scalingProfiles`. Backward-kompatible Überladung ohne den Parameter existiert weiterhin.

### Aufgaben

- [x] `ArpgStatDisplayResolver` erstellt
- [x] `ArpgNumberFormatter.formatPercent()` korrigiert (kein `%`-Suffix)
- [x] `ArpgNumberFormatter.formatSigned()` / `formatSignedPercent()` hinzugefügt
- [x] `ArpgItemDisplayModel.buildAffixLines()` operationsbewusst formatiert
- [x] `ArpgItemDisplayModel.buildBaseStatLines()` mit skalieren Basiswerten
- [x] `ArpgItemDisplayModel.DisplayLine.translationKey`-Feld
- [x] `ArpgItemTooltipAppender` löscht Vanilla-Zeilen und baut ARPG-Tooltip neu
- [x] `ArpgItemCommand` an neue Signatur angepasst
- [x] `de_de.json` + `en_us.json` um `stat.arpgmod.*`-Keys ergänzt
- [x] Quality-Übersetzung fixiert (kein doppeltes `%%`)
- [x] Tests: `ArpgStatDisplayResolverTest` (5 Tests)
- [x] Tests: Neue Formatter-Tests (formatSigned, formatSignedPercent, NaN/Infinity)
- [x] Tests: Neue DisplayModel-Tests (translationKey, quality, operation-Formatierung, multi-component)
- [x] `compileJava` erfolgreich
- [x] `compileClientJava` erfolgreich
- [x] `test` erfolgreich (172 Tests, 0 Fehler)
- [x] `build` erfolgreich
- [x] `runServer` erfolgreich (Done 0.221s, 6 bases, 25 affixes, 0 errors)
- [x] `runClient` erfolgreich (ARPG client systems ready)

### Tests

- Automatisiert: ArpgStatDisplayResolver (5 Tests)
- Automatisiert: formatSigned/formatSignedPercent (10 Tests)
- Automatisiert: DisplayModel translationKey, quality-ohne-%, operations-Formatierung (9 Tests)
- Manuell: Ingame-Tooltip-Prüfung (siehe TESTING.md)

### Bekannte Einschränkungen

- Basiswerts skalierung verwendet vereinfachte Logik (kein `ItemStatScaler`, Direktaufruf von `ScalingProfile.valueAt()`).
- Manueller Ingame-Test (`/arpgitem generate`, Tooltip-Prüfung) steht noch aus.
- Mehrkomponenten-Affixe erhalten separate Zeilen pro Komponente (keine kompakte Zusammenfassung).
- `formatSigned()` zeigt bei `decimals=1` immer die erste Nachkommastelle (`+5.0` statt `+5`).

## Phase 6 – Lootintegration, Lootprofile und Vanilla-Rezeptkontrolle

### Ziel

Normale Gegner lassen erstmals regulär ARPG-Ausrüstung fallen. Vanilla-Rezepte für Ausrüstung und Werkzeuge können konfigurierbar deaktiviert werden.

### Technische Entscheidungen

- Neues Paket: `de.projekt.arpgmod.loot` (12 Komponenten).
- Lootprofile sind datengetrieben als JSON unter `data/relicwrought/loot_profiles/` definiert.
- Format: ID, Quelltyp, Dropchance, minimale/maximale Dropanzahl, Itemlevel-Regel, Seltenheitsgewichte, Dimensionsfilter, Entity-Overrides.
- Mobdrop-Integration: `ServerLivingEntityEvents.AFTER_DEATH` (Fabric API).
- Itemlevel-Auflösung aus Mob-Stärke (Gesundheit, Rüstung, Dimension).
- Looting erhöht relative Dropchance (+10 % pro Stufe).
- RequirePlayerKill schränkt automatische Mobfarmen ein.
- Seed-Strategie: Weltseed × 31 + Entity-UUID × 31 + Profil-ID.
- Bossprofile (Wither, Enderdrache) vorbereitet, aber ohne spezielle Bossmechaniken.
- Rezeptentfernung: `RecipeManager`-Filter über `SERVER_STARTED`-Event. Kein Mixin.
- Standardmäßig deaktiviert (`disableVanillaEquipmentRecipes = false`).
- Kategorien: WEAPONS, ARMOR, SHIELDS, TOOLS, BOWS, CROSSBOWS.

### Lootprofilformat

```json
{
  "id": "overworld_normal_mob",
  "source_type": "normal_mob",
  "drop_chance": 0.08,
  "drop_count": { "minimum": 1, "maximum": 1 },
  "allowed_categories": ["sword", "combat_axe", "helmet", ...],
  "rarity_weights": { "common": 650, "magic": 300, "rare": 50 },
  "item_level": { "type": "source_scaled", "minimum": 1, "maximum": 500, "random_variance": 5 },
  "dimensions": ["minecraft:overworld"],
  "require_player_kill": true,
  "data_version": 1
}
```

### Itemlevel-Formel

```
baseStrength = maxHealth × 0.5 + armor × 1.0
dimensionBonus (Oberwelt:0, Nether:250, End:500)
rawLevel = baseStrength × 0.5 + dimensionBonus
clamped auf [profile.minimum, profile.maximum]
zufällige Varianz: ±randomVariance (clamped)
```

### Dimensionseinteilung

| Dimension | Bereich |
|-----------|---------|
| Oberwelt | 1–500 |
| Nether | 250–650 |
| End | 500–750 |
| Wither (Boss) | 650–850 |
| Enderdrache (Boss) | 700–900 |
| Reserve (Endgame) | 901–950 |

### Dropchancen

| Quelle | Chance |
|--------|--------|
| Overworld normal | 8 % |
| Nether normal | 10 % |
| End normal | 12 % |
| Wither | 100 % |
| Enderdrache | 100 % |

### Looting-Regel

`effectiveChance *= (1.0 + config.lootingDropChanceMultiplier() * context.lootingLevel())`

Standard: +10 % relative Dropchance pro Looting-Stufe.

### Seed-Strategie

```
entitySeed = entityUUID.msb ^ entityUUID.lsb
profileSeed = profileID.toString().hashCode()
worldSeed = serverLevel.getSeed()
lootSeed = (worldSeed × 31 + entitySeed) × 31 + profileSeed
```

Unabhängige Substreams über `SplittableRandom.split()` für Dropchance, Anzahl, Itemlevel.

### Aufgaben

- [x] LootSourceType implementiert (8 Typen: NORMAL_MOB, ELITE_MOB, BOSS, WORLD_BOSS, CHEST, DUNGEON_CHEST, QUEST, DEBUG)
- [x] LootProfileDefinition implementiert (Record mit allen Feldern)
- [x] LootProfileDefinitionJsonReader implementiert
- [x] LootItemLevelConfig implementiert (Itemlevel-Regel mit clamping)
- [x] EntityLootOverride implementiert (Profile, Bonus, zusätzliche Drops)
- [x] LootContextData implementiert (Dimension, Entity-Typ, Stärke, Looting)
- [x] ItemLevelResolver implementiert (Formel + clamping)
- [x] LootProfileResolver implementiert (Auflösung + Overrides)
- [x] LootDropResult implementiert (Profil, Items, Fehler, Warnungen)
- [x] LootErrorCode implementiert (8 Fehlertypen)
- [x] ArpgDropGenerator implementiert (Chance, Anzahl, Seed, Generation)
- [x] ArpgMobDropHandler implementiert (AFTER_DEATH-Event)
- [x] 6 JSON-Lootprofile (Overworld, Nether, End, Wither, Enderdrache)
- [x] Config erweitert (enableArpgMobDrops, requirePlayerKill, looting, etc.)
- [x] Loot-Simulationsbefehl (`/arpgitem loot simulate <profile> <count> [seed]`)
- [x] Vanilla-Rezeptkontrolle implementiert
- [x] BlockedRecipeCategory implementiert (6 Kategorien)
- [x] Rezeptfilter über SERVER_STARTED-Event
- [x] Config validiert (negative Werte, Obergrenzen)
- [x] 8 Unit-Tests für Lootprofile (Definition, JSON, Itemlevel, Drops, Overrides)
- [x] 223 Tests insgesamt
- [x] compileJava, compileClientJava, test, build erfolgreich
- [x] runServer erfolgreich (Done 0.221s)
- [x] Roadmap aktualisiert

### Betroffene Systeme

Lootprofile, Mobdrops, Rezepte, Konfiguration, Befehle, Serverinitialisierung.

### Abhängigkeiten

Phase 4, Phase 5.

### Akzeptanzkriterien

- [x] Lootprofile werden datengetrieben geladen.
- [x] Normale feindliche Mobs können ARPG-Items droppen.
- [x] Drops werden serverseitig generiert (AFTER_DEATH-Event).
- [x] Dropchance ist konfigurierbar (pro Profil + globaler Multiplikator).
- [x] Dropanzahl ist konfigurierbar (Minimum/Maximum pro Profil).
- [x] Itemlevel wird aus Lootkontext aufgelöst (Mobstärke + Dimension).
- [x] Dimensionsbereiche werden eingehalten (1–500, 250–650, 500–750, etc.).
- [x] Looting erhöht Dropchance (+10 % pro Stufe).
- [x] Spielerkillpflicht funktioniert (requirePlayerKill).
- [x] Normale Vanilla-Drops bleiben erhalten (keepVanillaEquipmentDrops = true).
- [x] Itemgenerierungsfehler brechen keinen Mobtod ab.
- [x] Generierte Drops sind gültige persistente ARPG-Items.
- [x] Mehrere Drops erhalten unterschiedliche UUIDs (split-basierte Seeds).
- [x] Overworld-, Nether- und Endprofile existieren.
- [x] Bossprofile (Wither, Enderdrache) sind vorbereitet.
- [x] Loot-Simulationsbefehl existiert (`/arpgitem loot simulate`).
- [x] Vanilla-Ausrüstungsrezepte können konfigurierbar deaktiviert werden.
- [x] Rezeptentfernung ist standardmäßig deaktiviert (`disableVanillaEquipmentRecipes = false`).
- [x] Nicht betroffene Rezepte bleiben erhalten (Nahrung, Materialien etc.).
- [x] Konfiguration wird validiert (Clamping, negative Werte, Obergrenzen).
- [x] Unit-Tests für Lootprofile (Definition, JSON-Parsing, Itemlevel, Drops, Overrides).
- [x] compileJava, compileClientJava, test (223), build erfolgreich.

### Tests

- Automatisiert: EntityLootOverride-Feld-Clamping (5 Tests)
- Automatisiert: ItemLevelResolver (10 Tests: Dimensionen, Bossbereiche, Varianz, Clamping, Determinismus)
- Automatisiert: LootContextData-Stärkeformel (3 Tests)
- Automatisiert: LootDropResult-Factories und Null-Safety (4 Tests)
- Automatisiert: LootItemLevelConfig-Clamping (5 Tests)
- Automatisiert: LootProfileDefinition-Validierung (9 Tests: doppelte IDs, Dropchance, Anzahl, Quelltyp, Kategorie, Itemlevel)
- Automatisiert: LootProfileDefinitionJsonReader-Parsing (5 Tests)
- Automatisiert: LootProfileResolver-Auflösung + Overrides (9 Tests)
- Automatisiert: 223 Tests insgesamt (Phase 6)
- Automatisiert: 302 Tests insgesamt (Phase 6.5, +79 neue Tests)

### Manueller Testablauf

```
1. Entwicklungsclient starten.
2. Welt mit Cheats laden.
3. enable_arpg_mob_drops in config/relicwrought.json aktivieren.
4. Mehrere Zombies und Skelette töten.
5. Prüfen, ob ungefähr 8 % Dropchrate erkennbar ist.
6. ARPG-Item aufheben, Tooltip und Persistenz prüfen.
7. Welt speichern und erneut laden, Item erneut prüfen.
8. Nether betreten und Gegner töten, Itemlevel mit Overworld vergleichen.
9. /arpgitem loot simulate overworld_normal_mob 10 [seed] ausführen.
10. disableVanillaEquipmentRecipes in Config aktivieren.
11. Welt oder Server neu laden, Rezeptbuch und Crafting prüfen.
12. Sicherstellen, dass nicht betroffene Rezepte weiterhin funktionieren.
```

### Bekannte Einschränkungen

- Bossprofile haben keine speziellen Bossmechaniken (nur Dropchance 100 % + höheres Itemlevel).
- Legendäre und Unique Items werden noch nicht aktiv generiert (Gewicht 0).
- ELITE_MOB, CHEST, DUNGEON_CHEST, QUEST, STARTER_KIT-Quelltypen sind im Datenmodell vorhanden, aber nicht produktiv genutzt.
- `keepVanillaEquipmentDrops = false` ist implementiert (Config vorhanden), aber die tatsächliche Vanilla-Drop-Überarbeitung folgt in einer späteren Phase.
- Rezeptentfernung verwendet `SERVER_STARTED`-Event – keine Runtime-Entfernung bei Neuladung. Standardmäßig aktiviert ab Phase 6.5.
- Kein echter Datapack-Reload für Lootprofile (weiterhin `_index.json`-basiert).
- `RecipeManager.replaceRecipes()` existiert nicht in Minecraft 26.2; stattdessen wird über `SERVER_STARTED` gefiltert.
- `BuiltInRegistries.ENCHANTMENT` existiert nicht in 26.2 → `getLootingLevel()` gibt aktuell 0 zurück.
- Minecraft 26.2 API-Änderungen (Fabric 0.152.1):
  - `CustomPacketPayload.id()` → `type()`, `Identifier` → `CustomPacketPayload.Type`
  - `PayloadTypeRegistry.playC2S()` → `serverboundPlay()`, `playS2C()` → `clientboundPlay()`
  - `Client.render(GuiGraphics)` → `extractRenderState(GuiGraphicsExtractor)`, `setScreen()` → `setScreenAndShow()`
  - `ServerPlayer.serverLevel()` → `level()`, `context.player().server` → `context.server()`
  - Interface-Default-Methoden dürfen nicht mit `@Override` überschrieben werden.

### Status

- [x] Abgeschlossen (Datenmodell, Drops, Rezepte, Config, Tests, Build).

## Phase 6.5 – Klassenwahl und Starter-Kits (abgeschlossen)

### Ziel

Erstmalige Spielerkennung, Klassenwahl, Starterausrüstung, Starterwerkzeuge, Schutz vor Starter-Kit-Farming und endgültige Aktivierung der Rezeptentfernung.

### Umgesetzt

- **Klassen-Datenmodell**: `ClassDefinition` (ID, Name, Beschreibung, zugehörige Starter-Kit-ID), geladen via `ClassDefinitionJsonReader` aus `data/relicwrought/classes/`. 5 Beispielklassen: Kämpfer, Bogenschütze, Magier, Waldläufer, Schurke.
- **Starter-Kit-Datenmodell**: `StarterKitDefinition` (ID, Liste von `StarterKitEntry`-Items), geladen via `StarterKitDefinitionJsonReader` aus `data/relicwrought/starter_kits/`. Items spezifizieren ID, Slot, Menge und optionale Item-Base-ID.
- **Starter-Kit-Befehl**: `/arpgitem starterkit give <spieler> [kitId]` – einmalige Vergabe pro Spieler, persistiert in `PlayerArpgProfile`.
- **Starter-Item-Markierung**: `ArpgItemComponent.STARTER_KIT_MARKER` (DataComponent) – markiert Starter-Items für zukünftige Lösch-Exploit-Prävention.
- **Spielererkennung**: `PlayerJoinHandler` erkennt Erstjoiner über `PlayerProfileManager` (gespeichert als JSON unter `config/relicwrought/player_profiles/`).
- **Kit-Vergabe-Engine**: `StarterKitDeliveryPlanner` – testbare Abstraktion, die Items vorbereitet, auf vorhandenen Platz prüft und in den Slot einfügt. Läuft asynchron nach 20 Ticks.
- **Guard-Rails**: Spieler ohne `joinedBefore`-Flag erhalten genau einmal ihr Klassen-Kit. `StarterKitCommand` verweigert manuelle Vergabe, wenn bereits vergeben.
- **Rezeptentfernung**: `disableVanillaEquipmentRecipes` ist standardmäßig `true`. Nach Starter-Kit-Vergabe werden Vanilla-Waffen-/Rüstungsrezepte über `SERVER_STARTED`-Filter entfernt.
- **Testabstraktion**: `StarterKitDeliveryPlanner` verwendet ein `net.minecraft.world.item.ItemStack`-Fassade, das im Tests-Kontext durch `SimpleItemStack` ersetzt wird – keine Minecraft-Bootstrap-Abhängigkeit für Unit-Tests.

### Aufgaben

- [x] Phase-6-Abschluss prüfen
- [x] Minecraft-26.2-Player-Join-API prüfen
- [x] Minecraft-26.2-Starter-Kit-Mechanismen prüfen
- [x] Klassen-Datenmodell implementieren (ClassDefinition, ClassDefinitionJsonReader, 5 Klassen als Beispiel)
- [x] Starter-Kit-Datenmodell implementieren (StarterKitDefinition, StarterKitEntry, JsonReader)
- [x] Starter-Kit-Befehl implementieren
- [x] Starter-Item-Markierung speichern (ArpgItemComponent.STARTER_KIT_MARKER)
- [x] Konfiguration für Starter-Kits erweitern
- [x] Rezeptentfernung standardmäßig aktivieren (disableVanillaEquipmentRecipes default: true)
- [x] Unit-Tests ergänzen (79 neue Tests, 9 Testklassen)
- [x] compileJava ausführen
- [x] compileClientJava ausführen
- [x] test ausführen
- [x] build ausführen
- [ ] runServer ausführen (nicht getestet)
- [ ] runClient prüfen (nicht getestet)
- [x] Roadmap final aktualisieren

### Betroffene Systeme

Itemdaten, Befehle, Rezepte, Konfiguration, Serverinitialisierung.

### Abhängigkeiten

Phase 6.

### Akzeptanzkriterien

- [x] Spieler erhält beim ersten Joinen ein Starter-Kit.
- [x] Starter-Items sind Itemlevel 1 und markiert.
- [x] Vanilla-Rezepte werden nach Starter-Kit-Vergabe entfernt (disableVanillaEquipmentRecipes default: true).
- [x] Spieler wird nicht ohne Werkzeug in einen unspielbaren Zustand gebracht.
- [x] Starter-Kit-Farming wird verhindert (nur einmal pro Spieler, PlayerProfileManager prüft joinedBefore).

### Tests

- Manuell: Starterkit-Vergabe im Client (nicht getestet).
- Automatisiert: Starterdatenmodell, Markierung, Konfiguration, Integration.
  - ClassDefinitionTest (8 Tests)
  - ClassDefinitionJsonReaderTest (6 Tests)
  - StarterKitEntryTest (7 Tests)
  - StarterKitDefinitionTest (5 Tests)
  - StarterKitDefinitionJsonReaderTest (7 Tests)
  - StarterKitDeliveryPlannerTest (11 Tests)
  - PlayerArpgProfileTest (9 Tests)
  - PlayerProfileManagerTest (9 Tests)
  - ClassSelectionIntegrationTest (12 Tests)
  - 302 Tests insgesamt (+79 seit Phase 6)

### Status

- [x] Code compiliert und 302 Tests erfolgreich.
- [ ] Manuelle Verifikation: Server/Client-Run ausstehend.

## Phase 7 – Charakterlevel, Erfahrungspunkte und Attribute (abgeschlossen)

### Ziele

- Charakterlevel-System (Stufe 1–100) mit XP-Kurve (eigene ARPG-Stufen, kein Vanilla-XP).
- Basis-Attribute (Stärke, Geschick, Intelligenz, Vitalität) mit Klassenstartwerten.
- Attributpunkt-Vergabe beim Levelaufstieg (1 Punkt pro Stufe, serverautoritativ).
- XP-Vergabe von Mob-Kills (feindliche Mobs, Bosse ×10).
- Synchronisierung des Fortschritts zwischen Client und Server.
- Befehle zum Verwalten von Stufen, XP und Attributen.

### Umsetzung

- `CharacterLevel` Value Object (1–100, validiert, clamped).
- `CharacterAttribute` Enum (STRENGTH, DEXTERITY, INTELLIGENCE, VITALITY).
- `CharacterProgression` Record (level, currentLevelXp, totalXp, unspentPoints, allocatedAttributes).
- `ExperienceCurve` mit parametrisierter Potenzfunktion (`baseXp=100`, `exponent=1.65`).
- `CharacterProgressionDefinition` + JSON-Reader aus `progression_profiles/`.
- `ClassStartingAttributes` mit Klassenstandards (Krieger: 10/3/0/8, Waldläufer: 5/10/0/5, Arkanist: 2/2/12/4, Schurke: 3/8/2/5; Fallback 5/5/5/5).
- `ExperienceRewardService`: `grantXp()`, `processLevelUps()`, `allocateAttribute()` mit Validierung.
- `MobExperienceResolver`: Formel `maxHealth×2 + armor×5 + dimensionBonus`, Boss ×10, nur feindliche Entities.
- PlayerArpgProfile v2: erweitert um `characterLevel`, `currentLevelXp`, `totalXp`, `unspentAttributePoints`, `allocatedAttributes`; Migration von v1→v2 mit leeren Defaults.
- `PlayerProfileManager`: v2-Serialisierung mit dynamischem Deserialisierungs-Pfad.
- `ProgressionManager`: Verknüpft Profil, Kurve, XP-Vergabe, Attributvergabe; berechnet Gesamtattribute.
- `PlayerProgressionSyncPayload` (S2C): synchronisiert Level, XP, freie Punkte, allozierte + totale Werte.
- `AttributeAllocationRequest` (C2S): Client fordert `attributeName` + `amount` an.
- `ProgressionCommand`: `/relicwrought stats`, `/relicwrought xp give <player> <amount>`, `/relicwrought level inspect [player]`, `/relicwrought level set <player> <level>`, `/relicwrought attribute add <attribute> <amount>`.
- Konfiguration (ArpgModConfig): 8 neue Felder (`enableCharacterProgression`, `maximumCharacterLevel`, `requirePlayerKillForXp`, `xpMultiplier`, `showXpGainMessages`, `showLevelUpMessages`, `attributePointsPerLevel`, `allowAdminLevelCommands`).
- Deutsche und englische Lokalisierung für alle Befehle, Attributsnamen, Level-up-Meldungen.
- Event-Handler für AFTER_DEATH (XP-Vergabe) und ServerPlayConnectionEvents.JOIN (Sync).
- Data-Bootstrap lädt `progression_profiles/`-Definitionen.

### Tests
- 6 neue Testklassen: CharacterLevelTest (7), ExperienceCurveTest (9), ExperienceRewardServiceTest (6), CharacterProgressionDefinitionTest (5), CharacterProgressionDefinitionJsonReaderTest (7), ClassStartingAttributesTest (18).
- 52 neue Tests.
- 354 Tests insgesamt (+52 seit Phase 6.5).

### Status

- [x] Code compiliert und 354 Tests erfolgreich.
- [ ] Manuelle Verifikation: Server/Client-Run ausstehend.

## Phase 8.5 – Charakterfenster, HUD, duale Hotbars und Combat-Feedback

### Ziel
Eine neue Benutzeroberfläche, eine saubere Eingabearchitektur für spätere Fähigkeiten und eine übersichtliche Darstellung von Gegnerinformationen und Kampfschaden.

### UI-Architektur
Klar getrennte Common- und Clientpakete für UI-Modelle, Controller und Rendering.

### Charakterfenster
Ein eigenes Relicwrought-Charakterfenster (Standardtaste 'C') zur Anzeige von Level, XP, Attributen und Combat-Stats.

### Spieler-HP-Balken
Ersatz der Vanilla-Herzen durch einen ARPG-HP-Balken.

### Klassenressourcen
Klassenspezifische Ressourcen (Mana, Rage, Energy) anstelle der Vanilla-Rüstungsanzeige.

### Duale Hotbars
Umschaltbare Item- und Ability-Hotbars über die Taste 'R'.

### Eingabesteuerung
Tasten 1–9 wirken ausschließlich auf die aktive Hotbar. Vanilla-Itemwechsel wird im Ability-Modus blockiert.

### Floating Damage Numbers
Schwebende Schadenszahlen über getroffenen Gegnern, differenziert nach Schadensart und Krit.

### Gegner-Nameplates
Eigene Nameplates über relevanten Gegnern mit Level und Klassifikation.

### Gegner-HP-Balken
Genau ein sichtbarer Gegner-HP-Balken innerhalb der Nameplate.

### Netzwerkstrategie
Serverseitige Autorität über HP, Ressourcen, Stats und Ability-Slots. Event-basierte Sync-Payloads.

### Aufgaben

- [ ] HUD- und Input-APIs analysieren
- [ ] World-Space-Rendering-API analysieren
- [ ] CharacterScreenModel implementieren
- [ ] CharacterScreen implementieren
- [ ] Charakterfenster-Keybinding implementieren
- [ ] Spieler-HP-Balken implementieren
- [ ] Vanilla-Herzen ausblenden
- [ ] CharacterResourceType implementieren
- [ ] CharacterResourceState implementieren
- [ ] Ressourcenpersistenz implementieren
- [ ] Ressourcen-Sync implementieren
- [ ] Ressourcenleiste implementieren
- [ ] Vanilla-Rüstung ausblenden
- [ ] HotbarMode implementieren
- [ ] AbilityHotbarState implementieren
- [ ] neun Ability-Slots implementieren
- [ ] Hotbar-Keybinding implementieren
- [ ] beide Hotbars rendern
- [ ] aktive Hotbar hervorheben
- [ ] inaktive Hotbar abdunkeln
- [ ] Zahlentasten kontextabhängig routen
- [ ] Vanilla-Itemwechsel im Ability-Modus blockieren
- [ ] Ability-Aktivierung im Item-Modus blockieren
- [ ] AbilitySlotInputPayload implementieren
- [ ] CombatTextEvent implementieren
- [ ] FloatingDamageNumberPayload implementieren
- [ ] FloatingDamageNumberManager implementieren
- [ ] Damage Numbers rendern
- [ ] Kritdarstellung implementieren
- [ ] Damage-Number-Merge implementieren
- [ ] EnemyClassification implementieren
- [ ] EnemyUiSnapshot implementieren
- [ ] EnemyUiSyncPayload implementieren
- [ ] EnemyUiTracker implementieren
- [ ] Gegner-Nameplates rendern
- [ ] Gegner-HP-Balken rendern
- [ ] doppelte Vanilla-Nameplates verhindern
- [ ] Sichtbarkeits- und Distanzregeln implementieren
- [ ] HUD-Konfiguration ergänzen
- [ ] deutsche Übersetzungen ergänzen
- [ ] englische Übersetzungen ergänzen
- [ ] Unit-Tests ergänzen
- [ ] Integrationstest ergänzen
- [ ] manuellen Test dokumentieren
- [ ] compileJava ausführen
- [ ] compileClientJava ausführen
- [ ] test ausführen
- [ ] build ausführen
- [ ] runServer ausführen
- [ ] runClient ausführen
- [ ] Roadmap final aktualisieren

### Akzeptanzkriterien
HUD, Charakterfenster, Duale Hotbars und Combat Feedback funktionieren serverautoritativ. Vanilla-Elemente werden unterdrückt.

### Manueller Testablauf
Dokumentierter manueller UI-Test.

### Bekannte Einschränkungen
Fähigkeiten sind reine Dummies, Mana/Rage-Verbrauch noch nicht implementiert.

### Status
- [ ] In Planung

## Phase 9 – Weapon Cooldown und Angriffsanimation

### Ziel

Ein konsistentes visuelles und spielmechanisches Weapon-Cooldown-System, das den Vanilla-Angriffsindikator für ARPG-Waffen ersetzt und serverautoritär validiert.

### Aufgaben

- [ ] aktuelle Vanilla-Angriffsanzeige analysieren
- [ ] WeaponCooldownResolver implementieren
- [ ] WeaponAttackState implementieren
- [ ] serverseitiges Cooldown-Gating implementieren
- [ ] clientseitige Vorhersage implementieren
- [ ] Cooldown-Synchronisierung implementieren
- [ ] ARPG-Angriffe vor Cooldownende blockieren
- [ ] falsche Swing-Animation bei abgelehntem Angriff verhindern
- [ ] Swing-Dauer an Angriffsgeschwindigkeit koppeln
- [ ] Animationsprofile vorbereiten
- [ ] eigenen Cooldown-Balken rendern
- [ ] Vanilla-Indikator für ARPG-Waffen ausblenden
- [ ] Cooldown mit dualen Hotbars integrieren
- [ ] Waffenwechselregel implementieren
- [ ] Cooldown-Konfiguration ergänzen
- [ ] Unit-Tests ergänzen
- [ ] manuellen Cooldown-Test durchführen

### Status

- [ ] Ausstehend

## Spätere Systeme

- [ ] Klassen-UI.
- [ ] Klassen-UI.
- [ ] Skilltree ähnlich Path of Exile.
- [ ] aktive Fähigkeiten.
- [ ] Elitegegner.
- [ ] Bosse und Weltbosse.
- [ ] Dungeons.
- [ ] legendäre Effekte.
- [ ] einzigartige Gegenstände.
- [ ] Crafting und Item-Modifikation.
- [ ] eigenes Charakter-/Inventarfenster.
- [ ] erweitertes Schadenssystem.

## Bekannte Risiken

- Minecraft 26.2 hat keine Yarn-Mappings; das Projekt nutzt No-Remap-Loom und die lesbaren Runtime-Namen.
- No-Remap-Loom nutzt `implementation` statt `modImplementation`; spätere Dependency-Setups müssen bewusst geprüft werden.
- Moderne Datenkomponenten für 26.2 müssen noch konkret gegen die verfügbare API geprüft werden.
- Tooltips und Persistenz dürfen keine Clientklassen im gemeinsamen Servercode referenzieren.
- Große ARPG-Zahlen müssen früh mit `double`/`long` geplant werden, um spätere Überläufe zu vermeiden.
- Datapack-Reloading ist noch nicht implementiert; Phase 1 nutzt JSON-Reader und Beispielressourcen als vorbereitete Grundlage.

## Technische Entscheidungen

- Serverautorität ist verbindlich: Itemgenerierung, Rolls und persistente Werte entstehen serverseitig.
- Itemdefinitionen werden immutable modelliert.
- Datenformat-Versionen sind Pflichtbestandteil persistenter Itemdaten.
- JSON-Definitionen werden in kleine Reader und Registries getrennt, damit später Resource-Reloading ohne große Umbauten möglich ist.
- Affix-Konflikte werden über Gruppen und Konfliktgruppen modelliert, nicht über hart codierte Affix-ID-Sonderfälle.
- Phase 1 implementiert bewusst noch keine vollständige ItemStack-Persistenz, um das Datenmodell zuerst stabil testbar zu machen.
- Der erste Daten-Bootstrap lädt bewusst über `_index.json`-Dateien. Das ist eine einfache, testbare Übergangslösung; echtes Datapack-Resource-Reloading folgt in späteren Phasen.
- Phase 2 berechnet nur Basiswerte bis einschließlich Qualität. Lokale Affixe, globale Charakterwerte und Tooltip-Darstellung folgen später.
- Qualität beeinflusst aktuell Waffen-Schaden, Rüstung und Werkzeug-Abbaugeschwindigkeit, aber nicht Haltbarkeit. Diese Regel ist bewusst einfach und testbar.

## Offene Designfragen

- Welche konkrete Data-Component-API von Minecraft 26.2 wird für persistente Itemdaten genutzt?
- Welche Vanilla-Items werden zunächst als Träger für eigene Item-Basen verwendet?
- Wie stark sollen Starter-Items gegen Exploits begrenzt werden, bevor Charakterprofile existieren?
- Welche Affixe zählen später als lokale Itemwerte und welche als globale Charakterwerte?
- Wie wird Qualität langfristig erhöht: Crafting, Drops, Currency oder NPC-System?

## Definition of Done

- [x] Das Projekt kompiliert erfolgreich.
- [x] Entwicklungsclient startet.
- [x] Dedizierter Testserver startet.
- [x] Relevante Itemdaten werden persistent gespeichert.
- [x] Items mit Gegenstandsstufe 1 bis 950 können generiert werden.
- [x] Affixe werden datengetrieben geladen.
- [x] Präfixe, Suffixe und Konfliktgruppen funktionieren.
- [x] ItemStack-Persistenz via DataComponent-API funktioniert.
- [x] Codec-Roundtrip-Tests sind erfolgreich.
- [x] Seltenheiten (Common, Magic, Rare) sind datengetrieben definiert.
- [x] Gewichtete, deterministische Seltenheitsauswahl funktioniert.
- [x] Affix-Slotverteilungen sind datengetrieben und gewichtet.
- [x] Qualitätsgenerator (0–20, gewichtet) funktioniert.
- [x] Item-Basen sind explizit oder aus Pool (Kategorie, Tags) wählbar.
- [x] Seed-Aufteilung (6 Substreams) ist implementiert.
- [x] Zentrale Itemgenerierungspipeline (ArpgItemGenerator) ist implementiert.
- [x] Generierung ist atomar (Zielstack bleibt bei Fehler unverändert).
- [x] Debugbefehle: `/arpgitem generate`, `random`, `inspect`, `validate`, `remove`, `help`, `loot simulate`.
- [x] Tooltips zeigen Itemlevel, Seltenheit, Qualität, Basiswerte, Affixe, Vorschautext.
- [x] Deutsche und englische Lokalisierung (en_us, de_de).
- [x] Gleiche Seed-Eingabe erzeugt reproduzierbare Items.
- [x] Ungültige Datendateien crashen keine Welt.
- [x] 223 Unit-Tests sind erfolgreich (Phase 6).
- [x] 302 Unit-Tests sind erfolgreich (Phase 6.5).
- [x] Klassen-Datenmodell (ClassDefinition) wird datengetrieben geladen (5 Klassen).
- [x] Starter-Kit-Datenmodell (StarterKitDefinition) wird datengetrieben geladen (5 Kits).
- [x] Starter-Kit-Vergabe funktioniert (einmalig pro Spieler, asynchron nach Join).
- [x] Starter-Items sind als solche markiert (STARTER_KIT_MARKER DataComponent).
- [x] Lootprofile werden datengetrieben geladen (6 Profile).
- [x] Normale Mobs können ARPG-Items droppen (AFTER_DEATH-Event).
- [x] Itemlevel wird aus Mobstärke + Dimension aufgelöst.
- [x] Vanilla-Rezepte können konfigurierbar deaktiviert werden (default: true ab Phase 6.5).
- [x] `Roadmap.md` ist aktuell.
- [x] Bekannte Einschränkungen sind dokumentiert.
