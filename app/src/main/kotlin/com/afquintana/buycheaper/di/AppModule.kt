package com.afquintana.buycheaper.di

import android.util.Log
import com.afquintana.buycheaper.data.remote.SupermarketApi
import com.afquintana.buycheaper.data.repository.FirebaseAuthRepository
import com.afquintana.buycheaper.data.repository.FirestoreShoppingRepository
import com.afquintana.buycheaper.domain.repository.AuthRepository
import com.afquintana.buycheaper.domain.repository.ShoppingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import com.ihsanbal.logging.Level.BASIC
import com.ihsanbal.logging.LoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @JvmStatic
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    @JvmStatic
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    @JvmStatic
    fun provideAuthRepository(impl: FirebaseAuthRepository): AuthRepository = impl

    @Provides
    @Singleton
    @JvmStatic
    fun provideShoppingRepository(impl: FirestoreShoppingRepository): ShoppingRepository = impl

    @Provides
    @Singleton
    @JvmStatic
    fun provideRetrofit(): Retrofit {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(
                LoggingInterceptor.Builder()
                    .setLevel(BASIC)
                    .log(Log.VERBOSE)
                    .build()
            )

        return Retrofit.Builder()
            .baseUrl("https://example.com/api/")
            .client(okHttpClientBuilder.build())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideSupermarketApi(retrofit: Retrofit): SupermarketApi =
        retrofit.create(SupermarketApi::class.java)
}
