package com.example.appmovil.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProductApiService {
    @GET("products")
    suspend fun getProducts(): List<ProductCatalogDto>

    @GET("products/categories")
    suspend fun getCategories(): List<String>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): List<ProductCatalogDto>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductCatalogDto

    // US06: Creación de producto
    @POST("products")
    suspend fun addProduct(@Body product: CreateProductRequestDto): ProductCatalogDto
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
            Result.success(dtoList.mapToUiItems())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCategories(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val categories = api.getCategories()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProductsByCategory(category: String): Result<List<ProductCatalogItem>> = withContext(Dispatchers.IO) {
        try {
            val dtoList = api.getProductsByCategory(category)
            Result.success(dtoList.mapToUiItems())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProductDetail(productId: Int): Result<ProductCatalogDto> = withContext(Dispatchers.IO) {
        try {
            val dto = api.getProductById(productId)
            Result.success(dto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // US06: Envío del producto a la API
    suspend fun createProduct(product: CreateProductRequestDto): Result<ProductCatalogDto> = withContext(Dispatchers.IO) {
        try {
            val created = api.addProduct(product)
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun List<ProductCatalogDto>.mapToUiItems(): List<ProductCatalogItem> {
        return this.map { dto ->
            ProductCatalogItem(
                id = dto.id,
                title = dto.title.orEmpty().ifBlank { "Producto sin nombre" },
                price = dto.price,
                imageUrl = dto.image.orEmpty()
            )
        }
    }
}