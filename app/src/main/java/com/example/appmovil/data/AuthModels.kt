package com.example.appmovil.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Petición que enviamos a la Fake Store API
data class LoginRequest(
    val username: String,
    val password: String
)

// Respuesta que nos entrega la API al iniciar sesión
data class LoginResponse(
    val token: String?
)

// Definición del endpoint de autenticación
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}

// Instancia única de Retrofit
object RetrofitClient {
    private const val BASE_URL = "https://fakestoreapi.com/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}