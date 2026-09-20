package pl.wataha.app.ai

/**
 * Zatwierdzona warstwa wiedzy LoRa dla WILKA.
 *
 * Nie deklaruje sprzętu jako obsługiwanego, jeśli aplikacja nie ma jeszcze
 * działającego sterownika. V0.2 posiada kontrakt USB-OTG pod E22/SX1262.
 */
object LoraKnowledge {
    data class LoraReply(val topic: String, val text: String)

    private fun normalize(s: String): String {
        val nf = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
        return nf.replace("\\p{Mn}+".toRegex(), "").lowercase()
    }

    fun answer(userText: String): LoraReply? {
        val q = normalize(userText)
        val mentionsLora = listOf("lora", "sx1262", "e22", "heltec", "meshtastic", "868 mhz", "eu868", "modul radiowy")
            .any { q.contains(it) }
        if (!mentionsLora) return null

        val wantsToBuy = listOf("jaki kupic", "co kupic", "jaki modul", "nie mam modulu", "polecisz", "polec", "wybrac", "najlepszy")
            .any { q.contains(it) }

        if (wantsToBuy) {
            return LoraReply(
                "LoRa — wybór sprzętu",
                """
                📡 Jeśli dopiero wybierasz moduł LoRa do Polskiej Watahy:
                • wybieraj sprzęt przeznaczony dla regionu EU868,
                • sprawdź sposób połączenia z telefonem (USB-OTG / Bluetooth / Wi-Fi),
                • kupuj dopiero model oznaczony w aplikacji jako „obsługiwany”,
                • obecna V0.2 ma przygotowany kontrakt USB-OTG dla klasy E22/SX1262, ale fizyczna integracja jest etapem późniejszym.

                Jeśli napiszesz „chcę najprościej”, „chcę najtaniej” albo „chcę największy zasięg”, WILK może dobrać profil sprzętu, ale finalny model musi być zgodny z aktualną listą wspieranych urządzeń.
                """.trimIndent()
            )
        }

        val wantsSetup = listOf("polaczyc", "sparowac", "skonfigurowac", "ustawic", "telefon", "usb", "otg", "bluetooth", "nie widzi", "nie dziala")
            .any { q.contains(it) }

        if (wantsSetup) {
            return LoraReply(
                "LoRa — konfiguracja",
                """
                📡 Konfiguracja LoRa:
                1. Sprawdź, czy model jest na liście urządzeń obsługiwanych przez Polską Watahę.
                2. Dla połączenia USB sprawdź obsługę USB-OTG w telefonie i użyj kabla danych, nie tylko kabla do ładowania.
                3. Podłącz i zasil moduł.
                4. W aplikacji otwórz Łączność → LoRa i uruchom wykrywanie.
                5. Wybierz profil regionalny EU868.
                6. Wykonaj test nadawania/odbioru z drugim węzłem.

                Jeśli urządzenie nie zostanie wykryte, sprawdź kolejno: zasilanie → kabel/OTG → uprawnienia → firmware → zgodność modelu.
                """.trimIndent()
            )
        }

        return LoraReply(
            "LoRa — informacje",
            """
            📡 LoRa w Polskiej Watasze ma służyć do krótkich wiadomości i komunikacji awaryjnej poza internetem.
            Telefon sam nie ma radia LoRa, więc potrzebuje zgodnego modułu zewnętrznego.
            W Polsce/UE sprzęt musi być dobrany do właściwego pasma i obowiązujących parametrów radiowych.
            Napisz: „jaki moduł LoRa kupić?” albo „jak połączyć LoRa z telefonem?”, a przeprowadzę Cię dalej.
            """.trimIndent()
        )
    }
}
