package com.example.appmovil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmovil.data.LoginRequest
import com.example.appmovil.data.NetworkUtils
import com.example.appmovil.data.RetrofitClient
import com.example.appmovil.data.SessionManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successRole by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Iniciar Sesión",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo de Usuario
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = null
            },
            label = { Text("Nombre de usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Escenario 2 y 3: Alerta en rojo en caso de error o falta de internet
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFC62828),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Escenario 1: Confirmación de sesión y rol asignado
        if (successRole != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Sesión iniciada con éxito.\nRol: $successRole",
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botón de Iniciar Sesión
        Button(
            onClick = {
                // Escenario 3: Validar internet antes de consumir la API
                if (!NetworkUtils.hasInternetConnection(context)) {
                    errorMessage = "Sin conexión a internet. Verifique su red antes de continuar."
                    return@Button
                }

                if (username.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor ingrese usuario y contraseña."
                    return@Button
                }

                isLoading = true
                errorMessage = null

                coroutineScope.launch {
                    try {
                        val response = RetrofitClient.apiService.login(
                            LoginRequest(username.trim(), password.trim())
                        )

                        if (response.isSuccessful && response.body()?.token != null) {
                            val token = response.body()!!.token!!

                            // Para Fake Store API asignamos el ID 1 por defecto al usuario 'johnd' para probar Admin,
                            // o mapeamos según la lógica de negocio indicada
                            val simulatedUserId = if (username.trim() == "johnd") 1 else 3

                            // Escenario 1: Guardar sesión cifrada y mapear rol
                            sessionManager.saveSession(token, simulatedUserId)
                            successRole = sessionManager.getUserRole().name
                        } else {
                            // Escenario 2: Capturar error 401 o credenciales incorrectas
                            errorMessage = "Usuario o contraseña inválidos"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error de comunicación con el servidor: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar Sesión", fontSize = 16.sp)
            }
        }
    }
}