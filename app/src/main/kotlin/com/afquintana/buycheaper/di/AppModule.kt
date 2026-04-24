package com.afquintana.buycheaper.di

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
}
