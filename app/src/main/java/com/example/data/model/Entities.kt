package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val username: String,
    val bio: String,
    val country: String,
    val role: String, // "Admin", "Moderator", "User"
    val isBanned: Boolean = false,
    val isSuspended: Boolean = false,
    val profilePic: String, // name of default avatar drawable / image uri
    val coverImage: String = "gradient_marce", // name of cover theme
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "follows", primaryKeys = ["followerId", "followingId"])
data class FollowEntity(
    val followerId: String,
    val followingId: String
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorProfilePic: String,
    val content: String,
    val mediaUrl: String? = null,
    val mediaType: String? = "NONE", // "IMAGE", "VIDEO", "NONE"
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isSaved: Boolean = false,
    val isReported: Boolean = false
)

@Entity(tableName = "post_likes", primaryKeys = ["postId", "userId"])
data class PostLikeEntity(
    val postId: String,
    val userId: String
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorProfilePic: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val parentId: String? = null // For replies to comments
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val mediaUrl: String? = null,
    val mediaType: String? = "NONE", // "IMAGE", "VIDEO", "FILE", "NONE"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "faqs")
data class FaqEntity(
    @PrimaryKey val id: String,
    val question: String,
    val answer: String
)

@Entity(tableName = "contact_messages")
data class ContactMessageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val message: String,
    val reply: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_settings")
data class SystemSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)
