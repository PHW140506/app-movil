package com.example.appmovil.data

import com.google.gson.annotations.SerializedName

data class ProductCatalogDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val title: String? = "",
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("description") val description: String? = "",
    @SerializedName("category") val category: String? = "",
    @SerializedName("image") val image: String? = ""
)

data class ProductCatalogItem(
    val id: Int,
    val title: String,
    val price: Double,
    val imageUrl: String
)

data class CreateProductRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("price") val price: Double,
    @SerializedName("description") val description: String,
    @SerializedName("image") val image: String,
    @SerializedName("category") val category: String
)