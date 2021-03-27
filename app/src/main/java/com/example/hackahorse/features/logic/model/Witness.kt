package com.example.hackahorse.features.logic.model

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class Witness(
    @SerializedName("vote")
    val vote: BigInteger?,
    @SerializedName("nonce")
    val nonce: BigInteger?
)