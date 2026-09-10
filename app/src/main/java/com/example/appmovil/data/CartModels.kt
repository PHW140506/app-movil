package com.example.appmovil.data

import com.google.gson.annotations.SerializedName

// --- Modelos para la Auditoría de Carritos (US12) ---
data class CartProductDetail(
    val productId: Int,
    val title: String,
    val quantity: Int
)

data class CartAuditItem(
    val id: Int,
    val userId: Int,
    val date: String,
    val products: List<CartProductDetail>
)

// --- DTOs para Fake Store API (GET y POST /carts) ---
data class AddCartProductDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("quantity") val quantity: Int
)

data class CartResponseDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("userId") val userId: Int = 0,
    @SerializedName("date") val date: String = "",
    @SerializedName("products") val products: List<AddCartProductDto> = emptyList()
)

data class CreateCartRequestDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("products") val products: List<AddCartProductDto>
)

// --- Modelo para gestión de estado local del carrito personal (US09) ---
data class LocalCartItem(
    val productId: Int,
    val title: String,
    val price: Double,
    val imageUrl: String,
    var quantity: Int
)