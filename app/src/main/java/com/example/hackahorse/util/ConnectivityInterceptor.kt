package com.example.life365.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response

class ConnectivityInterceptor (
    val context: Context
) : Interceptor {
    companion object{
        fun isOnline(context: Context): Boolean {
            val cm =
                context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                        as ConnectivityManager


            if (Build.VERSION.SDK_INT < 23) {
                val ni = cm.activeNetworkInfo
                if (ni != null) {
                    return ni.isConnected && (ni.type == ConnectivityManager.TYPE_WIFI || ni.type == ConnectivityManager.TYPE_MOBILE)
                }
            } else {
                val n = cm.activeNetwork
                if (n != null) {
                    val nc = cm.getNetworkCapabilities(n)
                    return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc!!.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    )
                }
            }
            return false
        }

    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if(!isOnline(context.applicationContext)){
            throw NoConnectivityException()
        }
        return chain.proceed(chain.request())    }

}
