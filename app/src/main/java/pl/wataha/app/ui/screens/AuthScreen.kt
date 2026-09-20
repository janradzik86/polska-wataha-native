package pl.wataha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.FlagStripe
import pl.wataha.app.ui.components.GlassCard
import pl.wataha.app.ui.components.GradientButton
import pl.wataha.app.ui.components.KotwicaLogo
import pl.wataha.app.ui.theme.WatahaColors

/**
 * Logowanie / rejestracja — NORMALNE konto użytkownika (nie demo).
 * Konto demo pozostaje ukryte jako „weryfikacyjne” dla testerów.
 */
@Composable
fun AuthScreen(
    repo: Repository,
    onLoggedIn: () -> Unit
) {
    var mode by remember { mutableStateOf(1) } // 1 = rejestracja (domyślnie!), 0 = logowanie
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (busy) return
        busy = true
        error = null
        scope.launch {
            val result = if (mode == 0) repo.login(username, password)
            else repo.register(displayName, username, phone, password)
            busy = false
            result.fold(
                onSuccess = { onLoggedIn() },
                onFailure = { error = it.message ?: "Błąd" }
            )
        }
    }

    Box(Modifier.fillMaxSize().background(WatahaColors.White)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            KotwicaLogo(size = 64.dp)
            Spacer(Modifier.height(8.dp))
            Text("POLSKA WATAHA", style = MaterialTheme.typography.headlineMedium, color = WatahaColors.Ink)
            Text("Biel i czerwień. Razem przetrwamy.", style = MaterialTheme.typography.bodyMedium, color = WatahaColors.Grey)
            Spacer(Modifier.height(6.dp))
            FlagStripe(Modifier.width(200.dp))
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.FilterChip(selected = mode == 1, onClick = { mode = 1; error = null },
                    label = { Text("Załóż konto") })
                androidx.compose.material3.FilterChip(selected = mode == 0, onClick = { mode = 0; error = null },
                    label = { Text("Logowanie") })
            }
            Spacer(Modifier.height(14.dp))

            GlassCard {
                Column(Modifier.padding(16.dp)) {
                    if (mode == 1) {
                        Text("Nowe normalne konto — zapisuje się lokalnie i już działa offline.",
                            fontSize = 11.sp, color = WatahaColors.Grey)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = displayName, onValueChange = { displayName = it },
                            label = { Text("Imię / pseudonim") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = phone, onValueChange = { phone = it },
                            label = { Text("Telefon (opcjonalnie)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                    }
                    OutlinedTextField(value = username, onValueChange = { username = it },
                        label = { Text("Nazwa użytkownika") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it },
                        label = { Text("Hasło (min. 4 znaki)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                    Spacer(Modifier.height(14.dp))
                    if (error != null) {
                        Text(error ?: "", color = WatahaColors.FlagRed, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                    }
                    GradientButton(
                        text = if (mode == 0) "ZALOGUJ SIĘ" else "ZAŁÓŻ KONTO WATAHY",
                        icon = "🐺",
                        height = 52.dp,
                        fontSize = 14,
                        modifier = Modifier.fillMaxWidth()
                    ) { submit() }
                    if (busy) {
                        Spacer(Modifier.height(8.dp))
                        CircularProgressIndicator(color = WatahaColors.FlagRed, strokeWidth = 2.dp, modifier = Modifier.width(24.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Konto demonstracyjne dla testerów: cwilk / haslo123",
                fontSize = 11.sp, color = WatahaColors.Grey,
                modifier = Modifier.clickable {
                    scope.launch {
                        repo.login("cwilk", "haslo123").fold(
                            onSuccess = { onLoggedIn() },
                            onFailure = { error = it.message }
                        )
                    }
                }
            )
            Spacer(Modifier.height(6.dp))
            Text("Dlaczego rejestracja? Konto jest Twoje: reputacja, odznaki, wymiany.\nDziała w 100% offline — nie potrzebujesz internetu, by zacząć.",
                fontSize = 10.sp, color = WatahaColors.Grey, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text("Polska Wataha V0.2 • Czarny Wilk — Strażnik Prawdy", fontSize = 9.sp, color = WatahaColors.Grey)
        }
    }
}


