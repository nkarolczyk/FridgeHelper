package com.example.fridgehelper.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fridgehelper.R
import com.example.fridgehelper.data.repository.FridgeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// logika powiadomień
@HiltWorker
class ExpiryCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FridgeRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val daysThreshold = inputData.getInt(KEY_DAYS_THRESHOLD, 3)

        val expiring = repository.getExpiringSoon(daysThreshold)

        expiring.forEach { product ->
            val daysLeft = ((product.expiryDate - System.currentTimeMillis()) /
                    (1000 * 60 * 60 * 24)).toInt()

            val message = when {
                daysLeft <= 0 -> "${product.name} — już przeterminowany!"
                daysLeft == 1 -> "${product.name} — został 1 dzień!"
                else          -> "${product.name} — zostało $daysLeft dni"
            }

            sendNotification(product.id, message)
        }

        return Result.success()
    }

    private fun sendNotification(productId: Int, message: String) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Sprawdź lodówkę!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(productId, notification)
    }

    companion object {
        const val CHANNEL_ID = "expiry_channel"
        const val KEY_DAYS_THRESHOLD = "days_threshold"
    }
}