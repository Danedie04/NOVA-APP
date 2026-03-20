package com.novavpn.app.di

import android.content.Context
import com.novavpn.app.vpn.VpnRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-scoped dependencies.
 *
 * Hilt automatically injects @ApplicationContext without this module,
 * but explicit @Provides methods here make the dependency graph clear
 * and testable.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideVpnRepository(
        @ApplicationContext context: Context
    ): VpnRepository = VpnRepository(context)
}
