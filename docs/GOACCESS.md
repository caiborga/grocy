# Optionale Nginx-Logauswertung mit GoAccess

GoAccess kann Access-Logs eines Reverse-Proxy aggregieren. Die Anwendung selbst
schreibt keine Nginx-Logs und enthält kein Nginx.

## Voraussetzungen

- Nginx (oder vergleichbarer Proxy) vor der Anwendung, Access-Log aktiv
- Auswertung nur auf dem Server oder in einem geschützten Netz
- Keinen ungeschützten HTML-Report ins Internet legen

## Beispiel (nicht produktiv kopieren ohne Absicherung)

```bash
# Nur lokal, Report nicht öffentlich ausliefern
goaccess /var/log/nginx/access.log \
  --log-format=COMBINED \
  -o /root/goaccess-report.html
```

Den Report nicht unter einem öffentlichen Alias wie `/stats` ablegen.
Wenn eine Webansicht nötig ist, nur mit starker Authentifizierung oder
ausschließlich über SSH/SCP.

## Datenschutz

Access-Logs können IP-Adressen und URL-Pfade enthalten. Query-Strings mit
Tokens (`?token=`) sollten im Proxy möglichst nicht geloggt oder gekürzt
werden. Aufbewahrung kurz halten (siehe `docs/DATA_PROCESSING_INVENTORY.md`).
