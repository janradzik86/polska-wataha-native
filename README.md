# 🐺 Polska Wataha! — sieć lokalnej pomocy i przetrwania

Aplikacja społeczności **CzarneWilkiPrawdy**: ogłoszenia lokalne (barter), asystent **WILK**
offline, poradnik przetrwania, tryb kryzysowy 🚨, sieć mesh A–D (Bluetooth/Multipeer/LoRa-stub),
mapa dobroci, odznaki. **Motyw biało-czerwony, Kotwica.**

## Struktura
| Katalog | Co zawiera |
|---|---|
| `app/` | **Android** (Kotlin + Compose) — gotowy APK V0.2 (`deliverables/Polska-Wataha-v0.2.apk`) |
| `ios/` | **iOS** (SwiftUI, wernisaż V0.2) — projekt + `README.md` + `ota/` (instalacja bez sklepu) |
| `backend/` | Node.js: API (`/api/feedback`, auth, ogłoszenia) + panel WWW |
| `docs/` | Dokumentacja funkcji i roadmapa V0.1–V1.0 |
| `.github/workflows/` | Budowa podpisanego **IPA Ad Hoc w chmurze** (bez Maca) |
| `codemagic.yaml` | Ten sam build dla Codemagic.io |

## Szybki start
* **Android:** zainstaluj APK — konto demo `cwilk` / `haslo123`
* **iOS bez App Store:** `ios/DYSTRYBUCJA-BEZ-APP-STORE.md` (Ad Hoc + link/QR dla ludzi)
* **Backend lokalnie:** `cd backend && node server.js` → `http://localhost:8080`

## Budowa iOS w chmurze (GitHub Actions)
1. Repo → *Settings → Secrets → Actions*: `APPLE_TEAM_ID`, `APPLE_CERT_P12`,
   `APPLE_CERT_PASSWORD`, `APPLE_PROVISIONING_PROFILE` (base64).
2. *Actions → „Build iOS IPA (Ad Hoc…)” → Run* → w Artifacts podpisany `Polska-Wataha-iOS-v0.2.ipa`.
3. Testy rdzenia (asystent + mesh) biegają też na Linuxie: `cd ios/PolskaWataha/core && swift test` (6/6 ✅).

Dystrybucja: poza sklepem, do 100 iPhone'ów/rok (UDID), wymaga konta Apple Developer 99 USD/rok — szczegóły w `ios/DYSTRYBUCJA-BEZ-APP-STORE.md`.
