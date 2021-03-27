package com.example.hackahorse.features.scanner.model

import com.google.gson.annotations.SerializedName

data class ScanQrModel(
    @SerializedName("transaction_hash")
    val transactionHash: String,
    @SerializedName("vote")
    val vote: String?,
    @SerializedName("nonce")
    val nonce: String?
)