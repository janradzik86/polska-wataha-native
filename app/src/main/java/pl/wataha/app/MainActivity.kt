package pl.wataha.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlinx.coroutines.flow.MutableStateFlow
import pl.wataha.app.ui.nav.WatahaRoot
import pl.wataha.app.ui.theme.WatahaTheme

class MainActivity : ComponentActivity() {

    /** Żądanie otwarcia Trybu Kryzysowego z powiadomienia full-screen. */
    val openCrisis = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.getBooleanExtra("open_crisis", false) == true) {
            openCrisis.value = true
        }
        val app = application as WatahaApp
        setContent {
            WatahaTheme {
                WatahaRoot(
                    app = app,
                    openCrisis = openCrisis,
                    onCrisisConsumed = { openCrisis.value = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("open_crisis", false)) {
            openCrisis.value = true
        }
    }
}
