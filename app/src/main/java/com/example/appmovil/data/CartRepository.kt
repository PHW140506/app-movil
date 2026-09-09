package com.example.appmovil.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApiService {
    @GET("carts")
    suspend fun getAllCarts(): List<CartResponseDto>

    @POST("carts")
    suspend fun addToCartApi(@Body request: CreateCartRequestDto): CartResponseDto

    // US10: Endpoints PUT y DELETE para el carrito
    @PUT("carts/{id}")
    suspend fun updateCartApi(
        @Path("id") id: Int,
        @Body request: CreateCartRequestDto
    ): CartResponseDto

    @DELETE("carts/{id}")
    suspend fun deleteCartApi(@Path("id") id: Int): CartResponseDto
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

    // US10: Escenario 1 - Actualizar cantidad localmente
    fun updateItemQuantity(productId: Int, newQuantity: Int) {
        val currentList = _localCartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.productId == productId }
        if (index != -1) {
            if (newQuantity <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = newQuantity)
            }
            _localCartItems.value = currentList
        }
    }

    // US10: Escenario 2 - Remover producto localmente
    fun removeLocalItem(productId: Int) {
        val currentList = _localCartItems.value.toMutableList()
        currentList.removeAll { it.productId == productId }
        _localCartItems.value = currentList
    }

    // US10: Consumo de endpoints remotos PUT y DELETE
    suspend fun updateCartApiCall(cartId: Int, userId: Int, items: List<LocalCartItem>): Result<CartResponseDto> = withContext(Dispatchers.IO) {
        try {
            val dtoList = items.map { AddCartProductDto(it.productId, it.quantity) }
            val request = CreateCartRequestDto(userId = userId, date = "2026-09-09", products = dtoList)
            val response = api.updateCartApi(cartId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCartApiCall(cartId: Int): Result<CartResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteCartApi(cartId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearLocalCart() {
        _localCartItems.value = emptyList()
    }
}