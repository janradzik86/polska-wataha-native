package pl.wataha.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WilkAdaptiveCoreTest {
    @Test
    fun `prosba o prostsze wyjasnienie nie zmienia tematu odpowiedzi`() {
        val core = WilkAdaptiveCore()
        val first = core.ask("Jak rozpalić ogień?")
        val second = core.ask("Nie rozumiem, wyjaśnij prościej")
        assertEquals("Ogień", first.topic)
        assertEquals("Ogień", second.topic)
        assertEquals(ExplanationStyle.SIMPLE, second.style)
        assertTrue(second.text.contains("Najprościej"))
    }

    @Test
    fun `wyjasnij inaczej przechodzi do innego stylu`() {
        val core = WilkAdaptiveCore()
        core.ask("Jak oczyścić wodę?")
        val next = core.explainDifferently()
        assertNotNull(next)
        assertEquals(ExplanationStyle.SIMPLE, next!!.style)
        assertEquals("Woda", next.topic)
    }

    @Test
    fun `feedback uczy preferowanego stylu ale nie tresci procedury`() {
        val store = InMemoryWilkLearningStore()
        val core = WilkAdaptiveCore(store)
        val first = core.ask("Jak rozpalić ogień?")
        val simple = core.ask("napisz prościej")
        core.feedback(simple.id, FeedbackRating.HELPFUL, "tak jest zrozumiale")
        val state = core.getLearningState("Ogień")
        assertNotNull(state)
        assertEquals(ExplanationStyle.SIMPLE, state!!.preferredStyle)
        assertTrue(state.learnedPhrases.contains("tak jest zrozumiale"))
        assertFalse(first.crisis)
    }

    @Test
    fun `wilk rozpoznaje pytanie o zakup lora`() {
        val core = WilkAdaptiveCore()
        val answer = core.ask("Nie mam modułu LoRa. Jaki moduł kupić do telefonu?")
        assertEquals("LoRa — wybór sprzętu", answer.topic)
        assertTrue(answer.text.contains("EU868"))
    }

    @Test
    fun `wilk prowadzi konfiguracje lora`() {
        val core = WilkAdaptiveCore()
        val answer = core.ask("Jak połączyć moduł LoRa z telefonem przez USB OTG?")
        assertEquals("LoRa — konfiguracja", answer.topic)
        assertTrue(answer.text.contains("USB-OTG"))
        assertTrue(answer.text.contains("test", ignoreCase = true))
    }
}
