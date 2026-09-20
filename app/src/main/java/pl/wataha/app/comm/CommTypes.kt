package pl.wataha.app.comm

/** Warstwy architektury komunikacji:
 *  APPLICATION → COMM SERVICE (CommManager) → COMM ADAPTER → INTERNET / BLUETOOTH / WIFI DIRECT / LoRa
 */
enum class TransportKind(val label: String, val emoji: String) {
    INTERNET("Internet", "🌐"),
    BLUETOOTH("Bluetooth LE", "🔵"),
    WIFI_DIRECT("Wi-Fi Direct", "📶"),
    LORA("LoRa 2,4 GHz", "📻")
}

enum class TransportState(val label: String) {
    READY("Gotowy"),
    SCANNING("Skanowanie…"),
    DEMO("Tryb testowy"),
    OFFLINE("Brak połączenia"),
    NO_HARDWARE("Brak sprzętu — warstwa gotowa")
}

data class CommStatus(
    val kind: TransportKind,
    val state: TransportState,
    val detail: String = ""
)

data class CommPayload(
    val from: String,
    val to: String,
    val type: String,       // MESSAGE / POST / CRISIS / PING / MESH_PACKET
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class CommResult {
    data class Ok(val adapter: TransportKind, val detail: String = "") : CommResult()
    data class Fail(val adapter: TransportKind, val reason: String) : CommResult()
}

/** Jeden wspólny interfejs dla wszystkich kanałów łączności. */
interface CommunicationAdapter {
    val kind: TransportKind

    /** Czy warstwa może REALNIE nadać teraz (sprzęt, uprawnienia, połączenie). */
    suspend fun isAvailable(): Boolean

    /** Próba wysyłki. Nie rzuca wyjątków — zwraca wynik. */
    suspend fun send(payload: CommPayload): CommResult

    fun describe(): String
}

interface TransportStatusSource {
    val statuses: kotlinx.coroutines.flow.StateFlow<Map<TransportKind, TransportState>>
}
