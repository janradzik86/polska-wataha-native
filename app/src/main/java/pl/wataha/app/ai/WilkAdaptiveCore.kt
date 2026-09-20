package pl.wataha.app.ai

/**
 * WILK vNext — warstwa adaptacyjna nad istniejącym, deterministycznym AssistantEngine.
 *
 * Zasada bezpieczeństwa:
 * - fakty/procedury pochodzą wyłącznie z zatwierdzonej bazy wiedzy,
 * - "uczenie" zmienia sposób tłumaczenia, nie treść procedury,
 * - brak internetu nie blokuje działania.
 */
enum class ExplanationStyle { STANDARD, SIMPLE, STEP_BY_STEP, TECHNICAL }

enum class FeedbackRating { HELPFUL, NOT_HELPFUL }

data class WilkContext(
    val preferredStyle: ExplanationStyle = ExplanationStyle.STANDARD,
    val lastTopic: String? = null,
    val lastAnswerId: String? = null
)

data class WilkAnswer(
    val id: String,
    val text: String,
    val topic: String?,
    val crisis: Boolean,
    val style: ExplanationStyle,
    val source: String = "verified-local-kb"
)

data class TopicLearning(
    val topic: String,
    val preferredStyle: ExplanationStyle = ExplanationStyle.STANDARD,
    val helpfulByStyle: Map<ExplanationStyle, Int> = emptyMap(),
    val notHelpfulByStyle: Map<ExplanationStyle, Int> = emptyMap(),
    val learnedPhrases: Set<String> = emptySet()
)

interface WilkLearningStore {
    fun read(topic: String): TopicLearning?
    fun write(state: TopicLearning)
}

class InMemoryWilkLearningStore : WilkLearningStore {
    private val states = mutableMapOf<String, TopicLearning>()
    override fun read(topic: String): TopicLearning? = states[topic]
    override fun write(state: TopicLearning) { states[state.topic] = state }
}

/**
 * Główny punkt wejścia dla nowego WILKA.
 * UI powinno docelowo rozmawiać z tą klasą zamiast bezpośrednio z AssistantEngine.
 */
class WilkAdaptiveCore(
    private val store: WilkLearningStore = InMemoryWilkLearningStore()
) {
    private var lastAnswer: WilkAnswer? = null

    fun ask(text: String, context: WilkContext = WilkContext()): WilkAnswer {
        val normalized = normalize(text)

        val requestedStyle = detectRequestedStyle(normalized)
        if (requestedStyle != null && lastAnswer != null) {
            val previous = lastAnswer!!
            val rewritten = ExplanationLibrary.rewrite(previous.topic, previous.text, requestedStyle)
            val result = previous.copy(
                id = answerId(previous.topic, requestedStyle, rewritten),
                text = rewritten,
                style = requestedStyle
            )
            rememberStyle(previous.topic, requestedStyle)
            lastAnswer = result
            return result
        }

        val lora = LoraKnowledge.answer(text)
        val base = if (lora != null) {
            AssistantEngine.Reply(lora.text, crisis = false, topic = lora.topic)
        } else {
            AssistantEngine.answer(text)
        }

        val learnedStyle = base.topic?.let { store.read(it)?.preferredStyle }
        val style = requestedStyle ?: learnedStyle ?: context.preferredStyle
        val rendered = if (style == ExplanationStyle.STANDARD) base.text
        else ExplanationLibrary.rewrite(base.topic, base.text, style)

        val answer = WilkAnswer(
            id = answerId(base.topic, style, rendered),
            text = rendered,
            topic = base.topic,
            crisis = base.crisis,
            style = style
        )
        lastAnswer = answer
        return answer
    }

    fun explainDifferently(): WilkAnswer? {
        val previous = lastAnswer ?: return null
        val nextStyle = when (previous.style) {
            ExplanationStyle.STANDARD -> ExplanationStyle.SIMPLE
            ExplanationStyle.SIMPLE -> ExplanationStyle.STEP_BY_STEP
            ExplanationStyle.STEP_BY_STEP -> ExplanationStyle.TECHNICAL
            ExplanationStyle.TECHNICAL -> ExplanationStyle.SIMPLE
        }
        val rewritten = ExplanationLibrary.rewrite(previous.topic, previous.text, nextStyle)
        val result = previous.copy(
            id = answerId(previous.topic, nextStyle, rewritten),
            text = rewritten,
            style = nextStyle
        )
        rememberStyle(previous.topic, nextStyle)
        lastAnswer = result
        return result
    }

    fun feedback(answerId: String, rating: FeedbackRating, userComment: String? = null) {
        val answer = lastAnswer ?: return
        if (answer.id != answerId) return
        val topic = answer.topic ?: return

        val current = store.read(topic) ?: TopicLearning(topic)
        val helpful = current.helpfulByStyle.toMutableMap()
        val notHelpful = current.notHelpfulByStyle.toMutableMap()

        when (rating) {
            FeedbackRating.HELPFUL -> helpful[answer.style] = (helpful[answer.style] ?: 0) + 1
            FeedbackRating.NOT_HELPFUL -> notHelpful[answer.style] = (notHelpful[answer.style] ?: 0) + 1
        }

        val learnedPhrases = current.learnedPhrases.toMutableSet()
        userComment?.trim()?.takeIf { it.length in 3..160 }?.let { learnedPhrases += normalize(it) }

        val preferred = choosePreferredStyle(helpful, notHelpful, current.preferredStyle)
        store.write(
            current.copy(
                preferredStyle = preferred,
                helpfulByStyle = helpful,
                notHelpfulByStyle = notHelpful,
                learnedPhrases = learnedPhrases
            )
        )
    }

    fun getLearningState(topic: String): TopicLearning? = store.read(topic)

    private fun rememberStyle(topic: String?, style: ExplanationStyle) {
        if (topic == null) return
        val current = store.read(topic) ?: TopicLearning(topic)
        store.write(current.copy(preferredStyle = style))
    }

    private fun choosePreferredStyle(
        helpful: Map<ExplanationStyle, Int>,
        notHelpful: Map<ExplanationStyle, Int>,
        fallback: ExplanationStyle
    ): ExplanationStyle {
        return ExplanationStyle.entries.maxByOrNull { style ->
            (helpful[style] ?: 0) * 2 - (notHelpful[style] ?: 0)
        }?.takeIf { (helpful[it] ?: 0) > 0 } ?: fallback
    }

    private fun detectRequestedStyle(q: String): ExplanationStyle? = when {
        listOf("nie rozumiem", "prosciej", "latwiej", "po ludzku", "wytlumacz prosto", "nie da sie tak", "nie kumam")
            .any { q.contains(it) } -> ExplanationStyle.SIMPLE
        listOf("krok po kroku", "po kolei", "co najpierw", "instrukcja krok")
            .any { q.contains(it) } -> ExplanationStyle.STEP_BY_STEP
        listOf("technicznie", "dokladniej", "szczegolowo", "parametry")
            .any { q.contains(it) } -> ExplanationStyle.TECHNICAL
        else -> null
    }

    private fun answerId(topic: String?, style: ExplanationStyle, text: String): String {
        val hash = (topic.orEmpty() + "|" + style.name + "|" + text).hashCode()
        return "wilk-" + hash.toUInt().toString(16)
    }

    private fun normalize(s: String): String {
        val nf = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
        return nf.replace("\\p{Mn}+".toRegex(), "").lowercase()
    }
}

object ExplanationLibrary {
    fun rewrite(topic: String?, original: String, style: ExplanationStyle): String {
        if (style == ExplanationStyle.STANDARD) return original
        return when (topic) {
            "Ogień" -> fire(style)
            "Woda" -> water(style)
            "LoRa — wybór sprzętu" -> loraBuy(style)
            "LoRa — konfiguracja" -> loraSetup(style)
            else -> generic(original, style)
        }
    }

    private fun fire(style: ExplanationStyle): String = when (style) {
        ExplanationStyle.SIMPLE ->
            "🔥 Najprościej: przygotuj trzy rzeczy. 1) Coś bardzo łatwopalnego: sucha kora brzozy, wata albo drobne suche wióry. 2) Cienkie, suche patyczki. 3) Dopiero potem grubsze drewno. Najpierw zapal rozpałkę, gdy płomień jest stabilny dodawaj cienkie patyczki, a grube drewno dopiero na końcu. Nie rozpalaj ognia w zamkniętym pomieszczeniu bez bezpiecznej wentylacji."
        ExplanationStyle.STEP_BY_STEP ->
            "🔥 Ogień krok po kroku:\n1. Wybierz bezpieczne miejsce z dala od materiałów łatwopalnych.\n2. Zbierz suchą rozpałkę: korę, watę lub cienkie wióry.\n3. Przygotuj cienkie patyczki i osobno grubsze drewno.\n4. Zapal rozpałkę.\n5. Gdy płomień trzyma się sam, dokładaj cienkie patyczki.\n6. Dopiero gdy powstanie mocniejszy żar, dodaj grubsze drewno.\n7. Nie zostawiaj ognia bez nadzoru i nie używaj go w zamkniętym pomieszczeniu bez bezpiecznej wentylacji."
        ExplanationStyle.TECHNICAL ->
            "🔥 Zasada techniczna: ogień potrzebuje paliwa, tlenu i odpowiedniej temperatury. Zacznij od materiału o małej masie i dużej powierzchni (rozpałka), potem zwiększaj przekrój paliwa stopniowo: rozpałka → cienkie patyczki → grubsze drewno. Jeśli od razu położysz duże kawałki, odbiorą ciepło i zduszą płomień. Zachowaj przepływ powietrza i bezpieczną odległość od otoczenia."
        ExplanationStyle.STANDARD -> error("handled above")
    }

    private fun water(style: ExplanationStyle): String = when (style) {
        ExplanationStyle.SIMPLE ->
            "💧 Jeśli nie masz pewności, że woda jest bezpieczna, nie pij jej od razu. Usuń widoczne zabrudzenia przez czystą tkaninę lub filtr, a następnie zastosuj sprawdzoną metodę uzdatniania zgodną z lokalnymi zaleceniami. Jeśli masz możliwość, wybierz wodę butelkowaną lub oficjalny punkt dystrybucji."
        ExplanationStyle.STEP_BY_STEP ->
            "💧 Woda krok po kroku:\n1. Najpierw wybierz najczystsze dostępne źródło.\n2. Usuń widoczne osady przez filtr lub czystą tkaninę.\n3. Zastosuj zatwierdzoną metodę dezynfekcji zgodnie z instrukcją produktu albo oficjalnymi zaleceniami.\n4. Przechowuj uzdatnioną wodę w czystym, zamkniętym pojemniku.\n5. Jeśli służby podają komunikat o skażeniu chemicznym, samo gotowanie może nie wystarczyć — stosuj komunikaty służb."
        ExplanationStyle.TECHNICAL ->
            "💧 Filtracja mechaniczna usuwa część zawiesin, ale nie gwarantuje usunięcia wszystkich drobnoustrojów ani zanieczyszczeń chemicznych. Dlatego etap oczyszczania i dezynfekcji trzeba dobierać do rodzaju zagrożenia. Przy oficjalnym ostrzeżeniu o skażeniu stosuj wyłącznie metody wskazane przez służby."
        ExplanationStyle.STANDARD -> error("handled above")
    }

    private fun loraBuy(style: ExplanationStyle): String = when (style) {
        ExplanationStyle.SIMPLE ->
            "📡 Jeśli jeszcze nic nie kupiłeś, wybierz sprzęt zgodny z europejskim pasmem 868 MHz i z metodą połączenia, którą Polska Wataha faktycznie obsługuje. W obecnej V0.2 przygotowana jest ścieżka USB-OTG dla modułów klasy E22/SX1262. Przed zakupem sprawdź w aplikacji listę „Obsługiwane urządzenia”, bo wsparcie sprzętu będzie rozszerzane."
        ExplanationStyle.STEP_BY_STEP ->
            "📡 Dobór LoRa krok po kroku:\n1. Sprawdź, czy telefon obsługuje USB-OTG.\n2. W Polsce/UE wybieraj wariant na 868 MHz.\n3. Dla obecnej architektury V0.2 najbezpieczniej wybierać sprzęt klasy E22/SX1262 zgodny z planowanym połączeniem USB-OTG.\n4. Nie kupuj wariantu 915 MHz przeznaczonego na inne regiony.\n5. Przed płatnością porównaj model z aktualną listą urządzeń obsługiwanych przez Polską Watahę."
        ExplanationStyle.TECHNICAL ->
            "📡 V0.2 ma kontrakt SerialTransport pod USB-OTG i ramki WatahaMesh. Warstwa LoRa wymienia E22-900T / SX1262 jako planowaną klasę sprzętu. Wariant radiowy musi odpowiadać regulacjom regionu EU868. Konkretne urządzenie powinno być oznaczone jako wspierane dopiero po wdrożeniu sterownika i teście nadawania/odbioru w aplikacji."
        ExplanationStyle.STANDARD -> error("handled above")
    }

    private fun loraSetup(style: ExplanationStyle): String = when (style) {
        ExplanationStyle.SIMPLE ->
            "📡 Podłącz moduł do telefonu przez obsługiwany interfejs, uruchom Polską Watahę i otwórz sekcję LoRa. Aplikacja powinna wykryć urządzenie, poprosić o potrzebne uprawnienia i wykonać test połączenia. Jeśli go nie widzi, sprawdź OTG, kabel, zasilanie i zgodność modelu."
        ExplanationStyle.STEP_BY_STEP ->
            "📡 Konfiguracja LoRa krok po kroku:\n1. Sprawdź zgodność modułu z Polską Watahą.\n2. Podłącz go do telefonu przez wymagany interfejs.\n3. Nadaj aplikacji wymagane uprawnienia.\n4. Otwórz Łączność → LoRa → Wykryj urządzenie.\n5. Wybierz profil EU868.\n6. Uruchom test nadawania/odbioru.\n7. Jeśli test nie przejdzie, sprawdź zasilanie, kabel/OTG, firmware i zgodność sprzętu."
        ExplanationStyle.TECHNICAL ->
            "📡 Konfiguracja powinna przejść przez warstwę transportu aplikacji: wykrycie urządzenia → otwarcie transportu → inicjalizacja profilu EU868 → test ramki WatahaMesh → potwierdzenie odbioru. Nie zapisuj konfiguracji jako poprawnej, dopóki aplikacja nie potwierdzi realnej transmisji."
        ExplanationStyle.STANDARD -> error("handled above")
    }

    private fun generic(original: String, style: ExplanationStyle): String = when (style) {
        ExplanationStyle.SIMPLE -> "🐺 Prościej:\n" + original.replace(";", ".").replace(" → ", " potem ")
        ExplanationStyle.STEP_BY_STEP -> {
            val parts = original.split(Regex("[.!?]\\s+")).map { it.trim() }.filter { it.isNotBlank() }.take(6)
            if (parts.isEmpty()) original
            else "🐺 Krok po kroku:\n" + parts.mapIndexed { i, p -> (i + 1).toString() + ". " + p }.joinToString("\n")
        }
        ExplanationStyle.TECHNICAL -> "🐺 Szczegółowo:\n" + original
        ExplanationStyle.STANDARD -> original
    }
}
