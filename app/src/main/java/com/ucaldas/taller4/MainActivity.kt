package com.ucaldas.taller4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PerfilScreen()
                }
            }
        }
    }
}

/**
 * Calcula las iniciales del nombre (Máximo 2 caracteres).
 * Bonus C: Avatar dinámico.
 */
private fun inicialesDe(nombre: String): String =
    nombre.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

@Composable
fun PerfilScreen() {
    // Parte 3: Estado que sobrevive a cambios de configuración (rotación)
    var publicaciones by rememberSaveable { mutableStateOf(0) }
    var nombre by rememberSaveable { mutableStateOf("Ana Gómez") }
    var editando by rememberSaveable { mutableStateOf(false) }
    var borrador by rememberSaveable { mutableStateOf("") }

    // Parte 2: Lista observable mediante mutableStateListOf para la recomposición
    val logros = remember { mutableStateListOf("Primer commit") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Permite desplazarse si la pantalla es pequeña/horizontal
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Parte 1.4: Avatar circular con iniciales dinámicas (Bonus C)
        // Orden estricto: .size() -> .clip() -> .background()
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = inicialesDe(nombre),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Parte 3.6: Validación de nombre no vacío
        Text(
            text = nombre.ifBlank { "Usuario sin nombre" },
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Ingeniería de Sistemas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Parte 1.6: Tarjeta de publicaciones interactiva (Parte 3.1)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Publicaciones", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$publicaciones",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bonus B: Deshabilitar botón cuando publicaciones <= 0
                    Button(
                        onClick = { if (publicaciones > 0) publicaciones-- },
                        enabled = publicaciones > 0
                    ) {
                        Text("-")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { publicaciones++ }) {
                        Text("+")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Formulario de edición
        Button(onClick = {
            borrador = nombre
            editando = !editando
        }) {
            Text(if (editando) "Cancelar" else "Editar perfil")
        }

        // Bonus A: Animación de aparición y desaparición suave
        AnimatedVisibility(
            visible = editando,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                OutlinedTextField(
                    value = borrador,
                    onValueChange = { borrador = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        nombre = borrador.ifBlank { "Usuario sin nombre" }
                        editando = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Parte 2: Sección de logros interactiva
        SeccionLogros(
            logros = logros,
            onAgregarLogro = {
                logros.add("Nuevo logro ${logros.size + 1}")
            }
        )
    }
}

@Composable
fun SeccionLogros(
    logros: List<String>,
    onAgregarLogro: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Logros", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        logros.forEach { logro ->
            Text(text = "• $logro", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = onAgregarLogro) {
            Text("Agregar logro")
        }
    }
}

@Preview(showBackground = true, name = "Perfil Preview")
@Composable
fun PerfilScreenPreview() {
    MaterialTheme {
        PerfilScreen()
    }
}