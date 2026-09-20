# 🏗️ ARCHITEKTURA — POLSKA WATAHA V0.1

## 1. Warstwy aplikacji Android

```
┌─────────────────────────────────────────────────────────────┐
│  APPLICATION  (Kotlin + Jetpack Compose, MVVM-style)         │
│  ui/screens/* — 14 ekranów • ui/nav — NavHost (Compose)      │
│  Repository (data/repo) — jedyne źródło danych dla UI        │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  COMM SERVICE  (comm/CommManager)                            │
│  • selekcja kanału, statusy, log testów, wektor online/offline│
│  • kolejka synchronizacji (pending_sync)                     │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│  COMM ADAPTER  (comm/*Adapter.kt) — wspólny kontrakt         │
│  interface CommunicationAdapter { isAvailable(); send(); }  │
│                                                            │
│  InternetAdapter  —  DZIAŁA (REST + OkHttp)                 │
│  BluetoothAdapter  —  skan BLE działa, transport V0.3       │
│  WifiDirectAdapter —  kontrakt, transport V0.3              │
│  LoRaAdapter       —  kontrakt + SerialTransport, V0.4      │
└──────────────────────────┬──────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
   INTERNET/HTTPS     BLUETOOTH LE      Wi-Fi DIRECT / LoRa
   (REST backend)     (scan → mesh)     (USB-OTG / radio)
```

## 2. Offline-first i synchronizacja

1. Każdy zapis (ogłoszenie, wiadomość, wymiana, sygnał kryzysowy, odznaka, węzeł) idzie
   **najpierw do lokalnej bazy Room** (`AppDatabase`, 10 tabel).
2. Równolegle trafia do **kolejki `pending_sync`** jako JSON `{entity, operation, payload}`.
3. Przy dostępnym internecie rekord jest natychmiast wysyłany (POST do backendu) i usuwany z kolejki.
4. Bez internetu zostaje z flagą `pending=true` (UI pokazuje „⏳ oczekuje na wysyłkę").
5. **WorkManager** (`SyncWorker`) co 15 min (przy sieci) + ręczne „SYNCHRONIZUJ TERAZ" czyści kolejkę.
6. `CommManager.forceOffline` (pula w Profilu) pozwala testować ten przebieg bez wyłączania Wi‑Fi.

**Tabele Room:** users, posts, messages, threads, exchanges, crisis_signals, badges, nodes, markers, pending_sync.

## 3. MESH LAB — symulator routingu (bez fizycznego LoRa)

- Topologia: `A—B—C—D` (główna, 1 hop = 1) + zapasowa `A—C` (koszt 3 — jak słabsza radiówka).
- Routing: **Dijkstra** z pomijaniem węzłów wyłączonych (`comm/MeshSim.route`).
- Awaria NODE B → trasa `A→D` przestawia się na `A→C→D`; awaria B i C → **store-and-forward**:
  pakiet zostaje w buforze węzła A, a „Wznów doręczanie" wysyła go po powrocie węzłów.
- Przebieg jest animowany w logu (hop, RSSI, bateria, potwierdzenie doręczenia).
- Testy jednostkowe routingu: `app/src/test/.../MeshSimTest.kt` (7/7 zielone) — uruchamiane w buildzie.

## 4. Mapa

Celowo **bez Google Maps** (brak klucza API nie może wywalać aplikacji — wymaganie użytkownika).
`ui/screens/MapScreen` rysuje własną projekcję lat/lng na Canvas: siatka, punkty (ogłoszenia wg typu,
sygnały kryzysowe, punkty pomocy, węzły sieci, Twoja pozycja z pulsem), dotyk = wybór punktu,
przyciski „Nawiguj" (intent geo:) i „Napisz". Działa w 100% offline.
GPS: uprawnienie na żądanie; brak zgody → pozycja domyślna Warszawa + informacja.

## 5. Tryb kryzysowy

Ekran `CrisisScreen`: 6 dużych przycisków (🆘 🤝 📢 📍 🗺️ 📡), brak animacji zbędnych,
czerwone tło, duże kontrasty (oszczędność energii + czytelność w terenie).
Sygnały: SOS / KOMUNIKAT / LOKALIZACJA → tabela `crisis_signals` → kolejka → backend.
Powiadomienie „kryzysowe" (kanał MAX, dźwięk alarmu, pełny ekran) potwierdza wysyłkę.

## 6. Powiadomienia — 5 kanałów

| Kanał | ID | Priorytet | Użycie |
|-------|----|-----------|--------|
| Wiadomości | `wataha_wiadomosci` | HIGH | nowa wiadomość w wątku |
| Wymiany | `wataha_wymiany` | HIGH | status propozycji |
| Pomoc | `wataha_pomoc` | HIGH | zgłoszenia pomocy |
| Lokalne | `wataha_lokalne` | DEFAULT | odznaki, synchronizacja |
| Kryzysowe | `wataha_kryzysowe` | **MAX + alarm + pełny ekran** | alerty kryzysowe |

## 7. Uprawnienia (na żądanie, nie przy starcie!)

| Funkcja | Uprawnienie | Kiedy prosimy |
|---------|-------------|---------------|
| Mapa / lokalizacja | ACCESS_FINE/COARSE_LOCATION | przycisk „🎯 GPS" na mapie |
| Skan BLE | BLUETOOTH_SCAN/CONNECT | „URUCHOM SKAN BLE" w Statusie sieci |
| Aparat | CAMERA + FileProvider | przycisk aparatu w nowym ogłoszeniu |
| Zdjęcia | (PickVisualMedia — bez uprawnień) | wybór z galerii |
| Powiadomienia | POST_NOTIFICATIONS | pierwsze powiadomienie (Android 13+) |

Manifest deklaruje **wszystko**, co potrzebne dla przyszłych etapów (BLE mesh, Wi-Fi Direct, kryzys).
Uprawnień nie wolno nadużywać: `BLUETOOTH_ADVERTISE` tylko dla przyszłego nadawania.

## 8. Backend (czysty Node.js, 0 zależności)

`backend/server.js` — REST + JSON file storage (`backend/data/db.json`), CORS, seed danych.

| Endpoint | Metoda | Rola |
|----------|--------|------|
| `/api/health` | GET | status + liczniki (używane przez aplikację) |
| `/api/auth/login` | POST | logowanie (`cwilk / haslo123`) |
| `/api/users • posts • messages • exchanges • crisis • badges • nodes • markers` | GET/POST | współdzielone kolekcje (`{data:{…}}`) — to wysyła kolejka sync z APK |
| `/api/comm` | POST | ramka mesh z InternetAdapter (symulacja bramy) |
| `/api/comm/log` | GET | dziennik ramek (panel web) |
| `/api/sync-pull` | GET | pełny zrzut — przyszła synchronizacja dwukierunkowa |
| `/` | GET | **panel web** (biel/czerwień, kotwica, zakładki) |

Panel web (`backend/public/index.html`) — vanilla JS: Panel, Ogłoszenia, Użytkownicy, Wiadomości,
Wymiany, Kryzys, Sieć, Log LoRa; auto-odświeżanie co 10 s; konto demo.

## 9. Ścieżka rozwoju (etapy)

- **V0.1** ✅ — to wydanie (APK + społeczność + offline + mapa + kryzys + symulator mesh)
- **V0.2** ✅ w V0.1 — komunikator + mapa + offline
- **V0.3** 🔜 — nadawanie ramek BLE (BLE 5.x Long Range) i grupy Wi‑Fi Direct; testy na dwóch telefonach
- **V0.4** 🔜 — moduł LoRa (E22-900T / SX1262 przez USB-OTG) przez `LoRaAdapter.SerialTransport`
- **V0.5** 🔜 — mesh + store‑and‑forward na fizycznych węzłach (algorytm już przetestowany w MESH LAB)
- **V1.0** 🎯 — pełny system + „Tryb niepodległościowy" (działanie bez Internetu i telefonii)

## 10. Bezpieczeństwo (uczciwie)

- Backend i aplikacja obsługują **HTTP (cleartext) dopuszczony tylko dla sieci lokalnej/LAN** (dev).
  Produkcja: HTTPS + klucz API. Hash haseł: V0.1 to demo — przejście na bcrypt/Argon2 w V0.2.
- Klucz podpisujący APK jest kluczem DEMO — do publikacji wygeneruj własny i podpisz ponownie.
