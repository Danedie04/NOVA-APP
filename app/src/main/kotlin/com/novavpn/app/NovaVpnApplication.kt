package com.novavpn.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * NovaVPN Application class.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation,
 * including a base class for the application that serves as the
 * application-level dependency container.
 */
@HiltAndroidApp
class NovaVpnApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide singletons or libraries here
    }
}
