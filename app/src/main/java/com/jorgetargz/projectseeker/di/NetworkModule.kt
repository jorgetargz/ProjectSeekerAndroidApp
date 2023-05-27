package com.jorgetargz.projectseeker.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jorgetargz.projectseeker.network.ProjectServices
import com.jorgetargz.projectseeker.network.SessionService
import com.jorgetargz.projectseeker.network.UsersServices
import com.jorgetargz.projectseeker.network.adapters.LocalDateTypeAdapter
import com.jorgetargz.projectseeker.network.commom.Config
import com.jorgetargz.projectseeker.network.interceptors.SpringBootAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
        .setLenient()
        .create()

    @Provides
    @Named(Config.RETROFIT_SPRING_BOOT)
    fun provideRetrofitSpringBoot(
        springBootAuthInterceptor: SpringBootAuthInterceptor,
        gson: Gson
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(springBootAuthInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl(Config.BASE_URL_SPRING_BOOT)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Provides
    fun provideSessionService(
        @Named(Config.RETROFIT_SPRING_BOOT) retrofit: Retrofit
    ): SessionService {
        return retrofit.create(SessionService::class.java)
    }

    @Provides
    fun provideUsersService(
        @Named(Config.RETROFIT_SPRING_BOOT) retrofit: Retrofit
    ): UsersServices {
        return retrofit.create(UsersServices::class.java)
    }

    @Provides
    fun provideProjectsService(
        @Named(Config.RETROFIT_SPRING_BOOT) retrofit: Retrofit
    ): ProjectServices {
        return retrofit.create(ProjectServices::class.java)
    }
}