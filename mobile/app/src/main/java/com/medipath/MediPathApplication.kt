package com.medipath

import android.app.Application
import com.medipath.core.network.RetrofitInstance

class MediPathApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.initialize(applicationContext)
    }
}
//
//package com.medipath
//
//import android.app.Application
//import com.medipath.core.network.RetrofitInstance
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.launch
//
//class MediPathApplication : Application() {
//    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
//
//    override fun onCreate() {
//        super.onCreate()
//
//        applicationScope.launch {
//            try {
//                RetrofitInstance.initialize(applicationContext)
//            } catch (e: Exception) {
//                android.util.Log.e("MediPathApp", "Retrofit init failed", e)
//            }
//        }
//    }
//}