package com.medipath

import android.app.Application
import com.medipath.core.network.RetrofitInstance

class MediPathApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.initialize(applicationContext)
    }
}