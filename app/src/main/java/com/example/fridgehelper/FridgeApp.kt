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
import java.util.Calendar
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
        //powiadomienie przy starcie aplikacji
        createNotificationChannel()
        //codzienne sprawdzanie dat ważności
        scheduleExpiryCheck(this, UserPreferences.DEFAULT_THRESHOLD)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "expiry_channel",
            "Expiry dates",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for products nearing expiry"
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    companion object {
        fun scheduleExpiryCheck(
            context: Context,
            daysThreshold: Int,
            notifyHour: Int   = UserPreferences.DEFAULT_NOTIFY_HOUR,
            notifyMinute: Int = UserPreferences.DEFAULT_NOTIFY_MINUTE
        ) {
            // próg dni do workera jako dane wejściowe
            val inputData = workDataOf(
                ExpiryCheckWorker.KEY_DAYS_THRESHOLD to daysThreshold
            )

            //zadanie (powiadomienia) powtarzaja sie co 24h z opóźnieniem do wybranej godziny
            val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setInitialDelay(delayUntil(notifyHour, notifyMinute), TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        // nie uruchamia się gdy bateria jest niska
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            // REPLACE zastępuje poprzednie zaplanowane zadanie
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "expiry_check",
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }

        // oblicza ile ms zostało do najbliższego wystąpienia danej godziny
        private fun delayUntil(hour: Int, minute: Int): Long {
            val now    = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (target.timeInMillis <= now.timeInMillis) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}