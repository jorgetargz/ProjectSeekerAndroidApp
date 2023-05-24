package com.jorgetargz.projectseeker.framework

import android.app.Application
import com.jorgetargz.projectseeker.network.commom.Config
import dagger.hilt.android.HiltAndroidApp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import timber.log.Timber
import timber.log.Timber.DebugTree

@HiltAndroidApp
class ProjectSeeker : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = io.getstream.chat.android.offline.plugin.configuration.Config(
                backgroundSyncEnabled = true,
                userPresence = true,
                persistenceEnabled = true,
                uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING,
                useSequentialEventHandler = false,
            ),
            appContext = this,
        )
        val key = Config.CHAT_CLIENT_API_KEY
        ChatClient.Builder(key, this)
            .withPlugin(offlinePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()
    }
}