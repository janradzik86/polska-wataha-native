# WILK vNext — handoff do finalnej Polskiej Watahy

Status: **integration-ready after target-map verification and build/test pass**

## Co zawiera pakiet

- `WilkAdaptiveCore.kt`
  - adaptacyjne style wyjaśniania,
  - "wyjaśnij inaczej",
  - feedback 👍/👎,
  - zapamiętywanie sposobu tłumaczenia,
  - lokalne uczenie sformułowań/synonimów,
  - blokada swobodnego przepisywania odpowiedzi kryzysowych,
  - sugestie akcji dla aplikacji.

- `WilkPersistentStore.kt`
  - trwała lokalna pamięć adaptacji,
  - brak synchronizacji z serwerem,
  - brak zależności od internetu.

- `VerifiedCrisisKnowledge.kt`
  - konserwatywna warstwa odpowiedzi o podwyższonym ryzyku,
  - RKO, silne krwawienie, zadławienie, oparzenia,
  - hipotermia, udar cieplny, bezpieczeństwo wody, powódź,
  - pierwszeństwo nad starszą bazą SurvivalData dla tych tematów.

- `LoraKnowledge.kt`
  - nauka i konfiguracja LoRa,
  - brak fałszywych deklaracji zgodności,
  - konkretne modele mogą być polecane dopiero z rejestru urządzeń faktycznie przetestowanych w finalnej aplikacji.

- `WilkIntegrationContract.kt`
  - kontrakt akcji:
    - Tryb Kryzysowy,
    - 112,
    - mapa offline,
    - moja pozycja,
    - LoRa,
    - mesh,
    - poradnik.

## Integracja w docelowej aplikacji

1. Przenieś cały pakiet `pl.wataha.app.ai` do docelowego modułu Android.
2. Nie podłączaj UI bezpośrednio do starego `AssistantEngine`.
3. Głównym wejściem ma być `WilkAdaptiveCore`.
4. Dla produkcji twórz rdzeń przez:
   `createPersistentWilk(context, loraRegistry)`
5. Zmapuj `WilkActionSuggestion` na prawdziwe ekrany/funkcje aplikacji.
6. `OPEN_OFFLINE_MAP` ma prowadzić do finalnej mapy offline przygotowanej przez Groka.
7. `SHOW_MY_LOCATION` ma korzystać z istniejącej warstwy lokalizacji aplikacji.
8. `OPEN_LORA` ma być aktywne tylko dla funkcji faktycznie zaimplementowanych.
9. Dostarcz implementację `LoraSupportRegistry` z finalnej aplikacji.
10. Jeśli lista przetestowanych urządzeń jest pusta, WILK nie może polecać konkretnego modelu jako zgodnego.

## Zasady bezpieczeństwa

- Fakty/procedury nie uczą się z feedbacku użytkownika.
- Feedback zmienia wyłącznie preferowany sposób wyjaśniania.
- Odpowiedzi oznaczone `crisis=true` nie są automatycznie przepisywane na luźniejsze style.
- Brak internetu nie blokuje rdzenia WILKA.
- WILK nie zastępuje 112, ratownika ani lekarza.
- Nie dodawaj leków, dawek ani diagnoz bez osobno zweryfikowanej bazy.

## Mapa offline

Finalny WILK jest przygotowany do mapy przez akcje:
- `OPEN_OFFLINE_MAP`
- `SHOW_MY_LOCATION`

Po ukończeniu map przez Groka trzeba tylko podpiąć te akcje do rzeczywistych ekranów i sprawdzić zachowanie przy całkowicie wyłączonym internecie.

## Testy

Plik:
`app/src/test/java/pl/wataha/app/ai/WilkAdaptiveCoreTest.kt`

obejmuje:
- zmianę stylu,
- feedback,
- lokalne uczenie fraz,
- blokadę przepisywania odpowiedzi kryzysowych,
- RKO z warstwy zweryfikowanej,
- akcje mapy offline,
- ścieżki LoRa.

Dodany workflow:
`.github/workflows/android-wilk-tests.yml`

Jeżeli GitHub Actions nie uruchomi workflow automatycznie, przed mergem uruchom lokalnie:
`./gradlew testDebugUnitTest`

## Gate przed finalnym mergem

Nie merge'ować do produkcji dopóki:
- mapa offline finalnej Polskiej Watahy nie jest znana i podpięta,
- finalny rejestr LoRa nie jest podłączony,
- testy Android nie przejdą,
- nie zostanie wykonany krótki test ręczny WILKA bez internetu.
