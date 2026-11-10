package com.lorenzosuarez.magiceightball.phone.di

import com.lorenzosuarez.magiceightball.phone.speech.AndroidSpeechListener
import com.lorenzosuarez.magiceightball.phone.speech.SpeechListener
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechModule {
    @Binds
    @Singleton
    abstract fun bindSpeechListener(impl: AndroidSpeechListener): SpeechListener
}
