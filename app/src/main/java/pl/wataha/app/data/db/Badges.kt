package pl.wataha.app.data.db

/** Katalog odznak — wzorce zasług. */
data class BadgeDef(val code: String, val name: String, val desc: String, val emoji: String)

object Badges {
    val ALL = listOf(
        BadgeDef("ORZEL", "Orzeł", "Założenie konta", "🦅"),
        BadgeDef("KOTWICA", "Kotwica", "Pierwsza wysłana wiadomość", "⚓"),
        BadgeDef("PIERWSZA_SZARZA", "Pierwsza Szarża", "Pierwsze ogłoszenie", "🐺"),
        BadgeDef("SOLIDARNOSC", "Solidarność", "Pierwsza oferta pomocy", "🤝"),
        BadgeDef("STRAZNIK", "Strażnik", "Użycie Trybu Kryzysowego", "🚨"),
        BadgeDef("WILK", "Wilk", "5 opublikowanych ogłoszeń", "🐺"),
        BadgeDef("WETERAN", "Weteran", "10 wysłanych wiadomości", "🎖️"),
        BadgeDef("GONIEC", "Goniec", "3 przeprowadzone wymiany", "📯")
    )

    fun byCode(code: String): BadgeDef? = ALL.firstOrNull { it.code == code }
}
