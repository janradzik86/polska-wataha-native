# Polska Wataha! — iOS (V0.2) 🦅

Natywny port aplikacji Android **V0.2** na iOS — **identyczny zestaw funkcji i wygląd** (biało-czerwony
motyw, kotwica, CzarneWilkiPrawdy, OLX-owy dom, szuflada z ✕, WILK offline, poradnik, tryb kryzysowy,
sieć mesh A–D, barter, zgłoszenia do Administracji).

> 🚀 **NIE chcesz App Store?** Instalacja bez sklepu (Ad Hoc + link/QR) — patrz **`DYSTRYBUCJA-BEZ-APP-STORE.md`**.

> ⚠️ **Ważne o środowisku:** ten projekt został zbudowany na **Linuxie** (Debian 13) — tutaj **nie da się
> podpisać ani zbudować pliku `.ipa`** (wymaga macOS/Xcode). Dlatego dostarczamy:
> 1. **Kompletny, gotowy projekt Xcode** (SwiftPM + XcodeGen) — otwierasz go na Macu i budujesz;
> 2. **Rdzeń przetestowany na Linuxie** — `swift test` w `core`: **6/6 testów zielonych**
>    (asystent WILK: woda/SOS/72h/fallback/polskie znaki + baza wiedzy; mesh: trasy, failover, bufory).
> 3. **Dokładne kroki poniżej** — jedyne, co musisz zrobić na Macu, to wybrać zespół podpisu.

---

## 1. Co odpowiada czemu (mapowanie Android → iOS)

| Android V0.2                                     | iOS V0.2 (ten port)                                          |
|--------------------------------------------------|--------------------------------------------------------------|
| Kotlin/Compose, motyw biało-czerwony, Kotwica    | SwiftUI, te same kolory (#D4213D / #1F2A44), 🦅 w kilku miejscach |
| NavigationDrawer + przycisk ✕                    | Nietypowa szuflada lewa zamykana ✕ (DrawerView)              |
| OLX-owy dom (karty, filtry, FAB +, zdjęcia)      | HomeView: karty, czipy filtrów, FAB, PhotosPicker + Aparat    |
| Room + kolejka sync (offline-first)              | SQLite3 (systemowe) + tabela `pending_sync`                  |
| Asystent WILK (offline, deterministyczny, głos)  | WilkService: SFSpeechRecognizer + AVSpeechSynthesizer, ten sam silnik `AssistantEngine` (SwiftPM, przetestowany) |
| Poradnik przetrwania (12 działów, 8 wskazówek)   | SurvivalView — ta sama treść z `SurvivalData`                 |
| Tryb kryzysowy 🚨 + kanały powiadomień           | CrisisView + UNUserNotificationCenter (fullscreen przy 🚨)   |
| MESH LAB (A–D, Dijkstra, failover, bufory)       | MeshLabView + `MeshSim` (SwiftPM, przetestowany)              |
| Wi-Fi Direct / Bluetooth / LoRa                  | **iOS-odpowiedniki:** BLE = prawdziwy skaner CoreBluetooth; Wi-Fi Direct → **MultipeerConnectivity** (V0.3, jak w Android); LoRa = uczciwy stub (protokół ramek gotowy, wymaga modułu) |
| Mapy Google                                          | MapKit (natywne mapy iOS)                                     |
| Camera intent / galeria                          | UIImagePickerController (aparat) + PhotosPicker (galeria)    |
| Kanały: kryzys/chat/wymiany/lokalne              | NotifChannel enum + dźwięk krytyczny przy SOS                 |

**Konto demo:** `cwilk` / `haslo123` (można też zarejestrować normalne konto — to jest domyślna ścieżka,
tak jak w V0.2).

## 2. Co już przetestowano (Linux, Swift 6.0.3)

```bash
cd core && swift test
# 6 testów, 0 błędów:
#  - WILK: odpowiedź o wodzie (woda/przegotować + temat "Woda")
#  - WILK: SOS → tryb kryzysowy + 112
#  - WILK: checklista 72 h
#  - WILK: uczciwy fallback ("Nie mam pewnej odpowiedzi" + Poradnik)
#  - WILK: polskie znaki/ł, baza wiedzy (>20 pozycji, 12 działów, 8 wskazówek)
#  - MESH: trasa A→B→C→D, awaria B → trasa zapasowa A→C→D, bufor i retransmisja
```

## 3. Budowa na macOS (krok po kroku)

Wymagania: **macOS 13+**, **Xcode 15+** (SDK iOS 17/O16 — celuje w iOS 16), **XcodeGen** (CLI).
(Z Xcode 15 można też użyć `brew install xcodegen`.)

```bash
# 1. Wejdź do katalogu projektu
cd polska-wataha/ios/PolskaWataha

# 2. Wygeneruj projekt Xcode (używa project.yml + pakietu core)
xcodegen generate
# → powstaje: PolskaWataha.xcodeproj

# 3. (opcjonalnie) uruchom testy rdzenia
swift test --package-path core

# 4. Otwórz w Xcode
open PolskaWataha.xcodeproj
```

W Xcode:
1. Wybierz target **PolskaWataha → Signing & Capabilities**.
2. Zaznacz **Automatically manage signing**, wybierz swój **Team** (darmowe konto Apple wystarczy na
   urządzenie; na App Store potrzebny płatny program).
3. Zmień **Bundle Identifier** jeśli Twój Team wymaga unikalności (domyślnie `pl.wataha.ios`).
4. Uruchom na symulatorze (**⌘R**) albo na telefonie (iPhone z iOS 16+).
   * Symulator: aparat niedostępny — użyj galerii; mikrofon/Bluetooth mogą wymagać Maca z T2/Apple Silicon.
5. Podłącz iPhone'a → wybierz go jako destination → **⌘R**. Pierwszy raz: zezwól na zaufanie
   deweloperowi (Ustawienia → Ogólne → Zarządzanie urządzeniami).

**IPA (plik instalacyjny)** — dwa sposoby:

```bash
# A) Archiwum przez Xcode: Product → Archive → Distribute App → Development / Ad Hoc
#    → wygeneruje .ipa (i pozwoli zainstalować podłączonym urządzeniom przez Xcode)

# B) Z linii poleceń (po xcodegen generate):
xcodebuild -project PolskaWataha.xcodeproj -scheme PolskaWataha \
  -destination 'generic/platform=iOS' \
  -allowProvisioningUpdates \
  -exportArchive -exportOptionsPlist ExportOptions.plist archive
# (nowocześniej: xcodebuild archive … -archivePath build/PolskaWataha.xcarchive)
```

Uwaga: **w tym repozytorium nie ma i nie może być gotowego .ipa** — podpis wymaga Twojego konta Apple.
Kroki powyżej to jedyna praca po Twojej stronie.

## 4. Struktura

```
ios/PolskaWataha/
├── project.yml                     # XcodeGen — definicja projektu
├── PolskaWataha/                   # aplikacja (target)
│   ├── Core/                       # modele, SQLite, Repository, seed, motyw, powiadomienia, lokalizacja
│   ├── Comm/                       # CommManager + adaptery: Internet / BLE / Wi-Fi-Direct(stub→Multipeer) / LoRa(stub)
│   ├── Services/WilkService.swift  # głos asystenta (rozpoznawanie + synteza, offline)
│   └── Views/                      # wszystkie ekrany (SwiftUI) + Assets.xcassets (orzeł, wilki, ikona)
├── core/                           # SwiftPM „PolskaWatahaCore” — SILNIK (bez UI, testowalny wszędzie)
│   ├── Sources/PolskaWatahaCore/   # SurvivalData (asystent+poradnik), MeshSim
│   └── Tests/                      # 6 testów (zielone na Linuxie)
└── assets/raw/                     # źródłowe grafiki (orzeł z koroną, wataha wilków, ikona 1024)
```

## 5. Co jest w środku (funkcje — identycznie z Android V0.2)

* **Strona główna** w stylu OLX: karty ogłoszeń (zdjęcie, tytuł, autor, dystans, czas), wyszukiwarka,
  filtry Wszystkie/Oddaję/Potrzebuję/Mogę pomóc, przycisk **+**, odświeżanie.
* **Ogłoszenia**: dodawanie z galerii **lub aparatu**, lokalizacja kontekstowa, publikacja offline
  (kolejka `pending_sync`), zamykanie własnych, propozycje wymiany.
* **Wymiany**: propozycje do mnie / moje, akceptacja/odrzucenie, powiadomienia, demo-symulacje.
* **Wiadomości**: wątki, dymki, dyktowanie głosem, czytanie niezamówionych liczników, demo-odpowiedzi.
* **WILK**: czat głosowy i tekstowy, 6 szybkich pytań, tematy odpowiedzi, odczyt na głos, zawsze offline;
  SOS wyzwala pełnoekranowe powiadomienie 🔴.
* **Poradnik przetrwania**: 12 działów, 8 wskazówek, wyszukiwarka, rozwijane karty (treść z `SurvivalData`).
* **Tryb kryzysowy 🚨**: SOS / lokalizacja / komunikat, przełącznik trybu (czerwony pasek + powiadomienia
  priorytetowe), status sieci mesh, podgląd sygnałów.
* **Mapa dobroci**: MapKit z pinami ogłoszeń i punktów pomocy, legenda, centrowanie na mnie.
* **MESH LAB**: węzły A–D (baterie, status), Dijkstra z trasą zapasową A→C, wysyłka pakietu, awaria B,
  bufory store-and-forward, log — ten sam silnik testowany na Linuxie.
* **Moje odznaki**: profil, reputacja, statystyki, 8 odznak (Orzeł, Kotwica, WILK…).
* **„Masz pomysł? Napisz!”**: zgłoszenia do Administracji, kategorie, statusy (offline/kolejka),
  lista Twoich zgłoszeń; backend `POST /api/feedback` (patrz `/backend` — panel 💡 Pomysły).
* **Ustawienia**: tryb offline, adres API, statusy transportów, ping serwera, skan BLE, demo, o aplikacji.

## 6. Backend

Domyślny adres: `https://api.polskawataha.pl` (można zmienić w Ustawieniach). Backend Node.js + panel
web + baza SQLite + testy znajdują się w `../backend` (ten sam, którego używa Android V0.2).
Kolejka offline wysyła operacje metodą `POST /api/{users|posts|messages|exchanges|crisis|badges|nodes|markers|feedback}`.
