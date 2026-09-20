package pl.wataha.app.comm

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * InternetAdapter — jedyny w pełni działający kanał w V0.1.
 * Obsługuje REST-owy backend (offline-first: aplikacja działa też bez niego).
 */
class InternetAdapter(private val baseUrlProvider: () -> String) : CommunicationAdapter {

    override val kind = TransportKind.INTERNET

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    data class HttpResponse(val code: Int, val body: String)

    fun serverUrl(): String = baseUrlProvider().trimEnd('/')

    /** Zapytanie REST — zwraca odpowiedź lub rzuca wyjątek przy braku sieci. */
    @Throws(Exception::class)
    fun request(method: String, path: String, jsonBody: JSONObject? = null): HttpResponse {
        val url = serverUrl() + path
        val builder = Request.Builder().url(url)
        if (jsonBody != null) {
            builder.post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
        } else {
            builder.method(method, null)
        }
        client.newCall(builder.build()).execute().use { resp ->
            return HttpResponse(resp.code, resp.body?.string() ?: "")
        }
    }

    /** Bezpieczne (nie rzucające) sprawdzenie czy backend odpowiada. */
    fun check(): Pair<Boolean, String> = try {
        val r = request("GET", "/api/health")
        if (r.code == 200) true to "OK — backend odpowiada (${serverUrl()})"
        else false to "Backend: HTTP ${r.code}"
    } catch (e: Exception) {
        false to "Brak połączenia z backendem (${e.javaClass.simpleName})"
    }

    override suspend fun isAvailable(): Boolean = check().first

    override suspend fun send(payload: CommPayload): CommResult = try {
        val json = JSONObject()
            .put("from", payload.from)
            .put("to", payload.to)
            .put("type", payload.type)
            .put("body", payload.body)
            .put("timestamp", payload.timestamp)
        val r = request("POST", "/api/comm", json)
        if (r.code in 200..299) CommResult.Ok(kind, "HTTP ${r.code}")
        else CommResult.Fail(kind, "HTTP ${r.code}: ${r.body.take(120)}")
    } catch (e: Exception) {
        CommResult.Fail(kind, "Brak internetu: ${e.message?.take(80)}")
    }

    override fun describe(): String =
        "REST do backendu (${serverUrl()}). Offline-first: brak odpowiedzi = kolejka synchronizacji."
}
