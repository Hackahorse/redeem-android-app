package org.tokend.muna.features.qr.view

import android.Manifest
import android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
import android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
import android.hardware.Camera.getNumberOfCameras
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hackahorse.R
import com.example.life365.util.ObservableTransformers
import com.example.life365.util.PermissionManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_scan_qr.*
import kotlinx.android.synthetic.main.toolbar_with_image.*
import org.tokend.muna.features.qr.model.NoCameraPermissionException
import org.tokend.sdk.utils.extentions.isBadRequest
import retrofit2.HttpException
import java.util.concurrent.TimeUnit


abstract class ScanQrFragment<ResultType : Any> : Fragment() {
    private val cameraPermission = PermissionManager(Manifest.permission.CAMERA, 404)
    private var hasCameraPermission: Boolean = false
    private var qrScanIsRequired: Boolean = false
    private var TAG = ScanQrFragment::class.java.name

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    protected val resultSubject = PublishSubject.create<ResultType>()
    val result: Observable<ResultType> = resultSubject


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan_qr, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initTitle()
        initQrScanner()
        initButtons()
        showPreSetErrorIfNeeded()

        cameraPermission.check(
            fragment = this,
            action = {
                hasCameraPermission = true
                resumeQrPreviewIfAllowed()
            },
            deniedAction = {
                hasCameraPermission = false
                resultSubject.onError(NoCameraPermissionException())
            }
        )
    }

    private fun initTitle() {
        title_text_view.text = getString(R.string.app_name)
    }

    private fun initQrScanner() {
        qrScanIsRequired = true
        qr_scanner_view.initializeFromIntent(
            IntentIntegrator.forSupportFragment(this)
                .setBeepEnabled(false)
                .setCameraId(CAMERA_FACING_BACK)
                .setDesiredBarcodeFormats(listOf(BarcodeFormat.QR_CODE.name))
                .createScanIntent()
        )
        qr_scanner_view.statusView.visibility = View.GONE
    }

    open fun initButtons() {
        var torchIsOn = false

        flash_switch_button.setOnClickListener {
            torchIsOn = !torchIsOn

            qr_scanner_view.barcodeView.setTorch(torchIsOn)

            if (torchIsOn) {
                flash_switch_button.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_flash_off)
                )
                flash_switch_button.contentDescription = getString(R.string.disable_flash_action)
            } else {
                flash_switch_button.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_flash_on)
                )
                flash_switch_button.contentDescription = getString(R.string.enable_flash_action)
            }
        }

        if (getNumberOfCameras() == 1) {
            camera_switch_button.visibility = View.INVISIBLE
        } else {
            camera_switch_button.setOnClickListener {
                val settings = qr_scanner_view.barcodeView.cameraSettings


                if (qr_scanner_view.barcodeView.isPreviewActive) {
                    qr_scanner_view.pause()
                }

                //swap the id of the camera to be used
                if (settings.requestedCameraId == CAMERA_FACING_BACK) {
                    settings.requestedCameraId = CAMERA_FACING_FRONT
                } else {
                    settings.requestedCameraId = CAMERA_FACING_BACK
                }
                qr_scanner_view.barcodeView.cameraSettings = settings

                qr_scanner_view.resume()
            }
        }


    }

    private fun showPreSetErrorIfNeeded() {

    }

    private fun resumeQrPreviewIfAllowed() {
        if (hasCameraPermission) {
            qr_scanner_view.resume()
        }
    }

    private fun resumeQrScanIfRequired() {
        if (qrScanIsRequired) {
            qr_scanner_view.decodeSingle(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult) {
                    handleQrCodeContent(result.text)
                }

                override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
            })
        }
    }

    protected open fun handleQrCodeContent(content: String) {
        Log.d(TAG, "handleQrCodeContent: SCAN_RESULT:\n$content")
        qrScanIsRequired = false

        getResult(content)
            .compose(ObservableTransformers.defaultSchedulersSingle())
         /*   .doOnSubscribe { loadingIndicator.show() }
            .doOnEvent { _, _ -> loadingIndicator.hide() }*/
            .subscribeBy(
                onSuccess = resultSubject::onNext,
                onError = this::showQrScanErrorAndRetry
            )
            .addTo(compositeDisposable)

    }

    private var hideQrScanErrorDisposable: Disposable? = null
    protected fun showQrScanErrorAndRetry(error: String) {
        hideQrScanErrorDisposable?.dispose()

        Log.d(TAG, "showQrScanErrorAndRetry: " + error)
        qr_scan_error_text_view.text = error
        if (qr_scan_error_text_view.visibility != View.VISIBLE) {
            //AnimationUtil.fadeInView(qr_scan_error_text_view)
        }

        val scheduleErrorFadeOut = {
            hideQrScanErrorDisposable =
                Observable.timer(ERROR_DURATION_MS, TimeUnit.MILLISECONDS)
                    .compose(ObservableTransformers.defaultSchedulers())
                    .subscribeBy(
                        onComplete = {
                            //AnimationUtil.fadeOutView(qr_scan_error_text_view)
                        }
                    )
                    .addTo(compositeDisposable)
        }

        Observable.timer(1, TimeUnit.SECONDS)
            .compose(ObservableTransformers.defaultSchedulers())
            .subscribeBy(
                onComplete = {
                    scheduleErrorFadeOut()
                    qrScanIsRequired = true
                    resumeQrScanIfRequired()
                }
            )
            .addTo(compositeDisposable)
    }

    protected open fun showQrScanErrorAndRetry(error: Throwable) {
        error.printStackTrace()
        Log.d(TAG, "showQrScanErrorAndRetry: " + error.javaClass.name)
        if (error is HttpException && error.isBadRequest()) {
            showQrScanErrorAndRetry("Wrong Qr Code")
        } else {
        }

    }

    fun resumeScanning() {
        qrScanIsRequired = true
        resumeQrScanIfRequired()
    }

    override fun onResume() {
        super.onResume()
        resumeQrPreviewIfAllowed()
        resumeScanning()
    }

    override fun onPause() {
        super.onPause()
        qr_scanner_view.pause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraPermission.handlePermissionResult(requestCode, permissions, grantResults)
    }

    /**
     * @return text displayed above the scanner viewfinder
     */
    abstract fun getTitle(): String?

    /**
     * @return result parsed from QR code content
     * or throws [Exception] with message that will be
     * displayed below the scanner viewfinder
     */
    abstract fun getResult(content: String): Single<out ResultType>

    companion object {
        private const val ERROR_DURATION_MS = 1500L
        private const val PRE_SET_ERROR_EXTRA = "pre_set_error"

        fun getBundle(preSetError: String?) = Bundle().apply {
            putString(PRE_SET_ERROR_EXTRA, preSetError)
        }
    }
}