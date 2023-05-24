package com.jorgetargz.projectseeker.framework

import android.app.Application
import com.jorgetargz.projectseeker.R
import dagger.hilt.android.HiltAndroidApp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import timber.log.Timber
import timber.log.Timber.DebugTree

@HiltAndroidApp
class ProjectSeeker : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = Config(
                backgroundSyncEnabled = true,
                userPresence = true,
                persistenceEnabled = true,
                uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING,
                useSequentialEventHandler = false,
            ),
            appContext = this,
        )
        ChatClient.Builder(getString(R.string.chat_client_api_key), this)
            .withPlugin(offlinePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()
    }
}