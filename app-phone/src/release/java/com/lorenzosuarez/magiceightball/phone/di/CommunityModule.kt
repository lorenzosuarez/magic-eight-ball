package com.lorenzosuarez.magiceightball.phone.di

import com.example.magiceightball.core.domain.repository.CommunityCharacters
import com.example.magiceightball.core.domain.repository.NoCommunityCharacters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Other people's characters in the app as it is released: none yet, and their shelf says they are coming. */
@Module
@InstallIn(SingletonComponent::class)
object CommunityModule {

    @Provides
    @Singleton
    fun provideCommunityCharacters(): CommunityCharacters = NoCommunityCharacters
}
