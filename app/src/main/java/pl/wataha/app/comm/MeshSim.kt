package pl.wataha.app.comm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * MESH LAB / SYMULATOR — test architektury bez fizycznych węzłów.
 * Topologia:  A ── B ── C ── D   (+ zapasowa krawędź A ── C)
 * Węzły można wyłączać (symulacja awarii) — routing przełącza się na trasy zapasowe,
 * a pakiety zablokowane przez awarię trafiają do bufora store-and-forward.
 */
class MeshSim {

    data class SimNode(
        val id: String,
        val name: String,
        var online: Boolean = true,
        var battery: Int = 100,
        val neighbors: List<String> = emptyList()
    )

    val nodes: Map<String, SimNode> = linkedMapOf(
        "A" to SimNode("A", "NODE A", true, 87, listOf("B", "C")),
        "B" to SimNode("B", "NODE B", true, 64, listOf("A", "C")),
        "C" to SimNode("C", "NODE C", true, 91, listOf("B", "D", "A")),
        "D" to SimNode("D", "NODE D", true, 45, listOf("C"))
    )

    /** pakiety czekające w buforach węzłów (store-and-forward) */
    private val buffers = mutableMapOf<String, MutableList<String>>()

    data class Buffered(val node: String, val target: String, val payload: String)

    private val _buffered = MutableStateFlow<List<Buffered>>(emptyList())
    val buffered: StateFlow<List<Buffered>> = _buffered

    private val _events = MutableStateFlow<List<String>>(listOf(
        "MESH LAB zainicjalizowany. Topologia: A—B—C—D (główna) + A—C (zapasowa, dłuższa).",
        "Wyślij pakiet testowy A→D lub wyłącz NODE B i zaobserwuj zmianę trasy."
    ))
    val events: StateFlow<List<String>> = _events

    val running: Boolean get() = true

    fun log(line: String) {
        _events.update { (it + line).takeLast(120) }
    }

    fun resetLog() {
        _events.value = listOf("MESH LAB — log wyczyszczony.")
    }

    fun setFailed(id: String, failed: Boolean) {
        val n = nodes[id] ?: return
        n.online = !failed
        if (failed) log("⚠️ $NODE_LABEL/$id — SYMULOWANA AWARIA. Węzeł nie odpowiada.")
        else log("✅ $NODE_LABEL/$id — wznowienie pracy (bateria ${n.battery}%).")
        // zapowiedź nowej trasy po zmianie topologii
        log("🛰️ Nowa trasa A→D: ${pathLabel(route("A", "D"))}")
    }

    private fun edge(a: String, b: String) = b in (nodes[a]?.neighbors ?: emptyList())

    /** Wagi krawędzi: trasa główna A→B→C→D (1), zapasowa A→C jest „droższa” (3 — słabsza radiówka). */
    private fun weight(a: String, b: String): Int = when {
        (a == "A" && b == "C") || (a == "C" && b == "A") -> 3
        else -> 1
    }

    /** Dijkstra — trasa omija węzły wyłączone. Zwraca listę węzłów trasy lub pustą listę. */
    fun route(from: String, to: String, ignoreFailed: Boolean = true): List<String> {
        val d = mutableMapOf<String, Int>()
        val prev = mutableMapOf<String, String>()
        val visited = mutableSetOf<String>()
        val queue = nodes.keys.toMutableSet()
        nodes.keys.forEach { d[it] = Int.MAX_VALUE }
        d[from] = 0
        while (queue.isNotEmpty()) {
            val u = queue.minByOrNull { d[it] ?: Int.MAX_VALUE } ?: break
            queue.remove(u)
            if (u == to) break
            visited.add(u)
            (nodes[u]?.neighbors ?: emptyList()).forEach { v ->
                if (visited.contains(v)) return@forEach
                if (ignoreFailed && nodes[v]?.online == false) return@forEach
                val alt = (d[u] ?: Int.MAX_VALUE) + weight(u, v)
                if (alt < (d[v] ?: Int.MAX_VALUE)) {
                    d[v] = alt
                    prev[v] = u
                }
            }
        }
        if ((d[to] ?: Int.MAX_VALUE) == Int.MAX_VALUE) return emptyList()
        val path = mutableListOf(to)
        var cur = to
        while (cur != from) {
            cur = prev[cur] ?: return emptyList()
            path.add(0, cur)
        }
        return path
    }

    fun pathLabel(path: List<String>): String =
        if (path.isEmpty()) "— brak trasy —"
        else path.joinToString(" → ")

    /** Wysyłka pakietu z animacją przejść między węzłami. */
    suspend fun sendPacket(from: String, to: String, simulateRetry: Boolean = false) {
        val path = route(from, to)
        if (path.isEmpty()) {
            // store-and-forward: zablokowany pakiet zostaje w buforze
            val node = from
            buffers.getOrPut(node) { mutableListOf() }.add(to)
            refreshBuffers()
            log("📦 Pakiet $from→$to: brak trasy (awaria pośredniego węzła). STORE-AND-FORWARD — pakiet przechowywany w $NODE_LABEL/$from.")
            return
        }
        log("🛰️ Pakiet $from→$to: trasa ${pathLabel(path)} (${path.size - 1} hop${if (path.size > 2) "s" else ""}).")
        for (i in 0 until path.size - 1) {
            val u = path[i]; val v = path[i + 1]
            delay(450)
            val rssi = -(60 + Random.nextInt(25))
            log("   ↦ ${NODE_LABEL}/$u → ${NODE_LABEL}/$v: ramka przekazana (RSSI $rssi dBm, ${nodes[v]!!.battery}% baterii).")
        }
        delay(300)
        log("✅ Doręczono: $from → $to (potwierdzenie odbioru z $NODE_LABEL/$to).")
    }

    fun flushBuffers(from: String, to: String) {
        val b = buffers[from] ?: return
        val idx = b.indexOf(to)
        if (idx < 0) return
        if (route(from, to).isEmpty()) {
            log("🧊 Pakiet $from→$to nadal bez trasy — pozostaje w buforze (store-and-forward).")
            return
        }
        b.removeAt(idx)
        refreshBuffers()
        log("📤 Store-and-forward: pakiet $from→$to doręczony po wznowieniu pracy węzłów.")
    }

    fun bufferedCount(): Int = buffers.values.sumOf { it.size }

    private fun refreshBuffers() {
        _buffered.value = buffers.flatMap { (node, list) ->
            list.map { Buffered(node, it, "pakiet $node→$it") }
        }.filter { it.node != it.target } // wyświetl tylko zaległe
            .filter { entry -> route(entry.node, entry.target).isEmpty() } // wciąż zablokowane
    }

    fun signalQuality(a: String, b: String): String {
        if (!edge(a, b)) return "—"
        val dist = sqrt((a[0] - b[0]).toFloat() * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]).toFloat())
        return "${(95 - dist * 9).toInt()}%"
    }

    companion object {
        const val NODE_LABEL = "NODE"
    }
}
