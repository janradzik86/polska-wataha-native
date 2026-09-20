package pl.wataha.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.wataha.app.data.model.PostType
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.ui.components.WatahaTopBar
import pl.wataha.app.ui.theme.WatahaColors
import java.io.File

@Composable
fun CreatePostScreen(
    repo: Repository,
    initialType: PostType? = null,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onRequestLocation: () -> Unit
) {
    var type by remember { mutableStateOf(initialType ?: PostType.GIVE) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var useMyLocation by remember { mutableStateOf(true) }
    var latText by remember { mutableStateOf(repo.myLocation().first.toString()) }
    var lngText by remember { mutableStateOf(repo.myLocation().second.toString()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val dir = File(context.filesDir, "images").apply { mkdirs() }
                    val f = File(dir, "img_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        f.outputStream().use { output -> input.copyTo(output) }
                    }
                    photoPath = f.absolutePath
                } catch (e: Exception) {
                    error = "Nie udało się zapisać zdjęcia: ${e.message}"
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().background(WatahaColors.White)) {
        WatahaTopBar("Nowe ogłoszenie", "Oddaj • pomóż • poproś", onBack = onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text("JAKIEGO TYPU JEST TO OGŁOSZENIE?", style = MaterialTheme.typography.labelMedium, color = WatahaColors.Grey)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PostType.entries.forEach { t ->
                    FilterChip(selected = type == t, onClick = { type = t },
                        label = { Text("${t.emoji} ${t.label}") })
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(value = title, onValueChange = { title = it },
                label = { Text("Tytuł") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = body, onValueChange = { body = it },
                label = { Text("Opis / szczegóły") }, minLines = 4, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text("🖼️ Wybierz zdjęcie")
                }
                if (photoPath != null) {
                    OutlinedButton(onClick = { photoPath = null }) { Text("Usuń 🗑️") }
                }
            }
            if (photoPath != null) {
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = File(photoPath!!), contentDescription = "Zdjęcie ogłoszenia",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(180.dp).background(WatahaColors.LightGrey, RoundedCornerShape(12.dp))
                )
            }
            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📍 Użyj mojej lokalizacji", Modifier.weight(1f))
                Switch(checked = useMyLocation, onCheckedChange = { useMyLocation = it })
            }
            if (!useMyLocation) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = latText, onValueChange = { latText = it },
                        label = { Text("Szerokość") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = lngText, onValueChange = { lngText = it },
                        label = { Text("Długość") }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = onRequestLocation) { Text("🎯 Pobierz GPS (uprawnienie)") }
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error ?: "", color = WatahaColors.FlagRed, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isBlank()) { error = "Podaj tytuł."; return@Button }
                    val lat = (if (useMyLocation) repo.myLocation().first else latText.toDoubleOrNull())
                    val lng = (if (useMyLocation) repo.myLocation().second else lngText.toDoubleOrNull())
                    if (lat == null || lng == null) { error = "Nieprawidłowa lokalizacja."; return@Button }
                    scope.launch {
                        repo.createPost(type, title.trim(), body.trim(), photoPath ?: "", lat, lng)
                        onDone()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WatahaColors.FlagRed),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("PUBLIKUJ OGŁOSZENIE", fontSize = 15.sp, color = WatahaColors.White)
            }
            Spacer(Modifier.height(8.dp))
            Text("Offline-first: bez internetu ogłoszenie trafi do lokalnej bazy i kolejki synchronizacji.",
                fontSize = 11.sp, color = WatahaColors.Grey)
        }
    }
}
