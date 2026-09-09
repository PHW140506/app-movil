package com.example.appmovil.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ProductApiService {
    @GET("products")
    suspend fun getProducts(): List<ProductCatalogDto>
}

class ProductRepository {

    private val api: ProductApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://fakestoreapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductApiService::class.java)
    }

    suspend fun fetchCatalog(): Result<List<ProductCatalogItem>> = withContext(Dispatchers.IO) {
        try {
            val dtoList = api.getProducts()
            val items = dtoList.map { dto ->
                ProductCatalogItem(
                    id = dto.id,
                    title = dto.title.orEmpty().ifBlank { "Producto sin nombre" },
                    price = dto.price,
                    imageUrl = dto.image.orEmpty()
                )
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}