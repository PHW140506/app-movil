package com.example.appmovil.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Petición al endpoint /auth/login
data class LoginRequest(
    val username: String,
    val password: String
)

// Respuesta al autenticar
data class LoginResponse(
    val token: String?
)

// Información del usuario obtenida de la API
data class UserDto(
    val id: Int,
    val username: String,
    val email: String?
)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Obtener información de los usuarios para mapear el ID real
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>
}

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