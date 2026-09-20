import Foundation

/// Wzorcowe dane demo — aplikacja nie jest pusta po instalacji.
/// Konto demonstracyjne:  cwilk / haslo123
enum Seed {
    static func seedIfEmpty(_ db: Database) {
        if db.countUsers() > 0 { return }
        let now = nowMs()

        struct U { let id: String; let user: String; let name: String; let phone: String; let bio: String; let rep: Double; let badges: String; let lat: Double; let lng: Double }

        let users: [U] = [
            U(id: "u_cwilk", user: "cwilk", name: "Czarny Wilk", phone: "+48 600 100 200", bio: "Strażnik Prawdy. Koordynator osiedla. Wataha wzywa — odpowiadam.", rep: 4.7, badges: "ORZEL,WILK,KOTWICA,STRAZNIK", lat: 52.2297, lng: 21.0122),
            U(id: "u_ostoja", user: "ostoja", name: "Ostoja", phone: "+48 601 211 300", bio: "Pomagam sąsiadom od 2022. Magazyn przy kościele.", rep: 4.2, badges: "SOLIDARNOSC,ORZEL", lat: 52.2367, lng: 20.9657),
            U(id: "u_sokol", user: "sokol", name: "Sokół", phone: "+48 602 342 100", bio: "Kierowca. Wożę rzeczy i ludzi w kryzysie.", rep: 3.9, badges: "ORZEL", lat: 52.2657, lng: 20.9478),
            U(id: "u_warta", user: "warta", name: "Warta", phone: "+48 603 111 222", bio: "Sanitariuszka. Apteczka zawsze pod ręką.", rep: 4.5, badges: "ORZEL,KOTWICA", lat: 52.2222, lng: 21.0455),
            U(id: "u_wichura", user: "wichura", name: "Wichura", phone: "+48 604 555 100", bio: "Radiooperator. Nasłuch 24/7.", rep: 4.0, badges: "ORZEL", lat: 52.1790, lng: 21.0150),
            U(id: "u_znicz", user: "znicz", name: "Znicz", phone: "+48 605 777 300", bio: "Senior. Wolontariusz społeczny.", rep: 3.7, badges: "ORZEL", lat: 52.2445, lng: 21.0890),
            U(id: "u_granit", user: "granit", name: "Granit", phone: "+48 606 888 400", bio: "Inżynier. Schron, prąd, woda — pomagam technicznie.", rep: 4.8, badges: "SOLIDARNOSC,WILK", lat: 52.3130, lng: 20.9070),
            U(id: "u_zorza", user: "zorza", name: "Zorza", phone: "+48 607 999 500", bio: "Zbiórka wody i żywności. Punkt: Ursynów.", rep: 4.1, badges: "SOLIDARNOSC", lat: 52.1150, lng: 21.0450),
            U(id: "u_bazant", user: "bazant", name: "Bazant", phone: "+48 608 000 600", bio: "Elektronik. Ładowarki, powerbanki, naprawy.", rep: 3.5, badges: "ORZEL", lat: 52.1860, lng: 20.9060),
            U(id: "u_lisa", user: "lisa", name: "Lisia", phone: "+48 609 123 700", bio: "Mama dwójki dzieci. Uczę się pomagać.", rep: 3.8, badges: "ORZEL", lat: 52.2530, lng: 21.1550)
        ]
        for u in users {
            db.upsertUser(User(id: u.id, username: u.user, displayName: u.name, phone: u.phone,
                               passwordHash: u.user == "cwilk" ? "haslo123" : "demo",
                               bio: u.bio, reputation: u.rep, badges: u.badges, isDemo: true,
                               lat: u.lat, lng: u.lng, lastSeen: now - 3_600_000))
        }

        func usr(_ id: String) -> U { users.first { $0.id == id }! }

        struct P { let t: PostType; let title: String; let body: String; let u: U }
        let posts: [P] = [
            P(t: .GIVE, title: "Powerbank 20 000 mAh do oddania", body: "Napięcie: 20 000 mAh, 2×USB, ładowanie 18 W. Sprawny, po testach. Odbiór Praga-Północ, mogę też podrzucić do południa.", u: usr("u_warta")),
            P(t: .GIVE, title: "Latarka czołowa + zapasowe baterie", body: "Czołówka 400 lm, 3 tryby. Baterie AA nowe w opakowaniu (6 szt.).", u: usr("u_granit")),
            P(t: .GIVE, title: "Koc termiczny NRC — nowy", body: "Nieotwierany, w folii. Do oddania za darmo. Mogę dostarczyć osobiście na terenie Woli.", u: usr("u_ostoja")),
            P(t: .GIVE, title: "Apteczka domowa (komplet)", body: "Bandaże, gaza, sól fizjologiczna, opatrunki, rękawiczki. Zestaw po przeglądzie, wszystko ważne do 2027.", u: usr("u_cwilk")),
            P(t: .GIVE, title: "Woda butelkowana 6×1,5 l", body: "Nieotwarte, termin do 2027. Odbiór Ursynów, okolice Imielina.", u: usr("u_zorza")),
            P(t: .NEED, title: "Potrzebuję leków na cukrzycę", body: "Insulina + paski do glukometru. Apteki nieczynne, mam zapas na 2 dni. Proszę o kontakt.", u: usr("u_znicz")),
            P(t: .NEED, title: "Ładowarka USB-C do telefonu", body: "Kabel i ładowarka spłonęły w skoku napięcia. Telefon to moje jedyne łącze z rodziną.", u: usr("u_bazant")),
            P(t: .NEED, title: "Pomoc przy przeprowadzce rzeczy", body: "Dwie osoby + samochód, okolice Targówka. Rzeczy: łóżko, szafa, pudła. Produkt na wieczór.", u: usr("u_lisa")),
            P(t: .NEED, title: "Brak wody — okolica Woli", body: "Od wczoraj nie ma wody w kranach. Szukam punktu dystrybucji wody pitnej.", u: usr("u_wichura")),
            P(t: .OFFER, title: "Mogę podwieźć do punktu pomocy", body: "Bus 9 miejsc, jeżdżę na trasie: Bielany–Wola–Centrum. Pomagam rodzinom i seniorom.", u: usr("u_sokol")),
            P(t: .OFFER, title: "Udostępniam schron w piwnicy", body: "Ceglany budynek, ogrzewanie, prąd z agregatu, miejsce dla 8 osób. Wejście od podwórza.", u: usr("u_granit")),
            P(t: .OFFER, title: "Pomogę w naprawie radia/odbiornika", body: "Radio CB i krótkofalówki — znam się na tym. Mam zapas części.", u: usr("u_ostoja"))
        ]
        for (i, p) in posts.enumerated() {
            db.upsertPost(Post(id: "p_seed_\(i)", authorId: p.u.id, authorName: p.u.name, type: p.t.rawValue,
                               title: p.title, body: p.body, lat: p.u.lat + Double(i % 3) * 0.006,
                               lng: p.u.lng + Double(i % 4) * 0.007, createdAt: now - Int64(i) * 1_800_000,
                               status: PostStatus.OPEN))
        }

        // Wątki Czarny Wilk <-> Sokół / Warta
        db.upsertThread(ThreadRow(threadId: "t_u_cwilk_u_sokol", userId: "u_sokol", userName: "Sokół", lastText: "Świetnie, będę pod bramą o 18:00.", lastAt: now - 3_600_000, unread: 0))
        db.upsertMessage(Message(id: "m_1", threadId: "t_u_cwilk_u_sokol", fromId: "u_sokol", toId: "u_cwilk", text: "Witaj. Widzę, że masz apteczkę — mam transport na Bemowo, mogę ją zawieźć do punktu pomocy.", createdAt: now - 5_400_000))
        db.upsertMessage(Message(id: "m_2", threadId: "t_u_cwilk_u_sokol", fromId: "u_cwilk", toId: "u_sokol", text: "Dzięki, Sokół! To by pomogło. Kiedy możesz podjechać?", createdAt: now - 4_500_000))
        db.upsertMessage(Message(id: "m_3", threadId: "t_u_cwilk_u_sokol", fromId: "u_sokol", toId: "u_cwilk", text: "Świetnie, będę pod bramą o 18:00.", createdAt: now - 3_600_000))
        db.upsertThread(ThreadRow(threadId: "t_u_cwilk_u_warta", userId: "u_warta", userName: "Warta", lastText: "Potwierdzam wymianę — biorę powerbank.", lastAt: now - 7_200_000, unread: 0))
        db.upsertMessage(Message(id: "m_4", threadId: "t_u_cwilk_u_warta", fromId: "u_warta", toId: "u_cwilk", text: "Cześć! Czy powerbank jest jeszcze dostępny? Przyda mi się do radiotelefonu.", createdAt: now - 8_100_000))
        db.upsertMessage(Message(id: "m_5", threadId: "t_u_cwilk_u_warta", fromId: "u_cwilk", toId: "u_warta", text: "Jest. Wymiana: apteczka w zamian? Zgadzam się.", createdAt: now - 7_500_000))
        db.upsertMessage(Message(id: "m_6", threadId: "t_u_cwilk_u_warta", fromId: "u_warta", toId: "u_cwilk", text: "Potwierdzam wymianę — biorę powerbank.", createdAt: now - 7_200_000))

        db.upsertExchange(Exchange(id: "e_1", postId: "p_seed_3", postTitle: "Apteczka domowa (komplet)", proposerId: "u_sokol", proposerName: "Sokół", ownerId: "u_cwilk", message: "Proponuję wymianę: transport apteczki do punktu pomocy na Bemowie. W zamian mogę dostarczyć 10 litrów wody.", status: ExchangeStatus.PENDING, createdAt: now - 2_000_000))
        db.upsertExchange(Exchange(id: "e_2", postId: "p_seed_0", postTitle: "Powerbank 20 000 mAh do oddania", proposerId: "u_cwilk", proposerName: "Czarny Wilk", ownerId: "u_warta", message: "Proponuję wymianę: powerbank za apteczkę podręczną. Mogę podjechać dziś.", status: ExchangeStatus.ACCEPTED, createdAt: now - 7_300_000))

        db.upsertCrisis(CrisisSignal(id: "c_1", userId: "u_ostoja", userName: "Ostoja", type: CrisisTypes.BROADCAST, text: "KOMUNIKAT: woda pitna dostępna przy kościele św. Anny. Punkt czynny do 22:00. Przekażcie dalej.", lat: 52.2367, lng: 20.9657, createdAt: now - 12_000_000))
        db.upsertCrisis(CrisisSignal(id: "c_2", userId: "u_znicz", userName: "Znicz", type: CrisisTypes.SOS, text: "SOS: kończą mi się leki na cukrzycę. Potrzebuję kontaktu z sanitariuszem.", lat: 52.2445, lng: 21.0890, createdAt: now - 9_000_000))
        db.upsertCrisis(CrisisSignal(id: "c_3", userId: "u_wichura", userName: "Wichura", type: CrisisTypes.LOCATION, text: "Moja lokalizacja: Wola, okolice Parku Sowińskiego.", lat: 52.1790, lng: 21.0150, createdAt: now - 6_000_000))

        func badge(_ id: String, _ uid: String, _ code: String, _ name: String, _ desc: String, _ emoji: String, _ t: Int64) {
            db.insertBadge(Badge(id: id, userId: uid, code: code, name: name, desc: desc, emoji: emoji, earnedAt: t))
        }
        badge("b1", "u_cwilk", "ORZEL", "Orzeł", "Założenie konta", "🦅", now - 30_000_000)
        badge("b2", "u_cwilk", "KOTWICA", "Kotwica", "Pierwsza wiadomość", "⚓", now - 25_000_000)
        badge("b3", "u_cwilk", "WILK", "Wilk", "5 ogłoszeń", "🐺", now - 20_000_000)
        badge("b4", "u_cwilk", "STRAZNIK", "Strażnik", "Użycie trybu kryzysowego", "🚨", now - 15_000_000)
        badge("b5", "u_ostoja", "SOLIDARNOSC", "Solidarność", "Pierwsza deklaracja pomocy", "🤝", now - 22_000_000)
        badge("b6", "u_sokol", "ORZEL", "Orzeł", "Założenie konta", "🦅", now - 28_000_000)
        badge("b7", "u_warta", "KOTWICA", "Kotwica", "Pierwsza wiadomość", "⚓", now - 18_000_000)

        db.upsertNode(NetworkNode(id: "n_A", name: "NODE A", type: NodeType.MESH, status: NodeStatus.ONLINE, lat: 52.2297, lng: 21.0122, battery: 87, lastSeen: now - 60_000))
        db.upsertNode(NetworkNode(id: "n_B", name: "NODE B", type: NodeType.MESH, status: NodeStatus.ONLINE, lat: 52.2367, lng: 20.9657, battery: 64, lastSeen: now - 60_000))
        db.upsertNode(NetworkNode(id: "n_C", name: "NODE C", type: NodeType.MESH, status: NodeStatus.ONLINE, lat: 52.2222, lng: 21.0455, battery: 91, lastSeen: now - 60_000))
        db.upsertNode(NetworkNode(id: "n_D", name: "NODE D", type: NodeType.MESH, status: NodeStatus.ONLINE, lat: 52.2657, lng: 20.9478, battery: 45, lastSeen: now - 60_000))
        db.upsertNode(NetworkNode(id: "bk1", name: "Brama Warszawa", type: NodeType.GATEWAY, status: NodeStatus.ONLINE, lat: 52.2297, lng: 21.0122, battery: 100, lastSeen: now - 60_000))
        db.upsertNode(NetworkNode(id: "bk2", name: "Węzeł rezerwowy", type: NodeType.GATEWAY, status: NodeStatus.DEGRADED, lat: 52.2657, lng: 20.9478, battery: 72, lastSeen: now - 600_000))

        db.upsertMarker(GeoMarker(id: "m1", title: "Szkoła Podstawowa nr 12", subtitle: "Punkt pomocy — zbiórka, ciepłe posiłki", kind: "POINT_POMOCY", emoji: "🏫", lat: 52.2297, lng: 21.0122))
        db.upsertMarker(GeoMarker(id: "m2", title: "Kościół św. Anny", subtitle: "Punkt dystrybucji wody", kind: "POINT_POMOCY", emoji: "⛪", lat: 52.2367, lng: 20.9657))
        db.upsertMarker(GeoMarker(id: "m3", title: "OSP Wola", subtitle: "Punkt medyczny — pierwsza pomoc", kind: "POINT_POMOCY", emoji: "🚒", lat: 52.2222, lng: 21.0455))
        db.upsertMarker(GeoMarker(id: "m4", title: "Centrum Pomocy Wola", subtitle: "Sztab — koordynacja, magazyn", kind: "SZTAB", emoji: "🏛", lat: 52.1790, lng: 21.0150))
        db.upsertMarker(GeoMarker(id: "m5", title: "Węzeł sieci — plac zamkowy", subtitle: "Repeater mesh — trasa A→B→C→D", kind: "SIEC", emoji: "📡", lat: 52.2657, lng: 20.9478))
    }
}
