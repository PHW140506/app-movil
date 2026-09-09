package com.example.appmovil.data

import com.google.gson.annotations.SerializedName

// Modelos directos de la API
data class CartItemDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("quantity") val quantity: Int
)

data class CartDto(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("products") val products: List<CartItemDto>
)

data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("price") val price: Double
)

// Modelos limpios para la interfaz de Compose
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