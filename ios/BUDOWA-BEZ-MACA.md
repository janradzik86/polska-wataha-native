# 🛠️ Budowa instalacyjnego pliku iOS BEZ Maca — uczciwa ściąga

> 🚀 **Wolisz aplikację POZA sklepem?** (bez App Store/TestFlight) — patrz: **`DYSTRYBUCJA-BEZ-APP-STORE.md`**

> **Najpierw najważniejsze (muszę być z Tobą szczery):**
>
> 1. **Plik instalacyjny iOS (.ipa) fizycznie nie może powstać na Linuxie.** Aplikacja napisana jest we
>    frameworku **SwiftUI**, który istnieje **wyłącznie w Xcode/macOS**. Na tym komputerze (Linux) nie ma
>    ani Xcode, ani iOS SDK — nikt tego nie obejdzie. Ktoś, kto sprzedaje „IPA zbudowany na Linuxie”,
>    oszukuje albo daje plik, który nie wystartuje na iPhonie.
>
> 2. **.ipa instaluje się tylko na iPhonie.** Jeśli nie masz iPhone'a, to żaden plik nie sprawi, że
>    zobaczysz aplikację — chyba że chodzi o iPhone'a kogoś z rodziny/znajomych.
>
> 3. **Autoryzacja (podpisanie) zawsze wymaga konta Apple.** Są trzy progi:
>
> | Podpis | Konto | Dla kogo | Ważność |
> |---|---|---|---|
> | **Development** | darmowe Apple ID | max 3 urządzenia (wpisane UDID), buduje się **w Xcode na Macu** | 7 dni |
> | **Ad Hoc** | Apple Developer **$99/rok** | dowolne iPhone'y wpisane w profil | 1 rok |
> | **TestFlight** | Apple Developer **$99/rok** | do 10 000 urządzeń, przez App Store Connect | 90 dni |

---

## Wariant A — najprostszy: 30 minut na czyimś Macu (polecany)

Poproś znajomego z MacBookiem (albo użyj Maca w pracy/szkole) i wykonaj:

```bash
cd polska-wataha/ios/PolskaWataha
brew install xcodegen          # 1 min
xcodegen generate              # tworzy PolskaWataha.xcodeproj
open PolskaWataha.xcodeproj    # otwiera Xcode
```

W Xcode:
1. Wybierz target → **Signing & Capabilities** → zaznacz *Automatically manage signing*
   → wybierz **Team** (może być darmowe Apple ID właściciela iPhone'a).
2. Podłącz iPhone'a → wybierz go jako destination → **⌘R**.
   (Free ID: na telefonie Ustawienia → Ogólne → VPN i urządzenia → Zaufaj deweloperowi.)
3. Plik instalacyjny: **Product → Archive → Distribute App → Development → Export**.

To jest **najkrótsza realna droga** do działającej aplikacji na iPhonie. Gotowy projekt leży w
`deliverables/Polska-Wataha-iOS-zrodla-v0.2.zip` — wystarczy go skopiować na Maca.

---

## Wariant B — bez Maca: chmura z macOS (CI), dla posiadacza płatnego konta Apple Developer

W repo są już gotowe pliki konfiguracyjne (nic nie kodujesz, tylko klikasz):

### B1. Codemagic (najprostsze dla początkującego)
1. Załóż konto na **codemagic.io** (logowanie GitHubem).
2. Załaduj repozytorium z kodem (folder `ios/PolskaWataha` + `codemagic.yaml` w korzeniu).
3. W ustawieniach projektu: **Apple Developer Portal** → wklej klucz API / certyfikat (wymaga
   Apple Developer Program) i wybierz **Ad Hoc** albo **TestFlight**.
4. Build → dostajesz **gotowy .ipa do pobrania** + (przy TestFlight) link do instalacji.

### B2. GitHub Actions (darmowe minuty na macOS)
Spakowany gotowiec **`Polska-Wataha-GITHUB-gotowe-v0.2.zip`** wgrywasz jako korzeń repo
(2 minuty przez github.com → New repository → upload) — w środku `.github/workflows/ios-build.yml`
i build wystartuje sam.
* **Tryb bez podpisu** (`CODE_SIGNING_ALLOWED=NO`) — działa od razu, ale plik **nie zainstaluje się**
  na zwykłym iPhonie (służy do sprawdzenia, że wszystko się kompiluje).
* **Tryb z podpisem** — w repo → Settings → Secrets podaj:
  `APPLE_TEAM_ID`, `APPLE_CERT_P12`, `APPLE_CERT_PASSWORD`, `APPLE_PROVISIONING_PROFILE`,
  a także `DEVELOPMENT_TEAM` w `project.yml`. Wtedy workflow wyeksportuje **IPA podpisany Ad Hoc**.

---

## Wariant C — iPhone od kogoś, bez Apple ID dewelopera

Jeśli masz iPhone'a (swój lub znajomego) i **możesz go na chwilę podłączyć do Maca** — wystarczy
**darmowe** Apple ID właściciela telefonu. To nie wymaga płacenia $99. Limit: 3 urządzenia, instalacja
ważna 7 dni (po tym podłączasz telefon znów na minutę i odnawiasz).

---

## Czego NIESTETY nie da się zrobić (żadna droga)

* Zbudować/podpisać IPA **bez** macOS (nawet w chmurze — tam i tak działa prawdziwy macOS).
* Zainstalować aplikacji **bez** iPhone'a (IPA to nie Android — nie odpalisz go w emulatorze
  dostępnym z tego komputera, bo emulator iOS też wymaga Maca).
* Użyć **TestFlight/App Store** bez płatnego konta Apple Developer.

---

## A jeśli celujesz w TELEFON Z ANDROIDEM?

Then masz już gotowca: **`deliverables/Polska-Wataha-v0.2.apk`** — instaluje się w 2 minuty
(zezwól w telefonie na instalację z nieznanych źródeł). Działa dziś, offline, z WILK-iem, poradnikiem,
trybem kryzysowym i siecią mesh. iOS powstał jako wierny port — jeśli nie masz dostępu do iPhone'a,
to Android jest Twoją drogą podaną na tacy.
