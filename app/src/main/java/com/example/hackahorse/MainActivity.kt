package com.example.hackahorse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.hackahorse.databinding.ActivityMainBinding
import com.example.hackahorse.features.scanner.ScannerFragment
import kotlinx.android.synthetic.main.toolbar.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein

class MainActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        goToScanningFragment()
        //initToolbar()
    }


    private fun goToScanningFragment() {
        val initialFragment = ScannerFragment.getInstance()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, initialFragment)
            .commit()
    }
}