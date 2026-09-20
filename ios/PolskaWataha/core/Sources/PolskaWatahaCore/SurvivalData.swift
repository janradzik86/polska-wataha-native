import Foundation

/// Jedna porada asystenta — dopasowywana po słowach kluczowych.
public struct AskEntry {
    public let topic: String
    public let keywords: [String]
    public let question: String
    public let answer: String
    public init(_ topic: String, _ keywords: [String], _ question: String, _ answer: String) {
        self.topic = topic; self.keywords = keywords; self.question = question; self.answer = answer
    }
}

/// Sekcja poradnika survivalowego (do czytania).
public struct SurvivalSection {
    public let id: String
    public let title: String
    public let emoji: String
    public let tags: [String]
    public let points: [String]
    public init(_ id: String, _ title: String, _ emoji: String, _ tags: [String], _ points: [String]) {
        self.id = id; self.title = title; self.emoji = emoji; self.tags = tags; self.points = points
    }
}

public enum SurvivalData {

    /// 💡 Wskazówka dnia — losowana rotacyjnie na pulpicie.
    public static let TIPS: [String] = [
        "Zawsze miej przy sobie naładowany powerbank i folię NRC — ratują życie w 10 minut.",
        "Woda pitna: 4 litry na osobę na dobę. Zapas 72h = minimum 12 litrów na osobę.",
        "Przed kryzysem zrób zdjęcia dokumentów — kopie w chmurze i w telefonie.",
        "Kryzysowy triage: najpierw krwotok, potem oddech, potem schronienie, potem woda.",
        "Telefon w kryzysie: tryb oszczędzania + słabsza sieć — bateria starczy na 3× dłużej.",
        "Kanał alarmowy watahy: wiadomość ze słowem SOS uruchamia pełny alert u sąsiadów.",
        "Koc termiczny NRC: srebrną stroną na zewnątrz — chroni przed przegrzaniem, złotą — przed wychłodzeniem.",
        "Wymieniaj, nie kupuj: w watahe liczy się zaufanie, nie złoto."
    ]

    /// ============ PORADNIK SURVIVALOWY — sekcje ============
    public static let sections: [SurvivalSection] = [
        SurvivalSection("bagaz72", "Checklista 72h — gotowy plecak", "🎒",
            ["pakowanie", "bagaz", "ewakuacja", "plecak", "72", "checklista"],
            ["💧 Woda 3–4 l na osobę + filtr/tabletki, 🍫 batony energetyczne i konserwy (3 dni),",
             "🧥 Odzież termiczna, folia NRC ×2, worki na śmieci (izolacja!),",
             "🔦 Latarka czołowa + zapasowe baterie, zapalniczka i świece,",
             "📻 Radio na baterie/korbkę (nasłuch kanałów),",
             "💊 Leki na 7 dni + apteczka pierwszej pomocy,",
             "🔌 Powerbank, kable, kopia dokumentów i gotówka (drobne banknoty),",
             "🗺️ Mapa papierowa okolicy + kompas, gwizdek, nóż wielofunkcyjny,",
             "🐕 Dla zwierząt: karma, smycz, dokumenty szczepień."]),
        SurvivalSection("woda", "Woda — zdobycie i oczyszczanie", "💧",
            ["woda", "picie", "oczyścić", "filtr", "deszczówka"],
            ["🔺 ZASADA: nie pij wody z nieznanego źródła bez oczyszczenia!",
             "PRZEGOTOWANIE (najpewniejsze): 1 min wrzenia + 10 min odstawienia.",
             "Tabletki uzdatniające / 2 krople jodyny na litr — 30 minut odczekać.",
             "Filtr butelkowy (np. Sawyer) lub własny: butelka → ściereczka → węgiel → piasek → żwir.",
             "Zbieraj: deszczówka (folia → wiadro), skropliny z folii na słońcu (transpiracja),",
             "Topniejący śnieg — przed użyciem przegotuj (może zawierać zanieczyszczenia).",
             "Woda z kaloryferów CO — techniczna: tylko do mycia, NIE do picia!",
             "Zapas: 4 l/os./dobę. Magazyn: ciemne, chłodne miejsce; wymieniaj co 6 miesięcy."]),
        SurvivalSection("ogien", "Ogień i ciepło bez zapałek", "🔥",
            ["ogień", "cieplo", "zapałki", "rozpalić", "zimno", "hipotermia"],
            ["Sposoby bez zapałek: bateria + folia aluminiowa / spinacz; soczewka; tarcie (łuk, świder).",
             "Rozpałka: kora brzozy, żywica, watolina, wiórki, kawałek świecy.",
             "Ognisko: miejsce osłonięte od wiatru, wianek z kamieni, NIGDY pod drzewem.",
             "W schronieniu: świeca w puszce daje światło i 15°C więcej w małym pomieszczeniu.",
             "Koc termiczny NRC: złota strona do ciała przy zimnie; srebrna na zewnątrz.",
             "Hipotermia: nie rozgrzewaj kończyn przed tułowiem — najpierw klatka piersiowa, kark, pachwiny.",
             "Uwaga na tlenek węgla: nigdy nie pal wynalazków bez wentylacji!"]),
        SurvivalSection("schron", "Schronienie — dom i teren", "🏠",
            ["schronienie", "dom", "piwnica", "izolacja", "namiot", "baza"],
            ["Najlepsza baza: piwnica/parter, ściany z cegły lub betonu, jedno wyjście awaryjne.",
             "Uszczelnij okna i drzwi (folia, taśma, mokre ręczniki).",
             "Zimą: pomieszczenie SHERPA — jeden pokój, zasłonięte okna, spanie blisko siebie.",
             "Latem: strona północna, okna zasłonięte folią odbijającą, butelki z wodą jako klimatyzator.",
             "Na zewnątrz: wigwam/ziemianka lub namiot z folii — podwójna ściana daje +8°C.",
             "Legowisko: gałęzie/tektura + koc — izolacja od ziemi jest kluczowa (nie śpij na gołej ziemi!).",
             "Znakuj punkt zbiórki dla watahy: kolorowa folia, flara, latarka w rytmie SOS."]),
        SurvivalSection("medycyna", "Pierwsza pomoc — co najważniejsze", "🩹",
            ["pierwsza pomoc", "rko", "krwotok", "zlamanie", "zadławienie", "opatrunek", "apteczka", "medycyna"],
            ["🚨 Zasada 112 — gdy brak sieci: NIE RUSZAJ, chroń, chłodź, okryj.",
             "KRWOTOK: ucisk bezpośredni 10 min + opatrunek uciskowy; opaska na kończynę tylko ostateczność.",
             "ZADŁAWIENIE: 5 uderzeń między łopatki → 5 uciśnięć nadbrzusza (Heimlich).",
             "RKO: 30 uciśnięć (5–6 cm, 100/min) : 2 wdechy, do wyczerpania lub przyjazdu pomocy.",
             "ZŁAMANIE: unieruchom w pozycji zastanej, chłód na okolicę, NIE nastawiaj.",
             "OPARZENIE: chłodna woda 15–20 min, jałowa osłona, niczego nie smaruj od razu!",
             "Apteczka watahy: rękawiczki, gaza, bandaż elastyczny, nożyczki, folia NRC, sól fizjologiczna.",
             "Leki: zapas 7 dni + lista chorób w kopercie na lodówce."]),
        SurvivalSection("orientacja", "Orientacja w terenie i mapy", "🧭",
            ["mapa", "kompas", "orientacja", "droga", "nawigacja", "słońce"],
            ["Słońce: wschód ≈ E, południe (12:00) ≈ S, zachód ≈ W — przybliżenie w PL.",
             "Cień na kiju: zaznacz koniec cienia, odczekaj 20 min, drugi znak → linia W–E.",
             "Gwiazdy: Polarną znajdziesz przedłużając 5× odległość dwóch gwiazd Wielkiej Wozy.",
             "Mech rośnie częściej na północnej stronie pni — ale to tylko wskazówka, nie pewnik.",
             "W lesie: trzymaj się grzbietów, idź po kładkach, oznaczaj trasę wstążkami.",
             "Mapa offline (pobierz przed wyjazdem) + kompas z telefonu.",
             "Punkty orientacyjne watahy: szkolne/kościelne wieże, maszty, dym — zaznacz na mapie."]),
        SurvivalSection("lacznosc", "Łączność i sygnały bez internetu", "📡",
            ["sygnał", "sos", "radio", "łączność", "mesh", "komunikacja", "wolanie", "sygnalizacja"],
            ["SOS: 3× krótki / 3× długi / 3× krótki (światło, dźwięk, gwizdek, miganie latarką).",
             "Gwizdek: sygnał słyszalny 3× dalej niż krzyk — noś przy sobie.",
             "Radio: nasłuch 4G → komunikat → wataha działa jak rozgłośnia (testuj!).",
             "Telefon w kryzysie: 112 działa nawet bez SIM (jeśli jest zasięg jakiejś sieci).",
             "MESH LAB (w tej aplikacji): testuj routing A→B→C→D i awarie — przećwicz wcześniej!",
             "Znaki na ziemi: X = potrzebuję pomocy, trójkąt = ranni, strzałka = kierunek marszu.",
             "Plan B: kartka na drzwiach z godziną i trasą — niech wataha wie, gdzie jesteś."]),
        SurvivalSection("pogoda", "Ekstremalne warunki — co robić", "🌡️",
            ["powódź", "burza", "upał", "mroz", "śnieg", "grad", "wiatr", "pogoda"],
            ["POWÓDŹ: wyżej, nie wjeżdżaj autem w wodę (30 cm znosi samochód!), prąd wyłączony przy zalaniu.",
             "BURZA: kucnij, nie bądź najwyższym punktem; od elektryki > 3 m; auto jest bezpiecznym schronem.",
             "UPAL: ciemne zasłony, wodne okłady, 1–2 l wody/godz. aktywności, unikaj 11:00–15:00.",
             "MROZ: wielowarstwowo (nie jedna gruba!), czapka i kark krytyczne; ruszaj palcami.",
             "WICHURA: zamknij okna, z dala od szyb, przygotuj powerbank i wodę — dryf sieci możliwy.",
             "SNIEG: zapas jedzenia 2 dni, łopata w aucie, napełnij wannę wodą (awaria sieci!).",
             "Po każdym zdarzeniu: sprawdź sąsiadów — seniorzy i chorzy potrzebują pomocy pierwsze."]),
        SurvivalSection("energia", "Energia i sprzęt w kryzysie", "🔌",
            ["energia", "prad", "ladowanie", "bateria", "powerbank", "telefon"],
            ["Telefon: tryb oszczędzania + zmniejsz ekran → 2–3× dłuższa praca.",
             "Powerbank 20 000 mAh = ~4 ładowania telefonu — noś przy sobie zawsze.",
             "Wyłącz zbędne aplikacje, używaj trybu samolotowego między nadaniami.",
             "Radio na korbkę: 5 min kręcenia = 10 min nasłuchu — bez baterii.",
             "Ładowanie auto/powerbank z gniazda zapalniczki (USB 12V→5V).",
             "Delikatna elektronika: odłącz od sieci przy burzy (przepięcia!).",
             "W watahe: jeden agregat = punkt ładowania dla 10 osób (wymieniaj numerem)."]),
        SurvivalSection("psychologia", "Kryzys psychiczny — jak pomóc", "🧠",
            ["stres", "strach", "panika", "psychologia", "dzieci", "emocje", "niepokój"],
            ["Panika to norma — oddychaj 4-7-8 (wdech 4, zatrzymaj 7, wydech 8).",
             "Nadaj strukturę: harmonogram dnia + małe zadania przywracają kontrolę.",
             "Dzieci: prawda w prostych słowach + przytulenie + zadanie do wykonania.",
             "Mów o faktach, nie o plotkach — fałszywe informacje podsycają strach.",
             "Pierwsze 72h: śpij wg możności, jedz regularnie, pij wodę — mózg potrzebuje paliwa.",
             "Gdy widzisz objawy ostrego stresu u siebie — zgłoś to watahe, nie jesteś sam.",
             "Zasada watahy: nikt nie zostaje sam. Jedno „jak się czujesz?” dziennie ratuje życie."]),
        SurvivalSection("zwierzeta", "Zwierzęta w kryzysie", "🐕",
            ["zwierzeta", "pies", "kot", "zabierac", "karma", "ewakuacja zwierząt"],
            ["NIE zostawiaj zwierząt! 80% zaginionych w kryzysie to efekt porzucenia.",
             "Plecak zwierzęcia: karma 7 dni, miska, smycz/kaganiec, kopia szczepień, koc.",
             "Znak identyfikacyjny: adresówka + tatuaż/żeton — zdjęcie z Tobą w telefonie.",
             "Transport: transporter/klatka, krople uspokajające przy silnym stresie — u weterynarza.",
             "Zwierzęta też się boją: ciepły kąt, koc, stały rytm karmienia.",
             "Punkty ewakuacji przyjmujące zwierzęta: zapytaj w punkcie pomocy watahy."]),
        SurvivalSection("dokumenty", "Dokumenty i gotówka", "📄",
            ["dokumenty", "gotowka", "pieniadze", "ubezpieczenie", "kopie"],
            ["Kopie: dowód, paszport, polisy, PESEL-rodziny — w kopercie wodoszczelnej + chmura.",
             "Gotówka: drobne nominały ×2 — bankomaty w kryzysie padają pierwsze.",
             "Dokumenty pojazdu, klucze zapasowe, karta EKUZ — w jednej kopercie przy plecaku.",
             "Zasady: oryginały w bezpiecznym miejscu, kopie zawsze przy Tobie.",
             "W watahe możesz też zdeponować kopię u zaufanego sąsiada (wymiana 2×)."])
    ]

    /// ============ BAZA WIEDZY ASYSTENTA (Q&A) ============
    public static let KB: [AskEntry] = [
        AskEntry("Woda", ["woda", "picie", "nawodnienie", "wodę", "wody", "oczyścić wodę", "filtr"],
            "Jak zdobyć i oczyścić wodę?",
            "💧 Woda: nie pij z nieznanego źródła bez oczyszczenia! Najpewniejsze: przegotować 1 min. Alternatywy: tabletki uzdatniające, filtr na butelkę, własny filtr (butelka → ściereczka → węgiel → piasek → żwir). Zbieraj deszczówkę folią. Norma: 4 l/os./dobę. Więcej: Poradnik Survivalowy → sekcja 💧 Woda."),
        AskEntry("Ogień", ["ogień", "ognia", "ognisko", "ognisk", "rozpalić", "zapłon", "zapałki", "ciepło", "zimno"],
            "Jak rozpalić ogień bez zapałek?",
            "🔥 Bez zapałek: bateria + folia/wełna stalowa, soczewka (okulary, lupa), tarcie łukiem. Rozpałka: kora brzozy, żywica, watolina, wiórki. Ognisko w osłoniętym miejscu, kamienny wianek, nigdy pod drzewem! Najprościej dla początkujących: zapalniczka turystyczna ×2 w plecaku."),
        AskEntry("Pierwsza pomoc", ["pierwsza pomoc", "krwotok", "rana", "zadławienie", "rko", "złamanie", "oparzenie", "apteczka"],
            "Co zrobić przy krwotoku / zadławieniu?",
            "🩹 KRWOTOK: twardy ucisk 10 minut + opatrunek uciskowy; opaska tylko ostateczność. ZADŁAWIENIE: 5 uderzeń w plecy → 5 uciśnięć nadbrzusza. RKO: 30 uciśnięć (100/min, 5–6 cm) : 2 wdechy. ZŁAMANIE: unieruchom w pozycji zastanej, nie nastawiaj! OPARZENIE: chłodna woda 15–20 min. Bez sieci: 112 działa nawet bez SIM — wybieraj!"),
        AskEntry("Schronienie", ["schronienie", "schron", "piwnica", "namiot", "nocleg", "gdzie spać"],
            "Jak przygotować schronienie?",
            "🏠 Baza: piwnica/parter, cegła lub beton, jedno wyjście awaryjne. Zimą zasłoń okna, śpijcie razem w małym pokoju. Latem: północna strona + folia na oknach. Na zewnątrz: podwójna ściana folii daje +8°C. Nie śpij na gołej ziemi — izolacja (gałęzie, tektura) to podstawa."),
        AskEntry("Checklista 72h", ["72", "checklista", "plecak", "bagaż", "ewakuacja", "spakować"],
            "Co musi być w plecaku 72h?",
            "🎒 Woda 3–4 l/os., jedzenie na 3 dni, folia NRC ×2, latarka + baterie, radio, leki 7 dni, apteczka, powerbank + kable, kopia dokumentów, gotówka, mapa i kompas, gwizdek, nóż, worki na śmieci (izolacja!), a dla pupila karma i dokumenty."),
        AskEntry("Sygnały SOS", ["sos", "sygnał", "sygnalizacja", "wezwanie", "ratunku", "wolanie"],
            "Jak nadać sygnał SOS?",
            "📡 SOS = 3 krótkie, 3 długie, 3 krótkie (światło/dźwięk/gwizdek — wszystko działa). Gwizdek słychać 3× dalej niż krzyk. Przy telefonie: 112 działa bez SIM, jeśli jest zasięg dowolnej sieci. W aplikacji: użyj 🚨 TRYBU KRYZYSOWEGO — sygnał poleci do watahy i do kolejki offline."),
        AskEntry("Telefon/bateria", ["bateria", "ładowanie", "telefon", "powerbank", "oszczędzanie"],
            "Jak oszczędzać baterię w kryzysie?",
            "🔌 Tryb oszczędzania + sieć 2G + jasność minimalna + tryb samolotowy między nadaniami = 2–3× dłużej. Powerbank 20 000 mAh to 4 ładowania. Radio korbkowe: 5 min = 10 min nasłuchu. W watahe umów punkt ładowania — jeden agregat dla 10 osób."),
        AskEntry("Kryzysowy SOS", ["pomocy", "niebezpieczeństwo", "napad", "atak", "ginę", "umieram"],
            "POTRZEBUJĘ POMOCY — co robić?",
            "🚨 Uruchom TRYB KRYZYSOWY (duży czerwony przycisk na pulpicie) → 🆘 POTRZEBUJĘ POMOCY → wyślij. Sygnał z Twoją lokalizacją trafi do watahy (online lub do kolejki). Jeśli możesz — dodaj szczegóły: co się dzieje, gdzie jesteś, czy potrzebujesz kogoś konkretnego. Trzymaj się — wataha czuwa!"),
        AskEntry("Jak dodać ogłoszenie", ["ogłoszenie", "dodać", "opublikować", "sprzedać", "oddać", "wystawić"],
            "Jak dodać ogłoszenie?",
            "📦 Na pulpicie wybierz kategorię: 📦 Oddaję / 🆘 Potrzebuję / 🤝 Mogę pomóc (albo ➕ w Ogłoszeniach). Dodaj tytuł, opis, zdjęcie (galeria lub aparat) i lokalizację → PUBLIKUJ. Bez internetu też działa — trafi do kolejki i sync-u."),
        AskEntry("Jak zaproponować wymianę", ["wymiana", "zamiana", "zamienić", "barter", "propozycja"],
            "Jak zaproponować wymianę?",
            "🔁 Otwórz ogłoszenie → ZAPROPONUJ WYMIANĘ → napisz, co dajesz w zamian (np. „powerbank za apteczkę”). Właściciel przyjmie lub odrzuci. Przyjęta wymiana zamyka ogłoszenie i buduje reputację. To serce watahy — handel oparty na zaufaniu!"),
        AskEntry("Mapa", ["mapa", "mapę", "gdzie jest", "punkt pomocy", "węzły", "trasa"],
            "Co pokazuje mapa?",
            "🗺️ Mapa w aplikacji: ogłoszenia (📦 bordo, 🆘 bursztyn, 🤝 zieleń), sygnały kryzysowe 🚨, punkty pomocy 🏫⛪🚒, sztaby 🏛, węzły sieci 📡 i Twoją pozycję. Działa offline, bez klucza Google Maps. Dotknij punktu, aby zobaczyć szczegóły i napisać do autora."),
        AskEntry("Offline", ["offline", "bez internetu", "kolejka", "synchronizacja", "synchronizuj"],
            "Jak działa tryb offline?",
            "📴 Wszystko zapisuje się najpierw lokalnie (SQLite), a przy braku internetu ląduje w kolejce (znaczek ⏳). Po powrocie sieci: automatycznie co 15 min lub ręcznie: Profil → SYNCHRONIZUJ TERAZ. W Profilu możesz włączyć „Tryb DEMO OFFLINE”, żeby to przetestować."),
        AskEntry("Mesh i LoRa", ["mesh", "lora", "węzły", "node", "store", "routing", "bramka"],
            "Jak działa mesh/LoRa?",
            "🧪 Sieć watahy to węzły A→B→C→D. W aplikacji: MESH LAB — symulujesz topologię, wyłączasz węzeł i widzisz przetasowanie trasy (np. awaria B → A→C→D) oraz store-and-forward (pakiet czeka w buforze). Fizyczne moduły LoRa podłączymy w etapie V0.4 — architektura już gotowa."),
        AskEntry("Powódź", ["powódź", "powodzi", "zalanie", "woda w domu", "zalało"],
            "Co robić przy powodzi?",
            "🌊 Idź na piętro/dach, przenieś cenne rzeczy wyżej, wyłącz prąd. NIE wjeżdżaj w zalaną ulicę (30 cm wody unosi auto!). Nie pij wody z kranu po zalaniu. Po opadnięciu: nie wchodź do budynków z pęknięciami, sprawdź sąsiadów. Punkt ewakuacji: zapytaj watahę."),
        AskEntry("Burza", ["burza", "piorun", "grzmot", "błyskawica"],
            "Jak się zachować podczas burzy?",
            "⛈️ Wejdź do budynku lub auta. Na otwartym: kucnij, nie bądź najwyższym punktem, od metalowych przedmiotów >3 m. Wyłącz z gniazdek elektronikę (przepięcia!). Odczekaj 30 min od ostatniego grzmotu."),
        AskEntry("Upał", ["upał", "gorąco", "przegrzanie", "udar", "słońce"],
            "Co robić w upał?",
            "🥵 1–2 l wody na godz. aktywności, unikaj 11–15, ciemne zasłony, mokre okłady na kark/nadgarstki. Objawy udaru (splątanie, gorąca sucha skóra): chłodź wodą i wzywaj pomoc. W watahe punktem chłodzenia może być piwnica — zapytaj."),
        AskEntry("Mróz", ["mróz", "mrozu", "zima", "śnieg", "przemarznięcie", "hipotermia"],
            "Co robić przy mrozie?",
            "🧊 Wielowarstwowo (naturalne tkaniny), czapka i kark krytyczne. Hipotermia: rozgrzewaj tułów, nie kończyny najpierw; gorący słodki napój. W domu: jeden mały pokój, zasłonięte okna, świeca w puszce. Napełnij wannę wodą przed mrozem (awaria sieci)."),
        AskEntry("Prąd", ["awaria prądu", "brak prądu", "czarno", "prąd zniknął", "agregat"],
            "Brak prądu — co robić?",
            "🔌 Lodówka: nie otwieraj — jedzenie trzyma ok. 4h (12h w zamrażarce). Używaj z wyprzedzeniem: naładuj powerbanki, telefony, napełnij wannę wodą. Radio korbkowe na nasłuch. W watahe ustal punkt z agregatem — jeden wystarczy dla wielu."),
        AskEntry("Woda w kranie", ["brak wody", "woda w kranie", "wodociąg", "zabrakło wody"],
            "Brak wody z kranu?",
            "🚱 1. Sprawdź sąsiadów (może awaria lokalna). 2. Punkt dystrybucji: zapytaj watahę (zwykle przy kościele/szkole — patrz Mapa). 3. Zapas: woda w kaloryferach to tylko techniczna! 4. Oczyszczanie: przegotuj. 5. Zgłoś w Pomocy → potrzebuję, wataha dowiezie."),
        AskEntry("Jedzenie", ["jedzenie", "jedzenia", "żywność", "głodny", "konserwy"],
            "Jak zdobyć jedzenie w kryzysie?",
            "🍲 Najpierw: wymiana w watahe (oddać co masz za coś innego). Potem: punkty zbiórki (Mapa), ogłoszenia „potrzebuję”. W terenie: jadalne korzenie, owoce leśne — ostrożnie! Zasada: przetworzone/trwałe > świeże. Nie jedz nieznanych grzybów i roślin. Zgłoś potrzebę w Pomocy — ktoś z watahy ma zapas."),
        AskEntry("Bezpieczeństwo", ["bezpieczeństwo", "złodziej", "obrona", "obcy", "niebezpiecznie", "sama"],
            "Jak zadbać o bezpieczeństwo?",
            "🛡️ Bądź w kontakcie z watahą: o której wychodzisz, kiedy wracasz. Zasada „dwóch świadków” przy obcych. Unikaj ciemnych miejsc — wyznacz oświetlone trasy z punktu pomocy. W razie zagrożenia: TRYB KRYZYSOWY → SOS, a przy telefonie 112. Zgłoś podejrzane zdarzenie — admini i sąsiedzi dostaną info."),
        AskEntry("Strach i stres", ["stres", "strach", "panika", "boję się", "niepokój", "lęk", "nerwy"],
            "Jak opanować strach?",
            "🧠 Oddech 4-7-8 (wdech 4 s, pauza 7 s, wydech 8 s) ×4. Plan na dziś: 3 małe zadania (woda, jedzenie, wiadomość do kogoś). Rozmowa: napisz do sąsiada w watahe — nikt nie pozostaje sam. Fakt > plotka: sprawdzaj informacje. Jeśli nie śpisz 2 noce — zgłoś w Pomocy."),
        AskEntry("Dzieci", ["dziecko", "dzieci", "dzieckiem", "dzieciom", "maluch"],
            "Jak pomóc dzieciom w kryzysie?",
            "👧 Prostota i prawda („jesteśmy bezpieczni, planujemy razem”), przytulenie, zadanie („pilnuj butelek z wodą”). Rutyna: posiłki, sen, zabawka z domu. Dzieci przejmują Twój spokój — mów spokojnie nawet gdy trudno. W watahe: wspólne ognisko/wieczorek pomaga najbardziej."),
        AskEntry("Zwierzęta", ["pies", "kota", "zwierzę", "koty", "psy"],
            "Co ze zwierzętami?",
            "🐕 Nigdy nie zostawiaj! Plecak pupila: karma 7 dni, miska, smycz, dokumenty, koc. Transport: transporter, przy silnym stresie krople uspokajające (zapytaj weterynarza). W ewakuacji: punkt zbiórki z informacją o zwierzętach — zapytaj w punkcie pomocy."),
        AskEntry("Wymiana zaufania", ["zaufanie", "reputacja", "odznaki", "punkty", "ranking"],
            "Jak zbudować reputację?",
            "🏅 Reputacja rośnie przez: rzetelne wymiany (przyjdź, dostarcz, potwierdź), pomoc sąsiadom, oddane rzeczy. Odznaki: Orzeł (konto), Kotwica (1. wiadomość), Pierwsza Szarża (1. ogłoszenie), Solidarność (oferta pomocy), Strażnik (tryb kryzysowy), Wilk (5 ogłoszeń), Weteran (10 wiadomości), Goniec (3 wymiany). Ranking: Reputacja w menu."),
        AskEntry("Wolontariat", ["wolontariat", "wolontariusz", "pomóc w", "angażować", "udzielić się"],
            "Jak zostać wolontariuszem watahy?",
            "🤝 Wybierz „MOGĘ POMÓC” i napisz, co potrafisz (transport, medycyna, elektronika, gotowanie). Koordynatorzy zobaczą Twoją ofertę i napiszą. Otrzymasz odznakę Solidarność, a przy stałej pomocy — miejsce w rankingu. Sprawdź też Pomoc → oferty — może ktoś już potrzebuje Ciebie!"),
        AskEntry("Pomysły", ["pomysł", "funkcję", "dodać funkcję", "ulepszenie", "sugestia"],
            "Mam pomysł na funkcję — gdzie wysłać?",
            "💡 W menu: „MASZ POMYSŁ? NAPISZ!” — wypełnij temat i treść, a zgłoszenie trafi do Administracji Watahy (z aplikacji i z panelu web). Każdy pomysł jest czytany — najczęściej zgłaszane funkcje wchodzą w kolejnych etapach."),
        AskEntry("Aplikacja", ["aplikacja", "wersja", "aktualizacja", "działa", "instalacja"],
            "Co to za aplikacja?",
            "🐺 Polska Wataha V0.2 — lokalna platforma wymiany i pomocy: ogłoszenia, komunikator, wymiany, pomoc, mapa offline, tryb kryzysowy, sieć mesh (symulator), asystent WILK i poradnik survivalowy. Wszystko działa offline. Twórca: Czarny Wilk — Strażnik Prawdy."),
        AskEntry("Gdzie jestem", ["gdzie jestem", "lokalizacja", "gps", "współrzędne", "pozycja"],
            "Jak sprawdzić swoją lokalizację?",
            "📍 Otwórz Mapa → 🎯 GPS (przyznaj uprawnienie). Twoja pozycja pojawi się jako niebieska kropka. W TRYBIE KRYZYSOWYM: 📍 MOJA LOKALIZACJA pokaże współrzędne i pozwoli je wysłać. Bez zgody GPS pokazuje pozycję domyślną (Warszawa)."),
        AskEntry("Kolejka", ["kolejka", "sync", "wysłane", "oczekuje", "pend"],
            "Co oznacza „oczekuje na wysyłkę”?",
            "⏳ Twoje dane zapisane offline czekają w kolejce na internet. Po powrocie sieci wysyłają się automatycznie (co 15 min) albo ręcznie: Profil → SYNCHRONIZUJ TERAZ. Ten mechanizm = Twoje działania nigdy nie giną."),
        AskEntry("Admini", ["administracja", "admin", "moderacja", "zgłosić", "nadużycie", "blokada"],
            "Jak skontaktować się z administracją?",
            "📮 Najszybciej: menu → „MASZ POMYSŁ? NAPISZ!” (wybierz temat: nadużycie/błąd/pomysł). W trybie kryzysowym użyj 📢 KOMUNIKAT — alert widzą wszyscy. Administratorzy odpowiadają w wiadomościach."),
        AskEntry("Powitanie", ["cześć", "hej", "witaj", "dzień dobry", "dobry", "siema", "hello", "hi"],
            "Cześć!",
            "🐺 Cześć! Jestem WILK — lokalny asystent watahy, specjalista od sytuacji kryzysowych i przetrwania. Działam w 100% offline. Zapytaj o: wodę 💧, ogień 🔥, pierwszą pomoc 🩹, schronienie 🏠, plecak 72h 🎒, sygnały SOS 📡, powódź 🌊, burzę ⛈️, stres 🧠… albo napisz własnymi słowami!"),
        AskEntry("Co umiesz", ["co umiesz", "pomoc", "funkcje", "co potrafisz", "jak działać"],
            "Co potrafisz?",
            "🏕️ Odpowiadam głosem i tekstem (naciśnij 🎤) o: survivalu, pierwszej pomocy, łączności awaryjnej, przygotowaniu 72h, wymianach i pomocy w watahe. Gdy wykryję alarm (np. „pomocy”) — zaproponuję TRYB KRYZYSOWY. Czytaj też Poradnik Survivalowy w menu — tam mam 12 pełnych sekcji.")]
}

/// Silnik odpowiedzi — deterministyczny, lokalny, offline. Bez „samonaprawiającego się” modelu.
public enum AssistantEngine {

    public struct Reply {
        public let text: String
        public let crisis: Bool
        public let topic: String?
        public init(_ text: String, crisis: Bool = false, topic: String? = nil) {
            self.text = text; self.crisis = crisis; self.topic = topic
        }
    }

    private static func normalize(_ s: String) -> String {
        s.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: Locale(identifier: "pl_PL"))
            .lowercased()
            .replacingOccurrences(of: "ł", with: "l")
    }

    public static func answer(_ raw: String) -> Reply {
        let q = normalize(raw)

        // 1. Kryzys — zawsze najwyższy priorytet
        let crisisWords = ["sos", "ratunku", "ratuj", "ginę", "umieram", "pomocy!", "niebezpieczeństwo", "napad", "atak serca", "nie oddycha", "krwawię"]
        if crisisWords.contains(where: { q.contains($0) }) {
            return Reply(
                "🚨 Rozpoznaję sygnał kryzysowy! Natychmiast:\n" +
                "1. Dotknij przycisku poniżej → TRYB KRYZYSOWY,\n" +
                "2. Wybierz 🆘 POTRZEBUJĘ POMOCY (SOS z Twoją lokalizacją poleci do watahy),\n" +
                "3. Jeśli możesz, dzwoń 112 — działa nawet bez SIM i bez internetu.\n" +
                "Trzymaj się — wataha czuwa! 🇵🇱",
                crisis: true, topic: "KRYZYS")
        }

        // 2. Dopasowanie po słowach kluczowych (scoring)
        var best: AskEntry? = nil
        var bestScore = 0
        for entry in SurvivalData.KB {
            var score = 0
            for kw in entry.keywords {
                let nkw = normalize(kw)
                if q.contains(nkw) {
                    score += nkw.count > 5 ? 3 : 2
                }
            }
            if score > bestScore { bestScore = score; best = entry }
        }

        if let b = best {
            return Reply(b.answer, crisis: false, topic: b.topic)
        }

        // 3. Fallback — pokaż tematy
        return Reply(
            "🤔 Nie mam pewnej odpowiedzi na to pytanie (działam w 100% offline — bez połączenia z modelem).\n" +
            "Najczęściej pytasz o:\n" +
            "💧 wodę i oczyszczanie • 🔥 ogień bez zapałek • 🩹 pierwszą pomoc • 🎒 plecak 72h\n" +
            "📡 sygnały SOS • 🌊 powódź • ⛈️ burzę • 🧠 stres • 🔌 brak prądu • 🔁 wymiany\n" +
            "Spróbuj zapytać prościej (np. „jak oczyścić wodę?”), albo:\n" +
            "🏕️ Otwórz Poradnik Survivalowy — mam tam 12 sekcji na każdą sytuację.",
            crisis: false, topic: nil)
    }
}
