package se.infomaker.googleanalytics.di

import dagger.assisted.AssistedFactory
import se.infomaker.googleanalytics.Tracker

@AssistedFactory
interface TrackerFactory {
    fun create(trackingId: String): Tracker
}