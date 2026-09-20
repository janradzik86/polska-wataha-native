# 🗝️ DANE TESTOWE — POLSKA WATAHA V0.1

## Konto demonstracyjne
```
użytkownik: cwilk
hasło:      haslo123
pseudonim:  Czarny Wilk
(funkcja:  WEJDŹ JAKO CZARNY WILK na ekranie logowania)
```

## Inne konta demo (hasło: `demo`)
| Użytkownik | Pseudonim | Reputacja | Rola |
|-----------|-----------|-----------|------|
| `ostoja` | Ostoja | 4.2 | magazyn przy kościele, pomoc |
| `sokol` | Sokół | 3.9 | kierowca, bus 9 miejsc |
| `warta` | Warta | 4.5 | sanitariuszka |
| `wichura` | Wichura | 4.0 | radiooperator |
| `znicz` | Znicz | 3.7 | senior, wolontariusz |
| `granit` | Granit | 4.8 | inżynier, schron |
| `zorza` | Zorza | 4.1 | zbiórka wody (Ursynów) |
| `bazant` | Bazant | 3.5 | elektronik |
| `lisa` | Lisa | 3.8 | mama dwójki dzieci |

## Dane startowe w APK (seed `SeedData`)
- **12 ogłoszeń**: 5×📦 oddaję • 4×🆘 potrzebuję • 3×🤝 mogę pomóc (Warszawa i okolice),
- **3 wątki** Czarny Wilk ↔ Sokół / Warta (6 wiadomości),
- **2 wymiany**: propozycja przychodząca (PENDING) + przyjęta,
- **3 sygnały kryzysowe**: KOMUNIKAT (woda przy kościele), SOS (leki), LOKALIZACJA,
- **7 odznak** przykładowych, **4 węzły MESH (A–D) + 2 bramy**, **5 punktów pomocy/sztabów**,
- lokalizacje w okolicach Warszawy (52.1–52.3 N, 20.9–21.2 E).

## Dane startowe backendu (`backend/data/db.json` — tworzony przy 1. starcie)
2 użytkowników pełnych + `cwilk`, 4 ogłoszenia, 5 węzłów, 2 markery, 1 wpis w logu comm — patrz `server.js → seedIfEmpty()`.

## Odznaki (zdobywasz w ~5 minut)
| Kod | Nazwa | Jak zdobyć |
|-----|-------|-----------|
| ORZEL | Orzeł 🦅 | założenie konta |
| KOTWICA | Kotwica ⚓ | pierwsza wiadomość |
| PIERWSZA_SZARZA | Pierwsza Szarża 🐺 | pierwsze ogłoszenie |
| SOLIDARNOSC | Solidarność 🤝 | pierwsza oferta pomocy |
| STRAZNIK | Strażnik 🚨 | użycie Trybu Kryzysowego |
| WILK | Wilk 🐺 | 5 ogłoszeń |
| WETERAN | Weteran 🎖️ | 10 wiadomości |
| GONIEC | Goniec 📯 | 3 wymiany |

## Konfiguracja backendu w APK
Domyślnie: `https://api.polskawataha.pl` (brak połączenia = tryb lokalny, zero błędów).
Preset do testów lokalnych: `http://10.0.2.2:8080` (emulator) lub `http://<IP-KOMPUTERA>:8080` (telefon w LAN).
