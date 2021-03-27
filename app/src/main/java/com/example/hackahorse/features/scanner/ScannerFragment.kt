package com.example.hackahorse.features.scanner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.hackahorse.MainActivity
import com.example.hackahorse.R
import com.example.hackahorse.databinding.FragmentCheckUserBinding
import com.example.life365.util.ObservableTransformers
import com.example.life365.util.UserFlowFragmentDisplayer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar_with_image.*
import org.tokend.muna.features.qr.view.ScanQrFragment


class ScannerFragment : Fragment() {
    private val fragmentDisplayer =
        UserFlowFragmentDisplayer(this, R.id.fragment_container)

    private val scanCompositeDisposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }


    private val compositeDisposable = CompositeDisposable()

    val isLoading = MutableLiveData(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentCheckUserBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_check_user, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toScan()
        //initToolbar()
    }


    private fun toScan() {
        val fragment = ScanRedeemableFragment().apply {
            arguments = ScanQrFragment.getBundle(null)

        }

        subscribeToScanResult(fragment)
        fragmentDisplayer.display(fragment, "scan", null)

    }

    private fun subscribeToScanResult(scanRedeemableFragment: ScanRedeemableFragment) {
        scanCompositeDisposable.clear()
        scanRedeemableFragment
            .result
            .compose(ObservableTransformers.defaultSchedulers())
            .subscribeBy(
                onNext = this::checkScannerResultNavigation,
                onError = {}
            )
            .addTo(scanCompositeDisposable)
    }

    private fun checkScannerResultNavigation(triple: Triple<String, String, String?>) {
        confirmRedeemableAndStartScan(triple.second, triple.third)
    }

    private fun confirmRedeemableAndStartScan(
        userAccountId: String,
        scanpatientModel: String? //model
    ) {
        Log.d("QR_RESULT", userAccountId)

    }

    override fun onDetach() {
        scanCompositeDisposable.dispose()
        super.onDetach()
    }

    companion   object {
        private val CONFIRM_REDEMPTION_REQUEST = "confirm_redemption".hashCode() and 0xfff
        private val VIEW_BOOKING_DETAILS_REQUEST = "view_booking".hashCode() and 0xfff

        fun getInstance(): ScannerFragment =
            ScannerFragment()
    }
}