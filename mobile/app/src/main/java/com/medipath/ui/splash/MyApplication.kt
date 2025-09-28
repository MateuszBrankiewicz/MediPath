package com.medipath.ui.splash

import android.app.Application
import com.medipath.data.api.RetrofitInstance

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //inicjalizacja retrofit instance na poczatku aplikacji, session manager i cookie jar są dostępne globalnie
        RetrofitInstance.initialize(applicationContext)
    }
}