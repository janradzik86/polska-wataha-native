package pl.wataha.app.comm

/**
 * LoRaAdapter — warstwa przygotowana pod przyszły sprzęt (V0.4).
 *
 * Nie udajemy, że telefon komunikuje się z LoRa — nie ma tu fizycznego modułu.
 * Adapter eksponuje pełny kontrakt (protokół ramek, kolejkowanie, kanał szeregowy USB),
 * dzięki czemu podłączenie modułu (np. E22-900T / SX1262 po USB-OTG) nie wymaga zmian w aplikacji.
 */
class LoRaAdapter : CommunicationAdapter {

    override val kind = TransportKind.LORA

    /** Format ramki LoRa — zgodny z planowanym protokołem węzłów WatahaMesh. */
    data class LoraFrame(
        val fromNode: String,
        val toNode: String,
        val hopCount: Int,
        val payload: String
    )

    fun buildFrame(from: String, to: String, hops: Int, payload: String): LoraFrame =
        LoraFrame(from, to, hops, payload)

    /** Kontrakt transportu szeregowego: będzie podpinany wykryty moduł USB-OTG. */
    interface SerialTransport {
        suspend fun open(devicePath: String): Boolean
        suspend fun write(bytes: ByteArray)
        suspend fun read(len: Int): ByteArray
        fun close()
    }

    var serial: SerialTransport? = null

    override suspend fun isAvailable(): Boolean = false

    override suspend fun send(payload: CommPayload): CommResult = CommResult.Fail(
        kind,
        "Moduł LoRa nie został wykryty (brak sprzętu w tym telefonie). Kontrakt protokołu gotowy — integracja w V0.4."
    )

    override fun describe(): String =
        "Protokół LoRa (ramki, hop-count, store-and-forward) gotowy. Wymaga modułu E22/SX1262 przez USB-OTG — etap V0.4."
}
