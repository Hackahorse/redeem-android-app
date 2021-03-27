package com.example.hackahorse.features.logic.model

import com.google.gson.annotations.SerializedName
import java.security.spec.ECPoint

data class Commitment(
    @SerializedName("commitment")
    val commitment: ECPoint
)