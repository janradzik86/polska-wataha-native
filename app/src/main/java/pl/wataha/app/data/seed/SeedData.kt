package pl.wataha.app.data.seed

import pl.wataha.app.data.db.AppDao
import pl.wataha.app.data.model.BadgeEntity
import pl.wataha.app.data.model.CrisisSignalEntity
import pl.wataha.app.data.model.ExchangeEntity
import pl.wataha.app.data.model.ExchangeStatus
import pl.wataha.app.data.model.GeoMarkerEntity
import pl.wataha.app.data.model.MarkerKind
import pl.wataha.app.data.model.MessageEntity
import pl.wataha.app.data.model.NetworkNodeEntity
import pl.wataha.app.data.model.NodeStatus
import pl.wataha.app.data.model.NodeType
import pl.wataha.app.data.model.PostEntity
import pl.wataha.app.data.model.PostStatus
import pl.wataha.app.data.model.PostType
import pl.wataha.app.data.model.ThreadEntity
import pl.wataha.app.data.model.Types
import pl.wataha.app.data.model.UserEntity

/**
 * Wzorcowe dane demo — aplikacja nie jest pusta po instalacji.
 * Konto demonstracyjne:  cwilk / haslo123
 */
object SeedData {

    suspend fun seedIfEmpty(dao: AppDao) {
        if (dao.userCount() > 0) return
        val now = System.currentTimeMillis()

        data class U(
            val id: String, val user: String, val name: String, val phone: String,
            val bio: String, val rep: Double, val badges: String, val lat: Double, val lng: Double
        )

        val users = listOf(
            U("u_cwilk", "cwilk", "Czarny Wilk", "+48 600 100 200",
                "Strażnik Prawdy. Koordynator osiedla. Wataha wzywa — odpowiadam.", 4.7, "ORZEL,WILK,KOTWICA,STRAZNIK", 52.2297, 21.0122),
            U("u_ostoja", "ostoja", "Ostoja", "+48 601 211 300",
                "Pomagam sąsiadom od 2022. Magazyn przy kościele.", 4.2, "SOLIDARNOSC,ORZEL", 52.2367, 20.9657),
            U("u_sokol", "sokol", "Sokół", "+48 602 342 100",
                "Kierowca. Wożę rzeczy i ludzi w kryzysie.", 3.9, "ORZEL", 52.2657, 20.9478),
            U("u_warta", "warta", "Warta", "+48 603 111 222",
                "Sanitariuszka. Apteczka zawsze pod ręką.", 4.5, "ORZEL,KOTWICA", 52.2222, 21.0455),
            U("u_wichura", "wichura", "Wichura", "+48 604 555 100",
                "Radiooperator. Nasłuch 24/7.", 4.0, "ORZEL", 52.1790, 21.0150),
            U("u_znicz", "znicz", "Znicz", "+48 605 777 300",
                "Senior. Wolontariusz społeczny.", 3.7, "ORZEL", 52.2445, 21.0890),
            U("u_granit", "granit", "Granit", "+48 606 888 400",
                "Inżynier. Schron, prąd, woda — pomagam technicznie.", 4.8, "SOLIDARNOSC,WILK", 52.3130, 20.9070),
            U("u_zorza", "zorza", "Zorza", "+48 607 999 500",
                "Zbiórka wody i żywności. Punkt: Ursynów.", 4.1, "SOLIDARNOSC", 52.1150, 21.0450),
            U("u_bazant", "bazant", "Bazant", "+48 608 000 600",
                "Elektronik. Ładowarki, powerbanki, naprawy.", 3.5, "ORZEL", 52.1860, 20.9060),
            U("u_lisa", "lisa", "Lisia", "+48 609 123 700",
                "Mama dwójki dzieci. Uczę się pomagać.", 3.8, "ORZEL", 52.2530, 21.1550)
        )
        users.forEach {
            dao.upsertUser(
                UserEntity(
                    id = it.id, username = it.user, displayName = it.name, phone = it.phone,
                    passwordHash = if (it.user == "cwilk") "haslo123" else "demo",
                    bio = it.bio, reputation = it.rep, badges = it.badges, isDemo = true,
                    lat = it.lat, lng = it.lng, lastSeen = now - 3_600_000
                )
            )
        }

        fun usr(id: String) = users.first { it.id == id }

        fun post(t: PostType, title: String, body: String, author: U) = Triple(t, title, body) to author

        val posts = listOf(
            post(PostType.GIVE, "Powerbank 20 000 mAh do oddania", "Napięcie: 20 000 mAh, 2×USB, ładowanie 18 W. Sprawny, po testach. Odbiór Praga-Północ, mogę też podrzucić do południa.", usr("u_warta")),
            post(PostType.GIVE, "Latarka czołowa + zapasowe baterie", "Czołówka 400 lm, 3 tryby. Baterie AA nowe w opakowaniu (6 szt.).", usr("u_granit")),
            post(PostType.GIVE, "Koc termiczny NRC — nowy", "Nieotwierany, w folii. Do oddania za darmo. Mogę dostarczyć osobiście na terenie Woli.", usr("u_ostoja")),
            post(PostType.GIVE, "Apteczka domowa (komplet)", "Bandaże, gaza, sól fizjologiczna, opatrunki, rękawiczki. Zestaw po przeglądzie, wszystko ważne do 2027.", usr("u_cwilk")),
            post(PostType.GIVE, "Woda butelkowana 6×1,5 l", "Nieotwarte, termin do 2027. Odbiór Ursynów, okolice Imielina.", usr("u_zorza")),
            post(PostType.NEED, "Potrzebuję leków na cukrzycę", "Insulina + paski do glukometru. Apteki nieczynne, mam zapas na 2 dni. Proszę o kontakt.", usr("u_znicz")),
            post(PostType.NEED, "Ładowarka USB-C do telefonu", "Kabel i ładowarka spłonęły w skoku napięcia. Telefon to moje jedyne łącze z rodziną.", usr("u_bazant")),
            post(PostType.NEED, "Pomoc przy przeprowadzce rzeczy", "Dwie osoby + samochód, okolice Targówka. Rzeczy: łóżko, szafa, pudła. Produkt na wieczór.", usr("u_lisa")),
            post(PostType.NEED, "Brak wody — okolica Woli", "Od wczoraj nie ma wody w kranach. Szukam punktu dystrybucji wody pitnej.", usr("u_wichura")),
            post(PostType.OFFER, "Mogę podwieźć do punktu pomocy", "Bus 9 miejsc, jeżdżę na trasie: Bielany–Wola–Centrum. Pomagam rodzinom i seniorom.", usr("u_sokol")),
            post(PostType.OFFER, "Udostępniam schron w piwnicy", "Ceglany budynek, ogrzewanie, prąd z agregatu, miejsce dla 8 osób. Wejście od podwórza.", usr("u_granit")),
            post(PostType.OFFER, "Pomogę w naprawie radia/odbiornika", "Radio CB i krótkofalówki — znam się na tym. Mam zapas części.", usr("u_ostoja"))
        )

        posts.forEachIndexed { i, pair ->
            val tri = pair.first
            val u = pair.second
            dao.upsertPost(
                PostEntity(
                    id = "p_seed_$i",
                    authorId = u.id,
                    authorName = u.name,
                    type = tri.first.name,
                    title = tri.second,
                    body = tri.third,
                    lat = u.lat + (i % 3) * 0.006,
                    lng = u.lng + (i % 4) * 0.007,
                    createdAt = now - i * 1_800_000L,
                    status = PostStatus.OPEN
                )
            )
        }

        fun msg(id: String, thread: String, from: String, to: String, text: String, t: Long) =
            MessageEntity(id = id, threadId = thread, fromId = from, toId = to, text = text, createdAt = t)

        // Wątki Czarny Wilk <-> Sokół
        dao.upsertThread(ThreadEntity(threadId = "t_u_cwilk_u_sokol", userId = "u_sokol", userName = "Sokół",
            lastText = "Świetnie, będę pod bramą o 18:00.", lastAt = now - 3_600_000, unread = 0))
        dao.upsertMessage(msg("m_1", "t_u_cwilk_u_sokol", "u_sokol", "u_cwilk", "Witaj. Widzę, że masz apteczkę — mam transport na Bemowo, mogę ją zawieźć do punktu pomocy.", now - 5_400_000))
        dao.upsertMessage(msg("m_2", "t_u_cwilk_u_sokol", "u_cwilk", "u_sokol", "Dzięki, Sokół! To by pomogło. Kiedy możesz podjechać?", now - 4_500_000))
        dao.upsertMessage(msg("m_3", "t_u_cwilk_u_sokol", "u_sokol", "u_cwilk", "Świetnie, będę pod bramą o 18:00.", now - 3_600_000))

        // Wątek Czarny Wilk <-> Warta
        dao.upsertThread(ThreadEntity(threadId = "t_u_cwilk_u_warta", userId = "u_warta", userName = "Warta",
            lastText = "Potwierdzam wymianę — biorę powerbank.", lastAt = now - 7_200_000, unread = 0))
        dao.upsertMessage(msg("m_4", "t_u_cwilk_u_warta", "u_warta", "u_cwilk", "Cześć! Czy powerbank jest jeszcze dostępny? Przyda mi się do radiotelefonu.", now - 8_100_000))
        dao.upsertMessage(msg("m_5", "t_u_cwilk_u_warta", "u_cwilk", "u_warta", "Jest. Wymiana: apteczka w zamian? Zgadzam się.", now - 7_500_000))
        dao.upsertMessage(msg("m_6", "t_u_cwilk_u_warta", "u_warta", "u_cwilk", "Potwierdzam wymianę — biorę powerbank.", now - 7_200_000))

        // Wymiany
        dao.upsertExchange(ExchangeEntity(
            id = "e_1", postId = "p_seed_3", postTitle = "Apteczka domowa (komplet)",
            proposerId = "u_sokol", proposerName = "Sokół", ownerId = "u_cwilk",
            message = "Proponuję wymianę: transport apteczki do punktu pomocy na Bemowie. W zamian mogę dostarczyć 10 litrów wody.",
            status = ExchangeStatus.PENDING, createdAt = now - 2_000_000
        ))
        dao.upsertExchange(ExchangeEntity(
            id = "e_2", postId = "p_seed_0", postTitle = "Powerbank 20 000 mAh do oddania",
            proposerId = "u_cwilk", proposerName = "Czarny Wilk", ownerId = "u_warta",
            message = "Proponuję wymianę: powerbank za apteczkę podręczną. Mogę podjechać dziś.",
            status = ExchangeStatus.ACCEPTED, createdAt = now - 7_300_000
        ))

        // Sygnały kryzysowe
        dao.upsertCrisis(CrisisSignalEntity("c_1", "u_ostoja", "Ostoja", Types.CRISIS_BROADCAST,
            "KOMUNIKAT: woda pitna dostępna przy kościele św. Anny. Punkt czynny do 22:00. Przekażcie dalej.",
            52.2367, 20.9657, now - 12_000_000))
        dao.upsertCrisis(CrisisSignalEntity("c_2", "u_znicz", "Znicz", Types.CRISIS_SOS,
            "SOS: kończą mi się leki na cukrzycę. Potrzebuję kontaktu z sanitariuszem.", 52.2445, 21.0890, now - 9_000_000))
        dao.upsertCrisis(CrisisSignalEntity("c_3", "u_wichura", "Wichura", Types.CRISIS_LOCATION,
            "Moja lokalizacja: Wola, okolice Parku Sowińskiego.", 52.1790, 21.0150, now - 6_000_000))

        // Odznaki przykładowe
        fun badge(id: String, uid: String, code: String, name: String, desc: String, emoji: String, t: Long) =
            BadgeEntity(id = id, userId = uid, code = code, name = name, desc = desc, emoji = emoji, earnedAt = t)
        dao.insertBadge(badge("b1", "u_cwilk", "ORZEL", "Orzeł", "Założenie konta", "🦅", now - 30_000_000))
        dao.insertBadge(badge("b2", "u_cwilk", "KOTWICA", "Kotwica", "Pierwsza wiadomość", "⚓", now - 25_000_000))
        dao.insertBadge(badge("b3", "u_cwilk", "WILK", "Wilk", "5 ogłoszeń", "🐺", now - 20_000_000))
        dao.insertBadge(badge("b4", "u_cwilk", "STRAZNIK", "Strażnik", "Użycie trybu kryzysowego", "🚨", now - 15_000_000))
        dao.insertBadge(badge("b5", "u_ostoja", "SOLIDARNOSC", "Solidarność", "Pierwsza deklaracja pomocy", "🤝", now - 22_000_000))
        dao.insertBadge(badge("b6", "u_sokol", "ORZEL", "Orzeł", "Założenie konta", "🦅", now - 28_000_000))
        dao.insertBadge(badge("b7", "u_warta", "KOTWICA", "Kotwica", "Pierwsza wiadomość", "⚓", now - 18_000_000))

        // Węzły sieci (symulator + bramy)
        dao.upsertNode(NetworkNodeEntity("n_A", "NODE A", NodeType.MESH.name, NodeStatus.ONLINE.name, 52.2297, 21.0122, 87, now - 60_000))
        dao.upsertNode(NetworkNodeEntity("n_B", "NODE B", NodeType.MESH.name, NodeStatus.ONLINE.name, 52.2367, 20.9657, 64, now - 60_000))
        dao.upsertNode(NetworkNodeEntity("n_C", "NODE C", NodeType.MESH.name, NodeStatus.ONLINE.name, 52.2222, 21.0455, 91, now - 60_000))
        dao.upsertNode(NetworkNodeEntity("n_D", "NODE D", NodeType.MESH.name, NodeStatus.ONLINE.name, 52.2657, 20.9478, 45, now - 60_000))
        dao.upsertNode(NetworkNodeEntity("bk1", "Brama Warszawa", NodeType.GATEWAY.name, NodeStatus.ONLINE.name, 52.2297, 21.0122, 100, now - 60_000))
        dao.upsertNode(NetworkNodeEntity("bk2", "Węzeł rezerwowy", NodeType.GATEWAY.name, NodeStatus.DEGRADED.name, 52.2657, 20.9478, 72, now - 600_000))

        // Markery mapy — punkty pomocy
        dao.upsertMarker(GeoMarkerEntity("m1", "Szkoła Podstawowa nr 12", "Punkt pomocy — zbiórka, ciepłe posiłki", MarkerKind.POINT_POMOCY.name, "🏫", 52.2297, 21.0122))
        dao.upsertMarker(GeoMarkerEntity("m2", "Kościół św. Anny", "Punkt dystrybucji wody", MarkerKind.POINT_POMOCY.name, "⛪", 52.2367, 20.9657))
        dao.upsertMarker(GeoMarkerEntity("m3", "OSP Wola", "Punkt medyczny — pierwsza pomoc", MarkerKind.POINT_POMOCY.name, "🚒", 52.2222, 21.0455))
        dao.upsertMarker(GeoMarkerEntity("m4", "Centrum Pomocy Wola", "Sztab — koordynacja, magazyn", MarkerKind.SZTAB.name, "🏛", 52.1790, 21.0150))
        dao.upsertMarker(GeoMarkerEntity("m5", "Węzeł sieci — plac zamkowy", "Repeater mesh — trasa A→B→C→D", MarkerKind.SIEC.name, "📡", 52.2657, 20.9478))
    }
}
