package com.example.appmovil.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface UserApiService {
    @GET("users")
    suspend fun getUsers(): List<AuditedUserDto>
}

class UserRepository {

    private val api: UserApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://fakestoreapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApiService::class.java)
    }

    suspend fun fetchUsers(): Result<List<UserUiModel>> = withContext(Dispatchers.IO) {
        try {
            val dtoList = api.getUsers()
            val uiModels = dtoList.map { dto ->
                val first = dto.name?.firstname.orEmpty().replaceFirstChar { it.uppercase() }
                val last = dto.name?.lastname.orEmpty().replaceFirstChar { it.uppercase() }
                val fullName = if (first.isBlank() && last.isBlank()) "Sin nombre" else "$first $last"

                UserUiModel(
                    id = dto.id,
                    fullName = fullName,
                    username = dto.username.orEmpty().ifBlank { "N/A" },
                    email = dto.email.orEmpty().ifBlank { "Sin correo" },
                    phone = dto.phone.orEmpty().ifBlank { "Sin teléfono" },
                    city = dto.address?.city.orEmpty().replaceFirstChar { it.uppercase() }.ifBlank { "No registrada" }
                )
            }
            Result.success(uiModels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}