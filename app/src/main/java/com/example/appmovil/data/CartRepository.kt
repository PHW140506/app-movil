package com.example.appmovil.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface CartApiService {
    @GET("carts")
    suspend fun getCarts(): List<CartDto>

    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}

class CartRepository {

    private val api: CartApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://fakestoreapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CartApiService::class.java)
    }

    suspend fun fetchAuditedCarts(): Result<List<CartAuditItem>> = withContext(Dispatchers.IO) {
        try {
            val carts = api.getCarts()
            val productsMap = api.getProducts().associateBy { it.id }

            val auditedCarts = carts.map { cart ->
                val detailedItems = cart.products.map { item ->
                    CartProductDetail(
                        productId = item.productId,
                        title = productsMap[item.productId]?.title ?: "Producto #${item.productId}",
                        quantity = item.quantity
                    )
                }
                CartAuditItem(
                    id = cart.id,
                    userId = cart.userId,
                    date = cart.date.take(10),
                    products = detailedItems
                )
            }
            Result.success(auditedCarts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}