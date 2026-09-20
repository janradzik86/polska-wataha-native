package pl.wataha.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.wataha.app.data.model.BadgeEntity
import pl.wataha.app.data.model.CrisisSignalEntity
import pl.wataha.app.data.model.ExchangeEntity
import pl.wataha.app.data.model.FeedbackEntity
import pl.wataha.app.data.model.GeoMarkerEntity
import pl.wataha.app.data.model.MessageEntity
import pl.wataha.app.data.model.NetworkNodeEntity
import pl.wataha.app.data.model.PendingSyncEntity
import pl.wataha.app.data.model.PostEntity
import pl.wataha.app.data.model.ThreadEntity
import pl.wataha.app.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        MessageEntity::class,
        ThreadEntity::class,
        ExchangeEntity::class,
        CrisisSignalEntity::class,
        BadgeEntity::class,
        NetworkNodeEntity::class,
        GeoMarkerEntity::class,
        PendingSyncEntity::class,
        FeedbackEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "polska_wataha.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
