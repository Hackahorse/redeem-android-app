package com.example.hackahorse.features.logic

import com.example.hackahorse.features.logic.model.Commitment
import com.example.hackahorse.features.logic.model.Witness
import java.security.spec.ECPoint

class VerifyCommitmentUseCase(private val commitment: Commitment, private val witness: Witness) {
    fun verifyCommitment(): Boolean {
        val point: ECPoint = EC.Points.addPoint(
            EC.Points.scalmult(EC.Constants.G, witness.nonce),
            EC.Points.scalmult(EC.Constants.H, witness.nonce)
        )
        return point.affineX
                .equals(commitment.commitment.affineX) && point.affineY
                .equals(commitment.commitment.affineY)
    }
}