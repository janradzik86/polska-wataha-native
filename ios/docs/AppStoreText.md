# 📝 Gotowe teksty do App Store / TestFlight (PL)
### Aplikacja: „Polska Wataha!” — wklej 1:1

---

## Nazwa i podtytuł (App Store Connect)

- **Nazwa:** Polska Wataha!
- **Podtytuł:** Pomoc lokalna, przetrwanie i łączność offline
- **Kategoria:** Lifestyle (podkategoria: Social Networking — jeśli dostępna)
- **Wiek:** 12+

## Opis (maximum 4000 znaków — poniżej od razu gotowe)

```
Polska Wataha! — lokalna sieć pomocy, która działa nawet wtedy, gdy zniknie internet.

Co znajdziesz w aplikacji?

🏠 OGŁOSZENIA LOKALNE W STYLU OLX
Oddawaj rzeczy, zgłaszaj potrzeby, oferuj pomoc — wszystko z okolicy, bez pieniędzy.
Wymiany są tu barterowe: „ja Ci pomogę, Ty mnie”.

🐺 WILK — ASYSTENT OFFLINE
Zadaj pytanie głosem albo tekstem: pierwsza pomoc, woda, ogień, schronienie, pogoda,
co spakować na 72 godziny. WILK działa w 100% bez internetu — bez żadnej subskrypcji.

🎒 PORADNIK PRZETRWANIA
12 działów wiedzy: woda, ogień, schron, medycyna, orientacja, łączność, energia,
psychologia i więcej. Wskazówka dnia i wyszukiwarka — wszystko offline.

🚨 TRYB KRYZYSOWY
Jeden przycisk: SOS, lokalizacja lub komunikat dla całej watahy. Powiadomienia
priorytetowe, status sieci na żywo, pełnoekranowe alerty w nagłych sytuacjach.

📡 SIEC MESH (A–D)
Laboratorium sieci: trasy, awarie, trasy zapasowe, bufory store-and-forward.
Gdy operatorzy padną — wataha dalej się komunikuje.

💬 WIADOMOŚCI I MAPA
Czat z sąsiadami (z dyktowaniem głosowym) i mapa dobroci: punkty pomocy,
woda, punkty medyczne, sztaby.

💡 MASZ POMYSŁ? NAPISZ!
Zgłoszenia prosto do Administracji — funkcje powstają z pomysłów użytkowników.

🦅 ODZNAKI I REPUTACJA
Orzeł, Kotwica, WILK, Strażnik… Zaufanie buduje się czynami, nie lajkami.

Dane zostają na Twoim urządzeniu (offline-first). Aplikacja nie śledzi Cię
i nie wysyła danych reklamowych. CzarneWilkiPrawdy. Kotwica. 🇵🇱
```

## Słowa kluczowe (keywords, max 100 znaków)

```
wataha,pomoc,kryzys,sos,offline,przetrwanie,sąsiedzi,wymiana,przydatne,czarne wilki
```

## Co nowego (What's New — wersja 0.2.0)

```
Pierwsze wydanie iOS. Wierny port wersji 0.2.0 dla Androida:
asystent WILK offline, poradnik przetrwania, tryb kryzysowy 🚨,
ogłoszenia lokalne (OLX-style), wymiany barterowe, sieć mesh A–D,
wiadomości z dyktowaniem, mapa dobroci, odznaki i zgłoszenia do Administracji.
```

## URL wsparcia (Support URL)

`https://github.com/<twoja-nazwa>/polska-wataha` (albo dowolna strona — np. mailto)

## URL marketingu

`https://github.com/<twoja-nazwa>/polska-wataha`

---

## 🛡️ Polityka prywatności (opublikuj jako stronę — np. GitHub Pages — i wklej link)

```html
<!DOCTYPE html>
<html lang="pl">
<head><meta charset="utf-8"><title>Polska Wataha! — Polityka prywatności</title>
<meta name="viewport" content="width=device-width, initial-scale=1"></head>
<body style="font-family:sans-serif;max-width:720px;margin:40px auto;padding:0 16px;line-height:1.6">
<h1>Polityka prywatności — Polska Wataha! (wersja 0.2.0)</h1>
<p>Ostatnia aktualizacja: 30 sierpnia 2026 r.</p>
<p>„Polska Wataha!” jest aplikacją <b>offline-first</b>. Oznacza to, że:</p>
<ul>
<li>Twoje dane (konto, ogłoszenia, wiadomości, odznaki) przechowywane są <b>lokalnie na Twoim
urządzeniu</b> w bazie SQLite.</li>
<li>Aplikacja <b>nie zbiera danych analitycznych, reklamowych ani nie śledzi</b> Twojej aktywności.</li>
<li>Mikrofon jest używany wyłącznie na Twoje żądanie — do dyktowania pytań do asystenta WILK
i wiadomości. Rozpoznawanie mowy odbywa się na urządzeniu.</li>
<li>Lokalizacja jest używana <b>tylko kontekstowo</b>: gdy dodajesz ogłoszenie lub wysyłasz sygnał
kryzysowy. Prosimy o zgodę systemową dopiero wtedy.</li>
<li>Jeśli włączysz opcjonalną synchronizację (przycisk w Ustawieniach), Twoje lokalne rekordy
(zgłoszenia „Masz pomysł?”, ogłoszenia) mogą zostać wysłane na serwer Administracji
(api.polskawataha.pl) w celu dalszego wsparcia. Nie sprzedajemy ani nie udostępniamy tych danych
osobom trzecim.</li>
<li>Bluetooth służy do wykrywania urządzeń sieci mesh w pobliżu — nie przesyła Twoich danych.</li>
</ul>
<p>Kontakt w sprawie danych: admin@polskawataha.pl</p>
<p>Możesz w każdej chwili usunąć aplikację i wszystkie jej dane z urządzenia.</p>
</body>
</html>
```

## Odpowiedzi na pytania prywatności (App Store Connect)

- **Czy zbierasz dane?** Częściowo: tylko treści zgłoszeń „Masz pomysł?” wysyłane na własny backend,
  gdy użytkownik sam to wywoła synchronizacją. Nie ma śledzenia, reklam, analityki.
- **Czy dane są powiązane z tożsamością?** Tak (nazwa użytkownika przy zgłoszeniu) — opisane
  w polityce prywatności.
- **Wymagane zgody systemowe:** aparat (zdjęcie do ogłoszenia), mikrofon (asystent/dyktowanie),
  rozpoznawanie mowy (asystent), lokalizacja (ogłoszenia/sygnały), Bluetooth (skaner mesh).
