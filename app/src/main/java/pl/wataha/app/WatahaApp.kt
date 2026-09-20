package pl.wataha.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pl.wataha.app.comm.CommManager
import pl.wataha.app.data.db.AppDatabase
import pl.wataha.app.data.repo.Repository
import pl.wataha.app.service.NotificationHelper
import pl.wataha.app.service.SyncWorker

class WatahaApp : Application() {

    lateinit var db: AppDatabase
        private set
    lateinit var comm: CommManager
        private set
    lateinit var repo: Repository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.init(this)
        db = AppDatabase.get(this)
        comm = CommManager(this)
        comm.installNetworkCallback()
        repo = Repository(this, db.dao(), comm)
        appScope.launch {
            repo.init()
        }
        SyncWorker.schedule(this)
    }

    companion object {
        fun of(app: Application): WatahaApp = app as WatahaApp
    }
}
