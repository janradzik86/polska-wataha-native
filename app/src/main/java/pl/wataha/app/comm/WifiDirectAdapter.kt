package pl.wataha.app.comm

/**
 * WifiDirectAdapter — V0.3/V0.5. Warstwa przygotowana:
 * - wykrywanie i łączenie peer-to-peer (Wi-Fi Direct / Hotspot 2.0),
 * - kanał danych: TCP przez grupę P2P,
 * - w kolejnym etapie: transport ramek mesh store-and-forward.
 * Fizycznie nie wysyła danych, dopóki nie zostanie podłączony transport (uczciwie opisane w UI).
 */
class WifiDirectAdapter : CommunicationAdapter {

    override val kind = TransportKind.WIFI_DIRECT

    override suspend fun isAvailable(): Boolean = false

    override suspend fun send(payload: CommPayload): CommResult = CommResult.Fail(
        kind,
        "Wi-Fi Direct w przygotowaniu (V0.3). Wymaga telefonów z obsługą grup P2P."
    )

    override fun describe(): String =
        "Warstwa gotowa do podłączenia: grupy P2P, kanał TCP, mesh urządzenie–urządzenie. Testy: V0.3."
}
