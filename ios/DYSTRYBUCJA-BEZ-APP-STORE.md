# 🚀 Dystrybucja Polska Wataha! BEZ App Store (Ad Hoc + OTA)
### Instalacja iPhone'ów bezpośrednio z linku — bez sklepu, bez przeglądu Apple

> **Tak, to możliwe.** Apple dopuszcza instalację aplikacji poza sklepem metodą **Ad Hoc**:
> podpisany plik `.ipa` wgrywasz na zwykłą stronę WWW, a ludzie instalują go jednym dotknięciem
> z Safari (lub kodem QR). **Zero sklepu, zero przeglądu, zero TestFlight.**
>
> **Co musi być spełnione (tego nie da się obejść — sprawdzone i potwierdzone):**
> 1. **Konto Apple Developer — 99 USD/rok.** Dystrybucja na cudze iPhone'y wymaga podpisu.
>    (Darmowe Apple ID = tylko 3 własne urządzenia i 7 dni — to nie jest dystrybucja.)
> 2. **Zbudowanie podpisanego IPA** — wymaga macOS; bez własnego Maca używasz chmury
>    (Codemagic / GitHub Actions — konfiguracje już gotowe).
> 3. **UDID każdego iPhone'a** — trzeba go raz dopisać do profilu (limit: **100 urządzeń / rok**).
>    Od tego nie ma ucieczki: instaluje się tylko na wpisane urządzenia.
>    (Do tego jest gotowy formularz — patrz Krok 4.)

---

## Schemat w pigułce

```
Ty (Administrator Watahy)
   │  1. Konto Apple Developer (99 USD/rok)          — developer.apple.com
   │  2. Zbierasz UDID telefonów (udid.io)           — do 100 szt./rok
   │  3. Dopisujesz UDID do konta + profil Ad Hoc
   │  4. Budujesz IPA w chmurze (bez Maca!)          — Codemagic / GitHub Actions
   │  5. Wgrywasz 3 pliki na stronę WWW:              — manifest.plist + PolskaWataha.ipa + install.html
   ▼
iPhone'y: dotknięcie linku w Safari → „Zainstaluj” → ikona orła na ekranie głównym 🦅
```

---

## Krok 1 — Konto Apple Developer (~20 min, raz w roku)

1. **developer.apple.com** → *Account* → **Enroll** → typ *Individual* (jednoosobowe) albo
   *Organization* (wataha jako grupa — potrzebne D-U-N-S i dokumenty firmy; dla startu wystarczy Individual).
2. Zapłać **99 USD/rok**.
3. Po akceptacji zanotuj **Team ID** (Account → Membership).

## Krok 2 — Certyfikaty i profil Ad Hoc (w portalu Apple)

1. **developer.apple.com → Certificates, Identifiers & Profiles**:
   * **Certyfikat:** *Apple Distribution* (pobierz `.p12` — przyda się w chmurze).
   * **Identifiers:** *App ID* `pl.wataha.ios`.
   * **Devices:** dodaj UDID każdego telefonu (z Kroku 4).
   * **Profiles:** *Distribution → Ad Hoc* → wybierz App ID, certyfikat i urządzenia → pobierz
     `.mobileprovision`.
2. Trzymaj te 3 pliki pod ręką (p12 + hasło + mobileprovision). **Nie podawaj ich nikomu obcemu.**

## Krok 3 — Budowa IPA w chmurze (bez Maca)

### Wariant A: Codemagic (najłatwiejszy)
1. **codemagic.io** → konto (GitHub) → *Add application* → wskaż repozytorium.
   Najprościej: spakowany gotowiec **`Polska-Wataha-GITHUB-gotowe-v0.2.zip`** — wgrywasz jego
   ZAWARTOŚĆ jako korzeń repo (w środku `codemagic.yaml`).
2. *App Store Connect* (integracja) → zaloguj konto Apple Developer.
3. W ustawieniach podpisu wybierz **Ad Hoc** i wgraj `p12`/`mobileprovision` z Kroku 2.
4. **Start build** → po ~10 minutach sztandarowy plik:
   **`export/PolskaWataha.ipa`** (do pobrania z artykułów builda).

### Wariant B: GitHub Actions (darmowe macOS przy publicznym repo)
1. **github.com** → *New repository* (możesz publiczne) → wgraj ZAWARTOŚĆ gotowca
   **`Polska-Wataha-GITHUB-gotowe-v0.2.zip`** (w środku `.github/workflows/ios-build.yml`
   i `exportOptions-adhoc.plist` — wszystko na właściwych miejscach).
2. Repo → *Settings → Secrets* → dodaj: `APPLE_TEAM_ID`, `APPLE_CERT_P12` (base64),
   `APPLE_CERT_PASSWORD`, `APPLE_PROVISIONING_PROFILE` (base64).
3. Workflow sam zbuduje i wrzuci **podpisany .ipa** do *Artifacts* (zakładka Actions).

### Wariant C: znajomy z Macem (30 minut)
Pożyczony MacBook + Xcode: `xcodegen generate` → `open PolskaWataha.xcodeproj` → Signing (Team) →
*Product → Archive → Distribute App → Development/Ad Hoc → Export*.

> **IPA dla wszystkich jest zawsze ten sam** — raz zbudowany, działa na wszystkich urządzeniach
> wpisanych do profilu przez cały rok (aktualizacja = nowy build, ale starzy użytkownicy
> dostają tylko nowy link).

## Krok 4 — Zbieranie UDID od ludzi (formularz 5 minut)

1. Każdy zainteresowany otwiera **udid.io** na swoim iPhonie (albo Ustawienia → Ogólne →
   Informacje → dotknij 3× pole „Numer seryjny” → pojawia się UDID).
2. Wysyła Ci: **UDID + imię** (formularz Google/chat/e-mail — jak wolisz).
3. Ty wklejasz UDID do portalu Apple (Krok 2) i **odświeżasz profil** (kolejne buildy).

## Krok 5 — Publikacja linku do instalacji (folder `ota/` jest gotowy)

1. Weź **3 pliki** z `ios/ota/`:
   * `manifest.plist`  → wgraj jako `https://TWOJA-STRONA/PolskaWataha/manifest.plist`
   * `PolskaWataha.ipa` → wgraj jako `https://TWOJA-STRONA/PolskaWataha/PolskaWataha.ipa`
   * `install.html`   → wgraj jako `https://TWOJA-STRONA/PolskaWataha/install.html`
2. W `manifest.plist` i `install.html` zamień `TWOJA-STRONA` na swój adres HTTPS.
   (Darmowy hosting z HTTPS: **GitHub Pages** / **Netlify** — 5 minut.)
3. **Wymagania techniczne** (żeby nie było niespodzianek):
   * serwer MUSI mieć **HTTPS** (iOS odrzuca HTTP),
   * manifest MUSI kończyć się na `.plist`,
   * `install.html` wgrywasz na serwerze z poprawnym typem treści (domyślnie działa).
4. Wyślij ludziom link **https://TWOJA-STRONA/PolskaWataha/install.html** (albo kod QR —
   wygeneruje się sam na tej stronie). Otwierają z Safari, dotykają *ZAINSTALUJ* → gotowe.

## Krok 6 — Odświeżanie i rozszerzanie

| Sytuacja | Co robisz |
|---|---|
| Kolejny przyjęty członek watahy | Dodaj jego UDID → przebuduj → wyślij nowy link (stare działa nadal dla starych) |
| Kontrola błędów | Nowy build tego samego „wariantu” — bez sklepu, bez czekania |
| Koniec roku | Odnów członkostwo + odśwież certyfikat/profil, przebuduj, wyślij nowy link |
| >100 urządzeń | Kolejne: Apple Enterprise (299 USD/rok — ale TYLKO dla organizacji/pracowników; Apple to egzekwuje i może odebrać konto) **albo** TestFlight (bez sklepu, do 10 000 osób) |

## Uczciwe "co jeśli…"

* **„Chcę ZERO płacenia Apple”** — nie istnieje dla dystrybucji na czyjeś iPhone'y. To blokada
  techniczna Apple, nie moja decyzja. (Darmowe ID = 3 własne urządzenia, 7 dni, wymaga Maca.)
* **„Chcę bez UDID, bez limitów”** — tak działają tylko: Enterprise ($299, tylko pracownicy
  organizacji) i App Store. Oba odpadały u Ciebie.
* **„Czy mogę po prostu wysłać plik .ipa na WhatsApp?”** — można, ale i tak zainstaluje się
  tylko na urządzeniach wpisanych do profilu; link z Kroku 5 jest wygodniejszy.
* **„Po co w ogóle Apple Developer, skoro to nie sklep?”** — bo każde urządzenie iOS wykonuje
  weryfikację podpisu przy instalacji. Podpis = konto Developer. To jest właśnie ta „bramka”.

---

### Pliki, które już masz (repozytorium `polska-wataha` na GitHubie)

```
.github/workflows/ios-build.yml  ← korzeń repo: Actions (testy→kompilacja→IPA Ad Hoc→Artifacts)
codemagic.yaml                   ← korzeń repo: ten sam build w Codemagic
ios/DYSTRYBUCJA-BEZ-APP-STORE.md ← ten przewodnik
ios/ota/install.html             ← strona instalacji z przyciskiem i QR (podmień TWOJA-STRONA)
ios/ota/manifest.plist           ← manifest OTA (podmień TWOJA-STRONA)
ios/PolskaWataha/                ← aplikacja + project.yml + exportOptions-adhoc.plist + core(testy)
ios/docs/AppStoreText.md         ← (opcjonalne) teksty, gdyby kiedyś jednak sklep

Paczki lokalne (deliverables/, poza repo):
  Polska-Wataha-GITHUB-gotowe-v0.2.zip  ← iOS-only gotowiec na osobne repo (korzeń = zawartość zipa)
  Polska-Wataha-iOS-zrodla-v0.2.zip     ← sam katalog ios/ z dokumentacją
  Polska-Wataha-v0.2.apk                ← Android (działa dziś)
```
