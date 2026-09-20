package pl.wataha.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testy lokalnego Asystenta WILK — działa w 100% offline, deterministycznie. */
class AssistantEngineTest {

    @Test
    fun `pytanie o wode zwraca porade dotyczaca wody`() {
        val r = AssistantEngine.answer("Jak oczyścić wodę z rzeki przed piciem?")
        assertTrue("REPLY:"+r.text, r.text.contains("wod", ignoreCase = true))
        assertTrue("REPLY:"+r.text, r.text.contains("przegot"))
        assertEquals("Woda", r.topic)
    }

    @Test
    fun `slowo SOS natychmiast uruchamia tryb kryzysowy`() {
        val r = AssistantEngine.answer("SOS! potrzebuję pomocy, ginę!")
        assertTrue(r.crisis)
        assertTrue(r.text.contains("TRYB KRYZYSOWY"))
        assertTrue(r.text.contains("112"))
    }

    @Test
    fun `pytanie o plecak 72h znajduje checkliste`() {
        val r = AssistantEngine.answer("co spakować na 72 godziny ewakuacji?")
        assertTrue(r.text.contains("72h") || r.text.contains("Woda"))
        assertFalse(r.crisis)
    }

    @Test
    fun `nieznane pytanie daje fallback z tematami`() {
        val r = AssistantEngine.answer("zqbxylj coś dziwnego")
        assertTrue(r.text.contains("Nie mam pewnej odpowiedzi"))
        assertTrue(r.text.contains("Poradnik"))
    }

    @Test
    fun `dopasowanie ignoruje polskie znaki`() {
        val r = AssistantEngine.answer("Jak złożyc ognisko?")
        assertTrue(r.text.contains("Ogień") || r.text.contains("zapałek"))
    }
}
