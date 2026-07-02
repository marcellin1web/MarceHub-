package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.model.*
import com.example.data.dao.*

@Database(
    entities = [
        UserEntity::class,
        FollowEntity::class,
        PostEntity::class,
        PostLikeEntity::class,
        CommentEntity::class,
        MessageEntity::class,
        AnnouncementEntity::class,
        FaqEntity::class,
        ContactMessageEntity::class,
        SystemSettingEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun followDao(): FollowDao
    abstract fun postDao(): PostDao
    abstract fun postLikeDao(): PostLikeDao
    abstract fun commentDao(): CommentDao
    abstract fun messageDao(): MessageDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun faqDao(): FaqDao
    abstract fun contactMessageDao(): ContactMessageDao
    abstract fun systemSettingDao(): SystemSettingDao
    abstract fun activityLogDao(): ActivityLogDao
}
