# Technische und organisatorische Maßnahmen

Bestandsaufnahme der im Repository erkennbaren Maßnahmen. Keine Aussage über
eine geprüfte, vollständige oder rechtssichere Absicherung.

## Vorhandene Maßnahmen

### Authentifizierung

- Passwörter mit BCrypt
- E-Mail-Bestätigung mit gehashtem, kurzlebigem Einmal-Token (24 h)
- Passwort-Reset mit gehashtem, kurzlebigem Einmal-Token (30 min)
- JWT mit serverseitiger Sitzung (`jti`-Hash), Ablauf und Widerruf; kein Default-Secret in `application.yml`
- Logout widerruft die aktuelle Sitzung
- Passwort-Reset widerruft alle Sitzungen
- Rate-Limiting (prozesslokal) für Login, Registrierung, Reset, Verifikation, Invite-Annahme
- Client-IP für Limits: `RemoteAddr`, bei Trusted-Proxy (Standard Loopback) `X-Real-IP` / `X-Forwarded-For`
- generische Antworten bei Registrierung (bereits vergebene E-Mail) und Passwort-Reset
- Login prüft das Passwort, bevor ein unverifizierter Account erkennbar wird

### Autorisierung

- Jeder API-Aufruf unter `/api/**` (außer Auth/Public) erfordert eine gültige Sitzung
- Haushaltsrechte sind in `Permissions` für OWNER / EDITOR / VIEWER zentralisiert
- Services prüfen Mitgliedschaft und Rolle; fremde IDs führen nicht zu Daten
- Einladungen: kryptografisch zufällig, SHA-256-Hash, Ablauf, max. 1 Nutzung, widerrufbar
- Globale Nutzer-CRUD-API ist entfernt
- Adminstatistik nur für in `APP_ADMIN_EMAILS` hinterlegte Konten, nur Aggregate

### Datenschutzfunktionen

- Impressum und Datenschutz öffentlich erreichbar (Inhalte mit `TODO`)
- keine Pflicht-Checkbox, kein Cookie-Banner
- Datenexport und Accountlöschung
- Accountlöschung anonymisiert personenbezogene Accountfelder
- alleiniger OWNER eines Haushalts mit weiteren Mitgliedern muss zuerst übertragen

### Betrieb und Fehler

- Healthcheck `/health` ohne Version, Secrets oder Stacktraces
- Produktionsprofil: kein SQL-Debug, keine OpenAPI-UI, Registrierung standardmäßig aus
- generische 500er ohne Exception-Namen
- Security-Header (u. a. Frame-Deny, Referrer-Policy, CSP, nosniff)
- Cache-Control `no-store` für `/api/**` und `/health`
- CORS nur für `APP_FRONTEND_URL`
- CSRF deaktiviert, weil Auth über `Authorization: Bearer` und nicht über Cookies läuft
- Request-/Upload-Limits 1 MB
- Mail- und Tokeninhalte werden nicht ins Log geschrieben

## Offene Risiken

- JWT liegt im `localStorage` (XSS kann die Sitzung stehlen). Es gibt kein HttpOnly-Cookie.
- Rate-Limiting ist in-memory und gilt pro Prozess, nicht clusterweit.
- Forwarded-IPs werden nur von Loopback-Proxys übernommen. Nginx muss `X-Real-IP` / `X-Forwarded-For` auf `$remote_addr` setzen (nicht ungeprüft anhängen). Der Origin darf nicht zusätzlich öffentlich erreichbar sein, oder `APP_RATE_LIMIT_TRUSTED_PROXIES` muss zur tatsächlichen Proxy-IP passen.
- Hibernate `ddl-auto: update` auch in Produktion: Schemaänderungen ohne Migrations-Review.
- Kein Unique-Constraint auf `(household_id, user_id)`.
- Kein Account-Lock nach Fehlversuchen (`lockedUntil` ungenutzt).
- Keine CSP ohne `'unsafe-inline'` für Styles (Element Plus / vorhandenes CSS).
- Kein eingechecktes Produktions-Compose/Nginx; lokale Compose-Datei bleibt gitignoriert, Vorlage ohne Passwort in `docker-compose.example.yml`.
- Reverse-Proxy-IPs: ohne Nginx-Header bleibt hinter einem Proxy nur Loopback sichtbar; mit Loopback als Trusted-Proxy und gesetztem `X-Real-IP` gilt die Client-IP.
- Keine automatisierte, reproduzierbare Backup-/Restore-Strecke im Repository.
- Impressum/Datenschutz enthalten `TODO` und sind unvollständig.
- Keine Behauptung, die Anwendung sei auditiert, pentested oder vollständig abgesichert.

## Logging

- Keine Passwörter, Roh-Tokens, Einladungslinks oder E-Mail-Bodies in Anwendungslogs
- Vorgeschlagene Aufbewahrung: 7–30 Tage
- Rotation: über Docker/journald/logrotate auf dem Server, nicht in der Anwendung
