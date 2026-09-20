# 🧪 TESTY — POLSKA WATAHA V0.2

## A0. Co przetestowano AUTOMATYCZNIE w V0.2
| Test | Wynik |
|------|-------|
| Kompilacja Kotlin + KSP + zasoby (3 fotorealistyczne obrazy) — 0 błędów | ✅ |
| `assembleRelease` — **V0.2 APK ~7,6 MB** (`versionCode 2`, `versionName 0.2.0`) | ✅ |
| Podpis — `apksigner verify` (RSA 2048, CN=Polska Wataha) | ✅ |
| **Testy jednostkowe — 12/12**: `AssistantEngineTest` 5/5 (woda→Woda, SOS→kryzys+112, 72h→checklista, fallback, polskie znaki) + `MeshSimTest` 7/7 (trasa A→B→C→D, awaria B→A→C→D, awaria B+C→brak trasy/store-and-forward, wznowienie, trasy zapasowe) | ✅ |
| **Backend E2E**: health → login → posts → comm → sync-pull → crisis → **feedback (nowość)** → panel web (zakładka 💡 Pomysły) | ✅ 7/7 |
| Obrazy w APK: hero_eagle / wolf_ai / wolf_pack (potwierdzone w `res/`) | ✅ |

## A. Testy V0.1 (rdzeń — bez zmian)


## A. Co przetestowano AUTOMATYCZNIE w tym środowisku (zanim przekazano APK)

| Test | Wynik |
|------|-------|
| Kompilacja Kotlin + KSP (Room) — 0 błędów, tylko uwagi | ✅ |
| `assembleRelease` — zbudowany **release** APK ~7,2 MB | ✅ |
| **Podpis** — `apksigner verify` (RSA 2048, CN=Polska Wataha) | ✅ |
| **Manifest/package** — `aapt dump badging`: `pl.wataha.app`, label „Polska Wataha", launchable `MainActivity`, minSdk 26, target 34, komplet uprawnień | ✅ |
| **Testy jednostkowe routingu mesh** (`MeshSimTest`, JVM): trasa A→D przez B→C; awaria B → A→C→D; awaria B i C → brak trasy (store-and-forward); wznowienie węzła → trasa wraca; trasa A→C główną przez B; po awarii B → krawędź zapasowa | ✅ **7/7** |
| **Backend E2E (kontrakt REST identyczny jak w APK)**: health → login (cwilk/haslo123) → POST /api/posts (sync queue) → POST /api/comm (ramka mesh) → GET /api/sync-pull → panel web → POST /api/crisis | ✅ 7/7 |

> **Uczciwa uwaga:** środowisko nie ma KVM, więc **nie** uruchomiono wirtualnego emulatora Androida.
> Weryfikacja „instalowalności" opiera się na podpisanym, poprawnie zbudowanym APK + testach jednostkowych
> i kontraktowych. Pierwsze uruchomienie na telefonie zajmie < 2 min (instrukcja niżej).

## B. Checklista ręczna na telefonie (17 punktów — tryb demo)

| # | Scenariusz | Ekran | Oczekiwanie |
|---|-----------|-------|-------------|
| 1 | Utwórz konto testowe | Start → Utwórz konto | konto istnieje w bazie (offline) |
| 2 | Zaloguj się | Logowanie (demo `cwilk/haslo123`) | pulpit z „Witaj, Czarny Wilk" |
| 3 | Utwórz ogłoszenie | Ogłoszenia → ➕ | widać na liście • odznaka „Pierwsza Szarża" |
| 4 | Przeglądaj ogłoszenia | Ogłoszenia | karty z emoji, dystansem, czasem |
| 5 | Szukaj ogłoszeń | pasek + filtry 📦🆘🤝 | wyniki filtrują się na żywo |
| 6 | Mapa | Mapa | punkty wg kolorów • Twoja pozycja • 🎯 GPS |
| 7 | Przykładowi użytkownicy | Reputacja | ranking 10 osób + odznaki |
| 8 | Wyślij wiadomość | Ogłoszenie → 💬 / Wiadomości | wątek + powiadomienie „Wiadomości" |
| 9 | Zaproponuj wymianę | Ogłoszenie → 🔁 | wpis w Wymianach + powiadomienie „Wymiany" |
| 10 | Zaoferuj pomoc | Pomoc → „MOGĘ POMÓC" | oferta na liście + odznaka „Solidarność" |
| 11 | Zgłoś potrzebę pomocy | Pomoc → „POTRZEBUJĘ POMOCY" | zgłoszenie na liście 🆘 |
| 12 | Oddaj rzecz | Ogłoszenia → ➕ → 📦 | wpis z kategorią wydania |
| 13 | Reputacja | Reputacja / Profil | gwiazdki, ranking, odznaki rosną |
| 14 | Odznaki | Profil → 🎖️ | minimum 1 nowa po powyższych krokach |
| 15 | TRYB KRYZYSOWY | Pulpit → 🚨 | czerwony ekran, 6 przycisków; 📢 → sygnał + alert MAX |
| 16 | Status połączenia | Sieć | 4 adaptery: Internet (status żywy), BLE, Wi-Fi Direct, LoRa; test PING; skan BLE |
| 17 | Offline | Profil → Tryb DEMO OFFLINE | nowe dane ⏳; wyłącz → SYNCHRONIZUJ TERAZ → kolejka = 0 |

## C. MESH LAB — scenariusz awarii Node B
1. Sieć → **MESH LAB**.
2. Trasa A→D: `A → B → C → D`.
3. Dotknij **NODE B** → „ZASYMULUJ AWARIĘ" → trasa: `A → C → D` (zapasowa krawędź A–C).
4. **WYŚLIJ A→D** → log pokazuje hop po hopie z RSSI i potwierdzenie.
5. Wyłącz też NODE C → pakiet trafia do bufora (**store-and-forward**), a przycisk **„WZNÓW DORĘCZANIE"** raportuje brak trasy.
6. Włącz B i C → „WZNÓW DORĘCZANIE" → pakiet doręczony.
7. „WYŚLIJ A→C" → trasa `A → B → C` (główna tańsza) — po awarii B: bezpośrednia `A → C`.

## D. Test offline na żywo (tryb samolotowy)
- Logowanie offline (konto już istnieje) ✅
- Przeglądanie wszystkich danych (Room) ✅
- Nowe wiadomości / zgłoszenia → znacznik ⏳ + „Kolejka: N" na pulpicie ✅
- Powrót sieci → WorkManager (15 min) lub ręczna synchronizacja ✅
- Backend + panel web pokazują dane z telefonu (jeśli ustawisz IP backendu) ✅
