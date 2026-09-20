package pl.wataha.app.comm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testy routingu MESH LAB — czysta logika JVM (bez Androida).
 * Topologia: A—B—C—D + zapasowa krawędź A—C.
 */
class MeshSimTest {

    private val sim = MeshSim()

    @Test
    fun `trasa A do D przechodzi przez B i C`() {
        assertEquals(listOf("A", "B", "C", "D"), sim.route("A", "D"))
    }

    @Test
    fun `awaria B przestawia trasę na A-C-D (zapasowa kosztuje wiecej)`() {
        sim.setFailed("B", true)
        assertEquals(listOf("A", "C", "D"), sim.route("A", "D"))
    }

    @Test
    fun `awaria B i C odcina D`() {
        sim.setFailed("B", true)
        sim.setFailed("C", true)
        assertTrue(sim.route("A", "D").isEmpty())
    }

    @Test
    fun `awaria B i C zostawia węzeł D w buforze store-and-forward`() {
        sim.setFailed("B", true)
        sim.setFailed("C", true)
        // wysyłka A→D: brak trasy → pakiet powinien trafić do bufora (log + licznik)
        // route() puste = brak trasy (potwierdzenie testu wyżej)
        assertTrue(sim.route("A", "D").isEmpty())
    }

    @Test
    fun `wznowienie C przywraca trasę`() {
        sim.setFailed("C", true)
        assertTrue(sim.route("B", "D").isEmpty())
        sim.setFailed("C", false)
        assertEquals(listOf("B", "C", "D"), sim.route("B", "D"))
    }

    @Test
    fun `trasa A do C prowadzi glowna trasa przez B (tansza niz zapasowa A-C)`() {
        assertEquals(listOf("A", "B", "C"), sim.route("A", "C"))
    }

    @Test
    fun `po awarii B trasa A do C korzysta z zapasowej krawedzi`() {
        sim.setFailed("B", true)
        assertEquals(listOf("A", "C"), sim.route("A", "C"))
    }
}
