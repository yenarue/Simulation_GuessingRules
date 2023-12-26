package yenarue.simulation.ria

import android.app.Application

class RiaApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Preferences.init(this)
    }
}