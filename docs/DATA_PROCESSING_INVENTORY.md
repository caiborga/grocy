# Datenverarbeitungsinventar (technisch)

Dieses Dokument beschreibt die in Grocy erkennbaren Verarbeitungen. Es ist
keine Rechtsberatung und behauptet keine DSGVO-Konformität.

Platzhalter mit `TODO` müssen vom Betreiber ergänzt werden.

## Datenarten

| Datenart | Beispiele | Personenbezug |
|---|---|---|
| Account | E-Mail, Anzeigename, Passwort-Hash, Verifizierungsstatus, letzte Anmeldung | ja |
| Sitzungen | Hash der JWT-ID (`jti`), Ablauf, Widerruf | indirekt |
| Auth-Tokens | Hash von E-Mail-Bestätigung und Passwort-Reset | indirekt |
| Haushalt | Name, Mitgliedschaften, Rollen (OWNER/EDITOR/VIEWER) | mittelbar |
| Fachdaten | Listen, Einträge, Rezepte, Zutaten, Schritte, Aktivitätsereignisse | können Namen enthalten |
| Einladungen | Token-Hash, Rolle, Ablauf, Nutzungszähler, Status | indirekt |
| Accountlöschung | Zeitstempel ohne Klarname/E-Mail | nein (aggregierbar) |
| Logs | technische Ereignisse, Fehlerklassen | möglichst ohne Klartext-PII |

## Zwecke

- Bereitstellung der Haushalts- und Listenfunktion
- Authentifizierung und Autorisierung
- E-Mail-Bestätigung und Passwort-Reset
- Einladungen in Haushalte
- Betriebssicherheit (Rate-Limiting, Healthcheck)
- optionale aggregierte Adminstatistik
- Accountlöschung und Datenexport auf Anforderung der betroffenen Person

## Speicherorte

- Anwendungsdatenbank (PostgreSQL in Produktion, H2 in Tests)
- JWT nur clientseitig (Browser-`localStorage`), serverseitig nur Session-Metadaten/Hash
- Anwendungslogs des Prozesses / Container-Stdout
- TODO: konkreter Datenbankhost und Speicherort der Backups

## Cookies

Es werden keine Authentifizierungs-Cookies gesetzt. Die Sitzung nutzt ein
Bearer-JWT im lokalen Speicher. Es gibt keinen Cookie-Banner, weil keine
nicht notwendigen Cookies verwendet werden.

## E-Mail-Versand

Versendet werden Bestätigungs- und Passwort-Reset-Mails über den konfigurierten
SMTP-Server (`SMTP_HOST`). Einladungen erfolgen als Link in der Anwendung, nicht
zwingend per E-Mail.

- TODO: Name und Sitz des E-Mail-Dienstleisters
- TODO: AV-Vertrag mit dem E-Mail-Dienst

## Hoster

- TODO: Name, Sitz und Standort des Hosters / der VM
- TODO: AV-Vertrag mit dem Hoster

Die Anwendung bindet in der Default-Konfiguration `0.0.0.0:8080`. Ob davor ein
Reverse-Proxy liegt, ist betrieblich zu dokumentieren. Cloudflare wird nicht
vorausgesetzt.

## Weitere Dienstleister

- TODO: Monitoring, Backup-Ziel, DNS, optional später Cloudflare
- GitHub Container Registry und GitHub Actions werden für Build/Deploy genutzt,
  soweit der Betreiber das Workflow-File verwendet.

## Backups

- TODO: Rhythmus, Speicherort, Verschlüsselung, Aufbewahrung, getestete Wiederherstellung

## Vorgeschlagene Löschfristen

Diese Fristen sind Vorschläge, keine fest verdrahteten Jobs (außer abgelaufene
Sitzungen und abgehakte alte Einträge).

| Daten | Vorschlag |
|---|---|
| JWT-Sitzungen | bis Ablauf oder Logout/Passwort-Reset, danach Cleanup |
| Bestätigungs-/Reset-Tokens | nach Verbrauch oder Ablauf (24 h / 30 min) |
| Anwendungslogs | 7–30 Tage, Rotation auf dem Server |
| Nginx-/Access-Logs | 7–14 Tage, falls ein Proxy genutzt wird |
| Account nach Löschung | Klartext-PII sofort anonymisieren; Löschereignis nur als Zähler |
| Fachdaten geteilter Haushalte | bleiben beim Haushalt, bis dieser selbst entfällt |
| Backups | TODO, an Accountlöschung und Logs anpassen |

## Export und Löschung

- Export: `GET /api/me/export`
- Löschung: `DELETE /api/me` (ggf. vorher Eigentumsübertragung)
