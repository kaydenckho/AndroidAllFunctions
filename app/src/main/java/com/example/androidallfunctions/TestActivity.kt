package com.example.androidallfunctions

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.androidallfunctions.databinding.ActivityTestBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1

@AndroidEntryPoint
class TestActivity : AppCompatActivity(), OnDayNightStateChanged {

    private val binding by lazy { ActivityTestBinding.inflate(layoutInflater) }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//
//
//        binding.testBtn.setOnClickListener {
//
////            val key = AES.get().genKey()
////            val cipher = AES.get().encrypt("Hello world", key)
////            binding.testTv.text = cipher.first
////            binding.testTv2.text = AES.get().sha256("Hello world")
//
//        }
//    }


    private var defaultFlag = 1
    var callback: OnDayNightStateChanged? = null
    var abc: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        defaultFlag = window.decorView.systemUiVisibility

        binding.testBtn.setOnClickListener {


            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, ShortVideoFragment(), ShortVideoFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        exitProcess(0)
    }

    enum class NetworkStatus {
        WIFI, MOBILE_DATA, DISCONNECTED
    }

    private fun checkNetworkStatus(context: Context): NetworkStatus {
        var result: NetworkStatus = NetworkStatus.DISCONNECTED
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities =
                connectivityManager.activeNetwork ?: return NetworkStatus.DISCONNECTED
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities)
                    ?: return NetworkStatus.DISCONNECTED
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.MOBILE_DATA
                else -> NetworkStatus.DISCONNECTED
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> NetworkStatus.WIFI
                        ConnectivityManager.TYPE_MOBILE -> NetworkStatus.MOBILE_DATA
                        else -> NetworkStatus.DISCONNECTED
                    }
                }
            }
        }
        return result
    }


    override fun onDayNightApplied(state: Int) {
        if (state == OnDayNightStateChanged.DAY){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}