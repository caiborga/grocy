# Nielsen Usability Heuristics

> Diese Datei dient als Design- und Review-Standard für die Entwicklung von Benutzeroberflächen.
> Jede neue Funktion sollte gegen diese Heuristiken geprüft werden.

---

# 1. Sichtbarkeit des Systemstatus

## Ziel

Der Benutzer sollte jederzeit verstehen, was die Anwendung gerade macht.

## Umsetzung

- Ladezustände anzeigen
- Fortschrittsbalken verwenden
- Erfolgsmeldungen anzeigen
- Fehlermeldungen anzeigen
- Buttons während Requests deaktivieren
- Autosave anzeigen
- Letzte Synchronisation anzeigen

## Gute Beispiele

- "Speichern..."
- "Gespeichert"
- Spinner während API-Requests
- Upload-Fortschritt

## Schlechte Beispiele

- Button reagiert scheinbar nicht
- Seite friert ohne Rückmeldung ein

---

# 2. Übereinstimmung zwischen System und Realität

## Ziel

Die Anwendung soll die Sprache des Benutzers sprechen.

## Umsetzung

- Fachbegriffe nur verwenden, wenn Benutzer sie kennt
- Natürliche Sprache verwenden
- Bekannte Symbole nutzen
- Reale Abläufe widerspiegeln
- Daten in gewohnten Formaten anzeigen

## Gute Beispiele

- Rechnung
- Kunde
- Bezahlt
- Papierkorb

## Schlechte Beispiele

- Entity
- Persist
- Commit
- UUID

---

# 3. Benutzerkontrolle und Freiheit

## Ziel

Benutzer dürfen Fehler machen und müssen diese leicht rückgängig machen können.

## Umsetzung

- Undo anbieten
- Abbrechen ermöglichen
- Bestätigungsdialoge bei kritischen Aktionen
- Keine Sackgassen erzeugen
- Browser-Zurück unterstützen

## Gute Beispiele

- Rechnung löschen → Bestätigung
- Snackbar mit "Rückgängig"

## Schlechte Beispiele

- Sofortiges endgültiges Löschen

---

# 4. Konsistenz und Standards

## Ziel

Gleiche Dinge müssen sich gleich verhalten.

## Umsetzung

- Einheitliche Farben
- Einheitliche Buttons
- Einheitliche Icons
- Einheitliche Begriffe
- Einheitliche Navigation
- Design-System verwenden

## Gute Beispiele

Primärbutton immer rechts

## Schlechte Beispiele

Speichern:

- mal links
- mal rechts
- mal grün
- mal blau

---

# 5. Fehlervermeidung

## Ziel

Fehler sollen möglichst gar nicht erst entstehen.

## Umsetzung

- Formulare validieren
- Sinnvolle Standardwerte
- Dropdown statt Freitext
- Autovervollständigung
- Eingaben einschränken
- Kritische Aktionen absichern

## Gute Beispiele

Datumsauswahl

## Schlechte Beispiele

Freies Texteingabefeld für Datumswerte

---

# 6. Wiedererkennen statt Erinnern

## Ziel

Der Benutzer soll möglichst wenig auswendig wissen müssen.

## Umsetzung

- Sichtbare Navigation
- Beschriftete Icons
- Vorschläge anzeigen
- Kürzlich verwendete Daten
- Suchfunktion
- Breadcrumbs

## Gute Beispiele

Dropdown mit Kunden

## Schlechte Beispiele

Kundennummer auswendig eingeben

---

# 7. Flexibilität und Effizienz

## Ziel

Anfänger und Experten sollen gleichermaßen effizient arbeiten können.

## Umsetzung

- Tastenkürzel
- Drag & Drop
- Mehrfachauswahl
- Bulk-Aktionen
- Schnellsuche
- Favoriten

## Gute Beispiele

CTRL + S

CTRL + K

Tab-Navigation

## Schlechte Beispiele

Jede Aktion nur per Maus

---

# 8. Ästhetisches und minimalistisches Design

## Ziel

Nur notwendige Informationen anzeigen.

## Umsetzung

- Wenig visuelles Rauschen
- Gute Weißräume
- Wichtige Informationen hervorheben
- Keine unnötigen Dialoge
- Progressive Disclosure nutzen

## Gute Beispiele

Leere Tabellen mit CTA

## Schlechte Beispiele

20 Buttons gleichzeitig sichtbar

---

# 9. Benutzer beim Erkennen und Beheben von Fehlern unterstützen

## Ziel

Fehlermeldungen müssen verständlich sein.

## Umsetzung

- Problem beschreiben
- Ursache erklären
- Lösung anbieten
- Fehlerstelle markieren

## Gute Beispiele

"E-Mail-Adresse ist ungültig."

## Schlechte Beispiele

"Error 500"

"Validation failed"

---

# 10. Hilfe und Dokumentation

## Ziel

Auch wenn gute Software möglichst selbsterklärend ist, sollte Hilfe leicht erreichbar sein.

## Umsetzung

- Tooltips
- FAQ
- Dokumentation
- Onboarding
- Hilfebereich
- Beispiele

## Gute Beispiele

Tooltip bei unbekannten Feldern

## Schlechte Beispiele

Benutzer muss externe Dokumentation durchsuchen

---

# UI-Review-Checkliste

Vor jedem Merge sollten folgende Fragen beantwortet werden:

## Systemstatus

- [ ] Werden Ladezustände angezeigt?
- [ ] Gibt es Erfolgsmeldungen?
- [ ] Werden Fehler angezeigt?

## Verständlichkeit

- [ ] Nutzt die UI Benutzersprache?
- [ ] Sind Begriffe eindeutig?

## Benutzerkontrolle

- [ ] Kann der Benutzer Aktionen abbrechen?
- [ ] Gibt es Undo oder Bestätigungen?

## Konsistenz

- [ ] Verhalten konsistent?
- [ ] Design-System eingehalten?

## Fehlervermeidung

- [ ] Validierungen vorhanden?
- [ ] Gute Standardwerte?

## Wiedererkennen

- [ ] Muss sich der Benutzer etwas merken?
- [ ] Sind Optionen sichtbar?

## Effizienz

- [ ] Tastenkürzel sinnvoll?
- [ ] Wiederkehrende Aufgaben optimiert?

## Minimalismus

- [ ] Nur notwendige Informationen?
- [ ] Kein visuelles Chaos?

## Fehlermeldungen

- [ ] Verständlich formuliert?
- [ ] Lösung vorgeschlagen?

## Hilfe

- [ ] Tooltips vorhanden?
- [ ] Dokumentation erreichbar?

---

# Grundprinzip

> Eine gute Benutzeroberfläche fällt nicht auf.
>
> Der Benutzer konzentriert sich auf seine Aufgabe – nicht auf die Bedienung der Software.
