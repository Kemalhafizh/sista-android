package com.sultanagung1.sista.di

import android.content.Context
import com.sultanagung1.sista.core.accessibility.FontScaleManager
import com.sultanagung1.sista.core.accessibility.LanguageManager
import com.sultanagung1.sista.core.accessibility.ThemeManager
import com.sultanagung1.sista.core.audio.AudioRecorderManager
import com.sultanagung1.sista.core.document.DownloadManager
import com.sultanagung1.sista.core.feature.FeatureFlagManager
import com.sultanagung1.sista.core.lite.LiteModeManager
import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.notification.NotificationChannelManager
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.sync.AppLifecycleSyncObserver
import com.sultanagung1.sista.core.sync.NetworkConnectivityObserver
import com.sultanagung1.sista.core.sync.OfflineActionQueue
import com.sultanagung1.sista.core.sync.SyncManager
import com.sultanagung1.sista.core.time.ServerTimeProvider
import com.sultanagung1.sista.core.update.InAppUpdateManager
import com.sultanagung1.sista.core.websocket.ReverbWebSocketManager
import com.sultanagung1.sista.data.local.SulaoneLocalStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideSulaoneLocalStore(@ApplicationContext context: Context): SulaoneLocalStore {
        return SulaoneLocalStore.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideLanguageManager(sessionManager: SessionManager): LanguageManager {
        return LanguageManager(sessionManager)
    }

    @Provides
    @Singleton
    fun provideFontScaleManager(sessionManager: SessionManager): FontScaleManager {
        return FontScaleManager(sessionManager)
    }

    @Provides
    @Singleton
    fun provideThemeManager(sessionManager: SessionManager): ThemeManager {
        return ThemeManager(sessionManager)
    }

    @Provides
    @Singleton
    fun provideWebSocketManager(sessionManager: SessionManager): ReverbWebSocketManager {
        return ReverbWebSocketManager(sessionManager)
    }

    @Provides
    @Singleton
    fun provideNotificationChannelManager(@ApplicationContext context: Context): NotificationChannelManager {
        return NotificationChannelManager(context)
    }

    @Provides
    @Singleton
    fun provideAudioRecorderManager(@ApplicationContext context: Context): AudioRecorderManager {
        return AudioRecorderManager(context)
    }

    @Provides
    @Singleton
    fun provideDownloadManager(@ApplicationContext context: Context): DownloadManager {
        return DownloadManager(context)
    }

    @Provides
    @Singleton
    fun provideOfflineActionQueue(
        localStore: SulaoneLocalStore,
        apiClient: ApiClient
    ): OfflineActionQueue {
        return OfflineActionQueue(localStore, apiClient)
    }

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(@ApplicationContext context: Context): NetworkConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        apiClient: ApiClient,
        localStore: SulaoneLocalStore,
        actionQueue: OfflineActionQueue,
        connectivityObserver: NetworkConnectivityObserver
    ): SyncManager {
        return SyncManager(context, apiClient, localStore, actionQueue, connectivityObserver)
    }

    @Provides
    @Singleton
    fun provideAppLifecycleSyncObserver(
        syncManager: SyncManager
    ): AppLifecycleSyncObserver {
        return AppLifecycleSyncObserver(syncManager)
    }

    @Provides
    @Singleton
    fun provideFeatureFlagManager(
        @ApplicationContext context: Context,
        apiClient: ApiClient,
        sessionManager: SessionManager
    ): FeatureFlagManager {
        return FeatureFlagManager(context, apiClient, sessionManager)
    }

    @Provides
    @Singleton
    fun provideInAppUpdateManager(@ApplicationContext context: Context, apiClient: ApiClient): InAppUpdateManager {
        return InAppUpdateManager(context, apiClient)
    }

    @Provides
    @Singleton
    fun provideServerTimeProvider(@ApplicationContext context: Context, apiClient: ApiClient): ServerTimeProvider {
        return ServerTimeProvider(context, apiClient)
    }

    @Provides
    @Singleton
    fun provideLiteModeManager(@ApplicationContext context: Context): LiteModeManager {
        return LiteModeManager(context)
    }
}
