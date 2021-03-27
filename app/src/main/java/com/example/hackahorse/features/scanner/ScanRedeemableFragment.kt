package com.example.hackahorse.features.scanner

import android.util.Log
import android.util.LruCache
import com.example.hackahorse.features.scanner.model.ScanQrModel
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import org.tokend.muna.features.qr.view.ScanQrFragment
import org.tokend.sdk.utils.extentions.isBadRequest
import org.tokend.sdk.utils.extentions.isNotFound
import retrofit2.HttpException
import java.util.concurrent.TimeUnit


class ScanRedeemableFragment : ScanQrFragment<Triple<String, String, String>>() {
    private var scannedContent = ""
    private val invalidRequests = LruCache<String, Throwable>(10)

    override fun getTitle(): String? = null

    override fun initButtons() {
        super.initButtons()
    }

    override fun getResult(content: String): Single<out Triple<String, String, String>> {
        val visualTimeout = Completable.timer(500, TimeUnit.MILLISECONDS)

        try {
            val qrScanModel = Gson().fromJson(content, ScanQrModel::class.java)
            if (qrScanModel.transactionHash == null && qrScanModel.vote == null && qrScanModel.nonce == null) {
                throw Exception()
            }

            qrScanModel.transactionHash


        } catch (e: Exception) {
            return Single.error(e)
        }
        return Single.just(Triple("", "", ""))

    }


    override fun handleQrCodeContent(content: String) {
        scannedContent = content

        val knownError = invalidRequests[content]
        if (knownError != null) {
            showQrScanErrorAndRetry(knownError)
        } else {
            super.handleQrCodeContent(content)
        }
    }

    override fun showQrScanErrorAndRetry(error: Throwable) {
        error.printStackTrace()
        Log.d("TAG", "showQrScanErrorAndRetry: " + error.javaClass.name)
        val message = error.localizedMessage

        if (error is HttpException && error.isBadRequest() || error is NoSuchElementException) {
            showQrScanErrorAndRetry("Wrong Qr Code")
        } else {
            if (error is HttpException && error.isNotFound()) {
                showQrScanErrorAndRetry("No data found")
            } else if (error is IllegalArgumentException && message?.contains("Resource must have a non null and non-empty 'id'") == true) {
                showQrScanErrorAndRetry("This user was deleted")
            } else {
                if (message != null) {
                    invalidRequests.put(scannedContent, error)
                    showQrScanErrorAndRetry(message)
                } else {
                    super.showQrScanErrorAndRetry(error)
                }
            }
        }
    }

}