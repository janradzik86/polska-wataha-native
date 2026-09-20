# 📲 INSTRUKCJA INSTALACJI APK — POLSKA WATAHA V0.1

## Co instalujesz
**`Polska-Wataha-v0.1.apk`** — natywna aplikacja Android (Kotlin + Compose), podpisana (release), ~7,2 MB.
Wymaganie: **Android 8.0 (API 26) lub nowszy**. Nie wymaga Google Play, konta Google ani żadnych usług.

---

## Krok po kroku

### 1. Przenieś plik na telefon
Wybierz wygodny sposób:
- **USB**: podłącz telefon jako MTP → skopiuj `Polska-Wataha-v0.1.apk` do `Pobrane/`,
- **e-mail / komunikator**: wyślij plik do siebie i otwórz z załącznika,
- **dysk / karta SD**,
- **adb** (deweloperzy): `adb install Polska-Wataha-v0.1.apk`.

### 2. Zainstaluj
1. Otwórz plik (menadżer plików / powiadomienie o pobraniu).
2. Android pokaże:**„Ta aplikacja nie pochodzi ze Sklepu Play"** → **Ustawienia / Zezwól z tego źródła** → wróć.
   *(Zezwolenie dotyczy tylko tej instalacji — to standard przy sideloadzie.)*
3. **Zainstaluj** → gotowe.

### 3. Uruchom
Ikona **„Polska Wataha"** (czerwono-biała kotwica). Ekran powitalny → przycisk:
- **„WEJDŹ JAKO CZARNY WILK"** — natychmiastowe demo (`cwilk / haslo123`), lub
- **„Utwórz konto testowe"** — własne konto (rejestracja działa też offline).

### 4. Pierwsze 5 minut (szybki test)
1. Pulpit → **Ogłoszenia** → ➕ → opublikuj „Oddaję latarkę" (zdjęcie z galerii lub aparatu).
2. **Mapa** → 🎯 GPS → zobacz ogłoszenia, punkty pomocy i węzły sieci.
3. **Wiadomości** → otwórz wątek z „Sokół" → wyślij tekst → **„🤖 Symuluj odpowiedź"** (dostaniesz powiadomienie).
4. Pulpit → **🚨 TRYB KRYZYSOWY** → 📢 KOMUNIKAT → wyślij (alarm + pełny ekran).
5. **Sieć** → MESH LAB → wyłącz NODE B → **„WYŚLIJ A→D"** → trasa przeskoczy na A→C→D.

---

## Test offline (ważne!)
1. **Profil → Ustawienia → „Tryb DEMO OFFLINE"** włącz (albo włącz tryb samolotowy w telefonie).
2. Stwórz ogłoszenie / wyślij wiadomość / zgłoś potrzebę → w UI widzisz **„⏳ oczekuje na wysyłkę"**.
3. Wyłącz demo → **Profil → „SYNCHRONIZUJ TERAZ"** → kolejka się czyści, dane lecą do backendu.

---

## Podłączenie backendu (opcjonalnie)
1. Na komputerze: `cd backend && node server.js` (Node ≥ 18) → `http://localhost:8080`.
2. Telefon i komputer w tej samej sieci Wi-Fi.
3. **Profil → Backend** → wpisz `http://ADRES-IP-KOMPUTERA:8080` (znajdziesz go przez `ipconfig`/`ip a`).
4. **Synchronizuj teraz** → Twoje dane widać w panelu web `http://ADRES-IP:8080/`.

---

## Typowe problemy
| Problem | Rozwiązanie |
|---------|-------------|
| „Nie można zainstalować — aplikacja nie jest zgodna" | Telefon ma Android < 8.0 — użyj innego telefonu |
| Instalacja nie startuje z menadżera plików | Zezwól aplikacji menadżera na „instalowanie nieznanych aplikacji" |
| Brak powiadomień | Android 13+: zezwól na powiadomienia przy pierwszym zapytaniu; sprawdź kanały w ustawieniach systemowych |
| Mapa pokazuje Warszawę | Przyznaj GPS (🎯 na mapie) — pozycja się zaktualizuje |
| BLE nie wykrywa urządzeń | Włącz Bluetooth i przyznaj uprawnienia lokalizacji (Android < 12) |

## Wersjonowanie
`versionName 0.1.0` • `versionCode 1` • kompilacja SDK 34 • minSdk 26 • podpis: CN=Polska Wataha (SHA-256: `dad3f62a…`)
