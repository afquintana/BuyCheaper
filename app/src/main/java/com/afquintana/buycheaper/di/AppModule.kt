package com.afquintana.buycheaper.di

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
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(impl: FirebaseAuthRepository): AuthRepository = impl

    @Provides
    @Singleton
    fun provideShoppingRepository(impl: FirestoreShoppingRepository): ShoppingRepository = impl

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/api/")
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideSupermarketApi(retrofit: Retrofit): SupermarketApi =
        retrofit.create(SupermarketApi::class.java)
}
