package com.example.appmovil.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CartApiService {
    @GET("carts")
    suspend fun getAllCarts(): List<CartResponseDto>

    @POST("carts")
    suspend fun addToCartApi(@Body request: CreateCartRequestDto): CartResponseDto
}

class CartRepository {

    private val api: CartApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://fakestoreapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CartApiService::class.java)
    }

    companion object {
        private val _localCartItems = MutableStateFlow<List<LocalCartItem>>(emptyList())
        val localCartItems: StateFlow<List<LocalCartItem>> = _localCartItems.asStateFlow()
    }

    // Función requerida por AuditCartsViewModel
    suspend fun fetchAuditedCarts(): Result<List<CartAuditItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllCarts()
            val auditedList = response.map { cartDto ->
                CartAuditItem(
                    id = cartDto.id,
                    userId = cartDto.userId,
                    date = cartDto.date,
                    products = cartDto.products.map { productDto ->
                        CartProductDetail(
                            productId = productDto.productId,
                            title = "Producto #${productDto.productId}",
                            quantity = productDto.quantity
                        )
                    }
                )
            }
            Result.success(auditedList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // US09: Consumo POST /carts
    suspend fun postCartToApi(userId: Int, productId: Int, quantity: Int): Result<CartResponseDto> = withContext(Dispatchers.IO) {
        try {
            val request = CreateCartRequestDto(
                userId = userId,
                date = "2026-09-09",
                products = listOf(AddCartProductDto(productId = productId, quantity = quantity))
            )
            val response = api.addToCartApi(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // US09: Manejo del estado local en memoria
    fun addOrUpdateLocalCart(productId: Int, title: String, price: Double, imageUrl: String, quantity: Int) {
        val currentList = _localCartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.productId == productId }

        if (existingIndex != -1) {
            val existingItem = currentList[existingIndex]
            currentList[existingIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentList.add(
                LocalCartItem(
                    productId = productId,
                    title = title,
                    price = price,
                    imageUrl = imageUrl,
                    quantity = quantity
                )
            )
        }
        _localCartItems.value = currentList
    }

    fun clearLocalCart() {
        _localCartItems.value = emptyList()
    }
}