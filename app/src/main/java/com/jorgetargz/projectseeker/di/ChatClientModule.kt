package com.jorgetargz.projectseeker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.getstream.chat.android.client.ChatClient

@Module
@InstallIn(SingletonComponent::class)
object ChatClientModule {

    @Provides
    fun provideChatClient(): ChatClient {
        return ChatClient.instance()
    }
}