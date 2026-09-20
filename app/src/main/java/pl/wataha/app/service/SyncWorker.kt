package pl.wataha.app.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import pl.wataha.app.WatahaApp
import java.util.concurrent.TimeUnit

/**
 * Okresowa synchronizacja kolejki offline z backendem.
 * Zadanie działa tylko przy obecnym internecie — wtedy kolejka pendings jest wysyłana.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? WatahaApp ?: return Result.success()
        return try {
            app.repo.syncNow()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "wataha_sync", ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}
