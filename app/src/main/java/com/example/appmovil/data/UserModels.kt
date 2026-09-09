package com.example.appmovil.data

import com.google.gson.annotations.SerializedName

data class NameDto(
    @SerializedName("firstname") val firstname: String? = "",
    @SerializedName("lastname") val lastname: String? = ""
)

data class GeolocationDto(
    @SerializedName("lat") val lat: String? = "",
    @SerializedName("long") val long: String? = ""
)

data class AddressDto(
    @SerializedName("city") val city: String? = "",
    @SerializedName("street") val street: String? = "",
    @SerializedName("number") val number: Int? = 0,
    @SerializedName("zipcode") val zipcode: String? = "",
    @SerializedName("geolocation") val geolocation: GeolocationDto? = null
)

data class AuditedUserDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("email") val email: String? = "",
    @SerializedName("username") val username: String? = "",
    @SerializedName("phone") val phone: String? = "",
    @SerializedName("name") val name: NameDto? = null,
    @SerializedName("address") val address: AddressDto? = null
)

data class UserUiModel(
    val id: Int,
    val fullName: String,
    val username: String,
    val email: String,
    val phone: String,
    val city: String
)