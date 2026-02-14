package com.example.finalproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.finalproject.data.model.UserEntity
import com.example.finalproject.data.model.CourseEntity
import com.example.finalproject.data.repository.CourseDao
import com.example.finalproject.data.repository.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [UserEntity::class, CourseEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "final_project_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            // יצירת משתמש ADMIN ראשוני
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = INSTANCE ?: return@launch
                                val dao = database.userDao()

                                val adminEmail = "colmanBB@gmail.com"
                                val adminPass = "123456"

                                val existing = dao.getByEmail(adminEmail)

                                if (existing == null) {
                                    dao.insert(
                                        UserEntity(
                                            email = adminEmail,
                                            passwordHash = sha256(adminPass),
                                            role = "ADMIN"
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private fun sha256(password: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(password.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
