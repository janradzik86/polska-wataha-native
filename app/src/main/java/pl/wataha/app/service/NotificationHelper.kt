package pl.wataha.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pl.wataha.app.MainActivity
import pl.wataha.app.R

/**
 * Kanały powiadomień:
 *  - Wiadomości
 *  - Wymiany
 *  - Pomoc
 *  - Lokalne
 *  - Kryzysowe (najwyższy priorytet, sygnał alarmowy)
 */
object NotificationHelper {

    const val CHAT = "wataha_wiadomosci"
    const val EXCHANGE = "wataha_wymiany"
    const val HELP = "wataha_pomoc"
    const val LOCAL = "wataha_lokalne"
    const val CRISIS = "wataha_kryzysowe"

    private const val CHAT_NAME = "Wiadomości"
    private const val EXCHANGE_NAME = "Wymiany"
    private const val HELP_NAME = "Pomoc"
    private const val LOCAL_NAME = "Lokalne"
    private const val CRISIS_NAME = "Kryzysowe"

    private var ctx: Context? = null

    fun init(context: Context) {
        ctx = context.applicationContext
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fun channel(id: String, name: String, importance: Int, desc: String, alarm: Boolean = false) {
            val ch = NotificationChannel(id, name, importance).apply {
                description = desc
                enableLights(true)
                enableVibration(true)
                if (alarm) {
                    this.setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                    )
                }
            }
            nm.createNotificationChannel(ch)
        }

        channel(CHAT, CHAT_NAME, NotificationManager.IMPORTANCE_HIGH, "Nowe wiadomości w wątkach")
        channel(EXCHANGE, EXCHANGE_NAME, NotificationManager.IMPORTANCE_HIGH, "Propozycje i statusy wymian")
        channel(HELP, HELP_NAME, NotificationManager.IMPORTANCE_HIGH, "Pomoc i zgłoszenia potrzeb")
        channel(LOCAL, LOCAL_NAME, NotificationManager.IMPORTANCE_DEFAULT, "Informacje lokalne")
        channel(CRISIS, CRISIS_NAME, NotificationManager.IMPORTANCE_MAX, "Alerty kryzysowe — najwyższy priorytet", alarm = true)
    }

    fun notify(
        channel: String,
        id: Int,
        title: String,
        text: String,
        bigText: String? = null,
        fullScreen: Boolean = false
    ) {
        val c = ctx ?: return
        if (Build.VERSION.SDK_INT >= 33 &&
            c.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(c, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_crisis", fullScreen)
        }
        val pi = PendingIntent.getActivity(
            c, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(c, channel)
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText ?: text))
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(if (channel == CRISIS) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)

        if (fullScreen) {
            builder.setFullScreenIntent(pi, true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setTimeoutAfter(0)
                .setVibrate(longArrayOf(0, 800, 400, 800))
        }

        try {
            NotificationManagerCompat.from(c).notify(id, builder.build())
        } catch (_: SecurityException) {
        }
    }
}
