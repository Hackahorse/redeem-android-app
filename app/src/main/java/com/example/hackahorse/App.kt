package com.example.hackahorse

import android.app.Application
import com.example.life365.util.ConnectivityInterceptor
import com.example.life365.util.ToastManager
import com.jakewharton.threetenabp.AndroidThreeTen
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class App : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@App))

        //utils
        bind() from provider { ToastManager(instance()) }

        //network
        bind<ConnectivityInterceptor>() with singleton { ConnectivityInterceptor(instance()) }

    }

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

    }
}