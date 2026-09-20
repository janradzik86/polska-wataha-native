package pl.wataha.app.comm

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * COMM SERVICE — centrum komunikacji.
 * Łączy warstwę aplikacji z adapterami: Internet / Bluetooth / Wi-Fi Direct / LoRa.
 */
class CommManager(private val context: Context) : TransportStatusSource {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("wataha_prefs", Context.MODE_PRIVATE)

    val mesh = MeshSim()

    val internet = InternetAdapter { apiBase }
    val bluetooth = BluetoothAdapter(context)
    val wifiDirect = WifiDirectAdapter()
    val lora = LoRaAdapter()

    val adapters: List<CommunicationAdapter> = listOf(internet, bluetooth, wifiDirect, lora)

    // ---- API backendu ----
    val apiBase: String
        get() = prefs.getString("api_base", DEFAULT_API) ?: DEFAULT_API

    fun setApiBase(url: String) {
        prefs.edit().putString("api_base", url.trim()).apply()
        _statuses.update { it.toMutableMap().also { m -> m[TransportKind.INTERNET] = pollInternetStatus() } }
    }

    fun apiBaseFlowProvider(): () -> String = { apiBase }

    // ---- Status połączenia ----
    private var realOnline = MutableStateFlow(detectOnline())

    /** Flaga DEMO: wymusza offline niezależnie od sieci (test kolejki synchronizacji). */
    val forceOffline = MutableStateFlow(prefs.getBoolean("force_offline", false))

    val online: StateFlow<Boolean> = MutableStateFlow(realOnline.value && !forceOffline.value).also {
        scope.launch {
            realOnline.collect { r ->
                it.value = r && !forceOffline.value
            }
        }
        scope.launch {
            forceOffline.collect { f ->
                it.value = realOnline.value && !f
            }
        }
    }

    fun setForceOffline(v: Boolean) {
        prefs.edit().putBoolean("force_offline", v).apply()
        forceOffline.value = v
    }

    private val cm: ConnectivityManager?
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private fun detectOnline(): Boolean {
        val c = cm ?: return false
        val caps = c.getNetworkCapabilities(c.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun installNetworkCallback() {
        val c = cm ?: return
        try {
            c.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    realOnline.value = true
                }

                override fun onLost(network: Network) {
                    realOnline.value = detectOnline() || hasOtherNetwork()
                }

                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    realOnline.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }
            })
        } catch (_: Exception) {
        }
    }

    private fun hasOtherNetwork(): Boolean {
        val c = cm ?: return false
        return c.allNetworks.any {
            (c.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) == true
        }
    }

    // ---- Status adapterów ----
    private val _statuses = MutableStateFlow(initialStatuses())
    override val statuses: StateFlow<Map<TransportKind, TransportState>> = _statuses

    private fun initialStatuses(): Map<TransportKind, TransportState> = mapOf(
        TransportKind.INTERNET to (if (realOnline.value) TransportState.READY else TransportState.OFFLINE),
        TransportKind.BLUETOOTH to TransportState.DEMO,
        TransportKind.WIFI_DIRECT to TransportState.DEMO,
        TransportKind.LORA to TransportState.NO_HARDWARE
    )

    private fun pollInternetStatus(): TransportState =
        if (detectOnline() && !forceOffline.value) TransportState.READY else TransportState.OFFLINE

    /** Odświeżenie statusów po zmianie sieci / uprawnień. */
    fun refreshStatuses() {
        _statuses.update { m ->
            m.toMutableMap().apply {
                put(TransportKind.INTERNET, pollInternetStatus())
            }
        }
    }

    // ---- Log testów ----
    val testLog: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    fun logTest(line: String) {
        testLog.update { (it + line).takeLast(80) }
    }

    /** Test adaptera „na żywo”. */
    suspend fun testTransport(kind: TransportKind) {
        val adapter = adapters.firstOrNull { it.kind == kind } ?: return
        logTest("— Test: ${adapter.kind.label} —")
        try {
            val avail = adapter.isAvailable()
            logTest("   dostępność warstwy: ${if (avail) "TAK" else "NIE"}")
            val res = adapter.send(CommPayload(from = "app-test", to = "backend-or-mesh", type = "PING", body = "ping-wataha"))
            when (res) {
                is CommResult.Ok -> logTest("   wysyłka OK (${res.detail})")
                is CommResult.Fail -> logTest("   wysyłka: ${res.reason}")
            }
        } catch (e: Exception) {
            logTest("   błąd testu: ${e.message?.take(80)}")
        }
        logTest("   ${adapter.describe()}")
    }

    /** Szybki przegląd środowiska BLE (liczy wykryte urządzenia w pobliżu). */
    suspend fun bleScanCount(): Int = bluetooth.scanOnce()

    companion object {
        const val DEFAULT_API = "https://api.polskawataha.pl"
    }
}
