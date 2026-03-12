package com.example.juegoaerolinea

import android.app.Application

class AirlineApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}
