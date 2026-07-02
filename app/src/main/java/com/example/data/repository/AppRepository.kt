package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class AppRepository(
    private val userDao: UserDao,
    private val followDao: FollowDao,
    private val postDao: PostDao,
    private val postLikeDao: PostLikeDao,
    private val commentDao: CommentDao,
    private val messageDao: MessageDao,
    private val announcementDao: AnnouncementDao,
    private val faqDao: FaqDao,
    private val contactMessageDao: ContactMessageDao,
    private val systemSettingDao: SystemSettingDao,
    private val activityLogDao: ActivityLogDao
) {
    // Flows
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncements()
    val allFaqs: Flow<List<FaqEntity>> = faqDao.getAllFaqs()
    val allContactMessages: Flow<List<ContactMessageEntity>> = contactMessageDao.getAllContactMessages()
    val allSettings: Flow<List<SystemSettingEntity>> = systemSettingDao.getAllSettings()
    val allLogs: Flow<List<ActivityLogEntity>> = activityLogDao.getAllLogs()

    // Users
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
    fun getUserByEmailFlow(email: String): Flow<UserEntity?> = userDao.getUserByEmailFlow(email)
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun deleteUser(email: String) {
        userDao.deleteUserByEmail(email)
        logActivity(email, "Account Deleted")
    }

    // Follow / Unfollow
    fun getFollowers(userId: String): Flow<List<FollowEntity>> = followDao.getFollowersFlow(userId)
    fun getFollowing(userId: String): Flow<List<FollowEntity>> = followDao.getFollowingFlow(userId)

    suspend fun followUser(followerId: String, followingId: String) {
        followDao.insertFollow(FollowEntity(followerId, followingId))
        
        // Update counts
        val follower = userDao.getUserByEmail(followerId)
        val following = userDao.getUserByEmail(followingId)
        
        if (follower != null) {
            userDao.insertUser(follower.copy(followingCount = follower.followingCount + 1))
        }
        if (following != null) {
            userDao.insertUser(following.copy(followersCount = following.followersCount + 1))
        }
        
        logActivity(followerId, "Followed user: $followingId")
    }

    suspend fun unfollowUser(followerId: String, followingId: String) {
        followDao.deleteFollow(followerId, followingId)
        
        // Update counts
        val follower = userDao.getUserByEmail(followerId)
        val following = userDao.getUserByEmail(followingId)
        
        if (follower != null) {
            userDao.insertUser(follower.copy(followingCount = (follower.followingCount - 1).coerceAtLeast(0)))
        }
        if (following != null) {
            userDao.insertUser(following.copy(followersCount = (following.followersCount - 1).coerceAtLeast(0)))
        }
        
        logActivity(followerId, "Unfollowed user: $followingId")
    }

    // Posts
    suspend fun createPost(
        authorId: String,
        authorName: String,
        authorProfilePic: String,
        content: String,
        mediaUrl: String? = null,
        mediaType: String? = "NONE"
    ) {
        val post = PostEntity(
            id = UUID.randomUUID().toString(),
            authorId = authorId,
            authorName = authorName,
            authorProfilePic = authorProfilePic,
            content = content,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            timestamp = System.currentTimeMillis()
        )
        postDao.insertPost(post)
        logActivity(authorId, "Created a new post")
    }

    suspend fun updatePost(post: PostEntity) {
        postDao.insertPost(post)
        logActivity(post.authorId, "Edited post: ${post.id}")
    }

    suspend fun deletePost(postId: String, userId: String) {
        postDao.deletePostById(postId)
        logActivity(userId, "Deleted post: $postId")
    }

    // Liking
    fun getLikesForPost(postId: String): Flow<List<PostLikeEntity>> = postLikeDao.getLikesForPost(postId)

    suspend fun likePost(postId: String, userId: String) {
        postLikeDao.insertLike(PostLikeEntity(postId, userId))
        val post = postDao.getPostById(postId)
        if (post != null) {
            postDao.insertPost(post.copy(likesCount = post.likesCount + 1))
        }
        logActivity(userId, "Liked post: $postId")
    }

    suspend fun unlikePost(postId: String, userId: String) {
        postLikeDao.deleteLike(postId, userId)
        val post = postDao.getPostById(postId)
        if (post != null) {
            postDao.insertPost(post.copy(likesCount = (post.likesCount - 1).coerceAtLeast(0)))
        }
        logActivity(userId, "Unliked post: $postId")
    }

    // Comments & Replies
    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> = commentDao.getCommentsForPost(postId)

    suspend fun addComment(
        postId: String,
        authorId: String,
        authorName: String,
        authorProfilePic: String,
        content: String,
        parentId: String? = null
    ) {
        val comment = CommentEntity(
            id = UUID.randomUUID().toString(),
            postId = postId,
            authorId = authorId,
            authorName = authorName,
            authorProfilePic = authorProfilePic,
            content = content,
            parentId = parentId,
            timestamp = System.currentTimeMillis()
        )
        commentDao.insertComment(comment)
        
        val post = postDao.getPostById(postId)
        if (post != null) {
            postDao.insertPost(post.copy(commentsCount = post.commentsCount + 1))
        }
        
        logActivity(authorId, if (parentId == null) "Commented on post: $postId" else "Replied to comment in post: $postId")
    }

    suspend fun deleteComment(commentId: String, postId: String, userId: String) {
        commentDao.deleteCommentById(commentId)
        val post = postDao.getPostById(postId)
        if (post != null) {
            postDao.insertPost(post.copy(commentsCount = (post.commentsCount - 1).coerceAtLeast(0)))
        }
        logActivity(userId, "Deleted comment: $commentId")
    }

    // Messaging
    fun getChatMessages(sender: String, receiver: String): Flow<List<MessageEntity>> =
        messageDao.getChatMessages(sender, receiver)

    suspend fun sendMessage(
        senderId: String,
        receiverId: String,
        content: String,
        mediaUrl: String? = null,
        mediaType: String? = "NONE"
    ) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        messageDao.insertMessage(message)
        logActivity(senderId, "Sent message to: $receiverId")
    }

    suspend fun markAsRead(messageId: String) = messageDao.markAsRead(messageId)

    // Announcements
    suspend fun createAnnouncement(title: String, content: String) {
        val ann = AnnouncementEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        announcementDao.insertAnnouncement(ann)
    }

    suspend fun deleteAnnouncement(id: String) = announcementDao.deleteAnnouncementById(id)

    // FAQ
    suspend fun addFaq(question: String, answer: String) {
        val faq = FaqEntity(
            id = UUID.randomUUID().toString(),
            question = question,
            answer = answer
        )
        faqDao.insertFaq(faq)
    }

    suspend fun deleteFaq(id: String) = faqDao.deleteFaqById(id)

    // Contact Us
    suspend fun sendContactMessage(name: String, email: String, message: String) {
        val msg = ContactMessageEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        contactMessageDao.insertContactMessage(msg)
    }

    suspend fun replyToContactMessage(id: String, reply: String) {
        val list = contactMessageDao.getAllContactMessages().firstOrNull() ?: emptyList()
        val found = list.find { it.id == id }
        if (found != null) {
            contactMessageDao.insertContactMessage(found.copy(reply = reply))
        }
    }

    suspend fun deleteContactMessage(id: String) = contactMessageDao.deleteContactMessageById(id)

    // System Settings
    suspend fun updateSetting(key: String, value: String) {
        systemSettingDao.insertSetting(SystemSettingEntity(key, value))
    }

    suspend fun getSettingValue(key: String): String? {
        return systemSettingDao.getSettingByKey(key)?.value
    }

    // Logging
    suspend fun logActivity(userId: String, action: String) {
        val log = ActivityLogEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            action = action,
            timestamp = System.currentTimeMillis()
        )
        activityLogDao.insertLog(log)
    }

    suspend fun clearLogs() = activityLogDao.clearLogs()

    // Backup & Restore Simulation
    suspend fun backupData(): String {
        // Gathers all records in a simple clean text format representing standard JSON backup
        val users = allUsers.firstOrNull() ?: emptyList()
        val posts = allPosts.firstOrNull() ?: emptyList()
        val faqs = allFaqs.firstOrNull() ?: emptyList()
        val anns = allAnnouncements.firstOrNull() ?: emptyList()
        
        return StringBuilder().apply {
            append("--- MARCEHUB DATABASE BACKUP ---\n")
            append("TIMESTAMP: ${System.currentTimeMillis()}\n")
            append("USERS_COUNT: ${users.size}\n")
            users.forEach { append("USER: ${it.email}|${it.username}|${it.role}|${it.country}|${it.isBanned}\n") }
            append("POSTS_COUNT: ${posts.size}\n")
            posts.forEach { append("POST: ${it.id}|${it.authorId}|${it.content.replace("\n", " ")}\n") }
            append("FAQS_COUNT: ${faqs.size}\n")
            faqs.forEach { append("FAQ: ${it.id}|${it.question}|${it.answer}\n") }
            append("ANNOUNCEMENTS_COUNT: ${anns.size}\n")
            anns.forEach { append("ANN: ${it.id}|${it.title}|${it.content}\n") }
            append("--- END OF BACKUP ---")
        }.toString()
    }

    suspend fun restoreData(backupContent: String): Boolean {
        return try {
            val lines = backupContent.split("\n")
            if (!lines.first().contains("MARCEHUB DATABASE BACKUP")) return false
            
            lines.forEach { line ->
                if (line.startsWith("USER: ")) {
                    val parts = line.substring(6).split("|")
                    if (parts.size >= 5) {
                        insertUser(
                            UserEntity(
                                email = parts[0],
                                username = parts[1],
                                bio = "Restored user",
                                country = parts[3],
                                role = parts[2],
                                isBanned = parts[4].toBoolean(),
                                profilePic = "avatar_1"
                            )
                        )
                    }
                } else if (line.startsWith("POST: ")) {
                    val parts = line.substring(6).split("|")
                    if (parts.size >= 3) {
                        postDao.insertPost(
                            PostEntity(
                                id = parts[0],
                                authorId = parts[1],
                                authorName = "Restored Author",
                                authorProfilePic = "avatar_1",
                                content = parts[2]
                            )
                        )
                    }
                } else if (line.startsWith("FAQ: ")) {
                    val parts = line.substring(5).split("|")
                    if (parts.size >= 3) {
                        faqDao.insertFaq(
                            FaqEntity(
                                id = parts[0],
                                question = parts[1],
                                answer = parts[2]
                            )
                        )
                    }
                } else if (line.startsWith("ANN: ")) {
                    val parts = line.substring(5).split("|")
                    if (parts.size >= 3) {
                        announcementDao.insertAnnouncement(
                            AnnouncementEntity(
                                id = parts[0],
                                title = parts[1],
                                content = parts[2]
                            )
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
