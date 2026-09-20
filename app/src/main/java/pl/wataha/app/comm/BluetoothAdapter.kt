package pl.wataha.app.comm

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * BluetoothAdapter — V0.3. Realny skan BLE (wykrywanie urządzeń w pobliżu).
 * Transfer ramek mesh po BLE (5.x / Long Range) zostanie podpięty w kolejnym etapie.
 * Nie udajemy działania tam, gdzie wymagany jest sprzęt.
 */
class BluetoothAdapter(private val context: Context) : CommunicationAdapter {

    override val kind = TransportKind.BLUETOOTH

    private val btManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    fun bleAvailable(): Boolean = btManager?.adapter != null

    fun scanEnabled(): Boolean =
        bleAvailable() &&
        (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)

    /** Krótki realny skan BLE (do 4 s). Zwraca liczbę wykrytych urządzeń (0 = brak, -1 = brak uprawnień/sprzętu). */
    suspend fun scanOnce(): Int {
        val adapter = btManager?.adapter ?: return -1
        if (!scanEnabled()) return -1
        val scanner = adapter.bluetoothLeScanner ?: return -1
        var count = 0
        try {
            withTimeoutOrNull(5000) {
                suspendCancellableCoroutine { cont ->
                    val cb = object : ScanCallback() {
                        override fun onScanResult(callbackType: Int, result: ScanResult) {
                            count++
                        }

                        override fun onScanFailed(errorCode: Int) {
                            if (cont.isActive) cont.resume(Unit)
                        }
                    }
                    runCatching { scanner.startScan(cb) }
                    cont.invokeOnCancellation {
                        runCatching { scanner.stopScan(cb) }
                    }
                    // automatyczne zakończenie po 4 s
                    kotlinx.coroutines.GlobalScope.launch {
                        delay(4000)
                        runCatching { scanner.stopScan(cb) }
                        if (cont.isActive) cont.resume(Unit)
                    }
                }
            }
        } catch (_: Exception) {
        }
        return count
    }

    override suspend fun isAvailable(): Boolean = scanEnabled()

    override suspend fun send(payload: CommPayload): CommResult = CommResult.Fail(
        kind,
        "Nadawanie ramek BLE będzie dostępne w V0.3 (wymaga pary urządzeń z modułem BLE mesh)."
    )

    override fun describe(): String =
        "Skaner BLE — wykrywa urządzenia w pobliżu. Transport ramek mesh: etap V0.3."
}
