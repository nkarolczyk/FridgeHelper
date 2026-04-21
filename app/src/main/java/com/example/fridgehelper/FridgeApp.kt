package com.example.fridgehelper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.fridgehelper.data.UserPreferences
import com.example.fridgehelper.worker.ExpiryCheckWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class FridgeApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleExpiryCheck(this, UserPreferences.DEFAULT_THRESHOLD)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "expiry_channel",
            "Terminy ważności",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Przypomnienia o kończących się produktach"
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    companion object {
        fun scheduleExpiryCheck(context: Context, daysThreshold: Int) {
            val inputData = workDataOf(
                ExpiryCheckWorker.KEY_DAYS_THRESHOLD to daysThreshold
            )

            val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "expiry_check",
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }
    }
}