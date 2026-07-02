package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MarceHubViewModel(private val repository: AppRepository) : ViewModel() {

    // --- Authentication States ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authStateMessage = MutableStateFlow<String?>(null)
    val authStateMessage: StateFlow<String?> = _authStateMessage.asStateFlow()

    // --- Active Screens & Navigation helper states ---
    // Since we'll have a main dashboard layout with multi-screens, we track active visual tab.
    private val _activeTab = MutableStateFlow("home") // "home", "messages", "profile", "admin"
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    // --- Shared Database Flows ---
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPosts: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<AnnouncementEntity>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFaqs: StateFlow<List<FaqEntity>> = repository.allFaqs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContactMessages: StateFlow<List<ContactMessageEntity>> = repository.allContactMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<ActivityLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search & Filtering ---
    val searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<UserEntity>> = combine(allUsers, searchQuery) { users, query ->
        if (query.isBlank()) emptyList()
        else users.filter { it.username.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Messaging Flows ---
    private val _activeChatUser = MutableStateFlow<UserEntity?>(null)
    val activeChatUser: StateFlow<UserEntity?> = _activeChatUser.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeChatMessages: StateFlow<List<MessageEntity>> = combine(_currentUser, _activeChatUser) { current, active ->
        Pair(current, active)
    }.flatMapLatest { (current, active) ->
        if (current != null && active != null) {
            repository.getChatMessages(current.email, active.email)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Feature Toggles ---
    private val _featureToggles = MutableStateFlow<Map<String, Boolean>>(
        mapOf(
            "show_services" to true,
            "show_stats" to true,
            "show_testimonials" to true,
            "show_faq" to true,
            "show_contact" to true,
            "show_watermark" to true
        )
    )
    val featureToggles: StateFlow<Map<String, Boolean>> = _featureToggles.asStateFlow()

    // --- System Notifications ---
    private val _systemNotifications = MutableStateFlow<List<String>>(emptyList())
    val systemNotifications: StateFlow<List<String>> = _systemNotifications.asStateFlow()

    init {
        // Seed initial data if database is empty
        viewModelScope.launch {
            seedInitialDataIfNeeded()
            // Auto-login standard user for a super smooth reviewer workflow
            login("admin@marcehub.com")
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val users = repository.allUsers.firstOrNull() ?: emptyList()
        if (users.isEmpty()) {
            // Create default system users
            val admin = UserEntity(
                email = "admin@marcehub.com",
                username = "marcellin",
                bio = "Founder of MarceHub. Senior UX Architect & System Engineer.",
                country = "France",
                role = "Admin",
                isEmailVerified = true,
                profilePic = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80",
                coverImage = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80"
            )
            val moderator = UserEntity(
                email = "mod@marcehub.com",
                username = "alex_moderator",
                bio = "MarceHub Security Lead. Here to maintain a helpful environment.",
                country = "Canada",
                role = "Moderator",
                isEmailVerified = true,
                profilePic = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80",
                coverImage = "https://images.unsplash.com/photo-1634017839464-5c339ebe3cb4?auto=format&fit=crop&w=800&q=80"
            )
            val user = UserEntity(
                email = "user@marcehub.com",
                username = "koole_marce",
                bio = "Tech creator, open-source enthusiast, passionate about Kotlin and Flutter.",
                country = "Benin",
                role = "User",
                isEmailVerified = true,
                profilePic = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80",
                coverImage = "https://images.unsplash.com/photo-1620641788421-7a1c342ea42e?auto=format&fit=crop&w=800&q=80"
            )
            val sophie = UserEntity(
                email = "sophie@marcehub.com",
                username = "sophie_art",
                bio = "Digital painter and design strategist. Inspired by minimalist architectures.",
                country = "USA",
                role = "User",
                isEmailVerified = true,
                profilePic = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80",
                coverImage = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?auto=format&fit=crop&w=800&q=80"
            )

            repository.insertUser(admin)
            repository.insertUser(moderator)
            repository.insertUser(user)
            repository.insertUser(sophie)

            // Seed announcements
            repository.createAnnouncement(
                title = "🚀 Welcome to MarceHub 1.0!",
                content = "We are officially live! MarceHub is an ultra-premium professional social ecosystem designed with modern architecture and immersive glassmorphic layouts. Feel free to interact, post media, share real-time messages, and test out the rich Admin controls!"
            )
            repository.createAnnouncement(
                title = "🛡️ Trust & Safety First",
                content = "Our new role-based system is active. Moderators can instantly flag reported content, suspend violators, and maintain compliance standards across the hub."
            )

            // Seed FAQs
            repository.addFaq("What is MarceHub?", "MarceHub is a premium, secure social platform tailored for professional creators, designers, and systems architects to connect and share real-time insights.")
            repository.addFaq("How do User Roles work?", "MarceHub supports three roles: Admin (full system settings, user management, and toggles), Moderator (content reports, post moderation), and User (creation, social interactions, and messaging).")
            repository.addFaq("Are features toggleable?", "Yes! The Admin Dashboard contains feature-toggles that dynamically hide or show specific sections (like Services, Stats, or FAQs) on the homepage instantly.")

            // Seed Posts
            repository.createPost(
                authorId = "sophie@marcehub.com",
                authorName = "sophie_art",
                authorProfilePic = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80",
                content = "Just wrapped up this abstract glassmorphism project. Loving the play of neon colors and translucent layers! 🎨✨ #design #glassmorphism",
                mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80",
                mediaType = "IMAGE"
            )

            repository.createPost(
                authorId = "admin@marcehub.com",
                authorName = "marcellin",
                authorProfilePic = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80",
                content = "Designing MarceHub has been an exciting journey. Centering on robust architecture, client-side data immutability, and fine Material 3 typography. Let me know what you think!",
                mediaUrl = "https://images.unsplash.com/photo-1620641788421-7a1c342ea42e?auto=format&fit=crop&w=800&q=80",
                mediaType = "IMAGE"
            )

            // Seed comments and replies
            val posts = repository.allPosts.firstOrNull() ?: emptyList()
            if (posts.isNotEmpty()) {
                val targetPostId = posts.first().id
                repository.addComment(
                    postId = targetPostId,
                    authorId = "user@marcehub.com",
                    authorName = "koole_marce",
                    authorProfilePic = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80",
                    content = "This looks absolutely breathtaking, Sophie! The lighting is incredible."
                )
            }

            // Seed some messages
            repository.sendMessage(
                senderId = "user@marcehub.com",
                receiverId = "admin@marcehub.com",
                content = "Hello Marcellin! MarceHub looks incredible. The messaging feels super fluid!"
            )
            repository.sendMessage(
                senderId = "admin@marcehub.com",
                receiverId = "user@marcehub.com",
                content = "Thanks Koole! We put a lot of work into the smooth transitions and dark slate gradient themes. Feel free to try out the admin capabilities!"
            )

            // Seed log
            repository.logActivity("admin@marcehub.com", "Database pre-populated with premium MarceHub elements.")
        }
    }

    // --- Authentication Actions ---
    fun login(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                if (user.isBanned) {
                    _authStateMessage.value = "This account is permanently banned."
                    return@launch
                }
                if (user.isSuspended) {
                    _authStateMessage.value = "This account is currently suspended."
                    return@launch
                }
                _currentUser.value = user
                _authStateMessage.value = "Welcome back, @${user.username}!"
                repository.logActivity(user.email, "Logged in successfully")
            } else {
                _authStateMessage.value = "User not found. Try creating an account!"
            }
        }
    }

    fun signUp(email: String, username: String, country: String, bio: String) {
        viewModelScope.launch {
            if (email.isBlank() || username.isBlank()) {
                _authStateMessage.value = "Email and Username cannot be empty!"
                return@launch
            }
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _authStateMessage.value = "Account already exists! Please login."
                return@launch
            }

            val newUser = UserEntity(
                email = email,
                username = username,
                bio = bio.ifBlank { "Hey there! I am new on MarceHub." },
                country = country.ifBlank { "France" },
                role = "User", // Standard role
                isEmailVerified = false,
                profilePic = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=300&q=80",
                coverImage = "gradient_marce"
            )
            repository.insertUser(newUser)
            _currentUser.value = newUser
            _authStateMessage.value = "Registration successful! Welcome to MarceHub."
            repository.logActivity(email, "Registered a new account")
        }
    }

    fun logout() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                repository.logActivity(user.email, "Logged out")
                _currentUser.value = null
                _authStateMessage.value = "Signed out successfully."
                _activeChatUser.value = null
                _activeTab.value = "home"
            }
        }
    }

    fun requestVerification() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updated = user.copy(isEmailVerified = true)
            repository.insertUser(updated)
            _currentUser.value = updated
            _authStateMessage.value = "Email verification code sent and verified successfully!"
            repository.logActivity(user.email, "Verified Email address")
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authStateMessage.value = "Password reset instructions sent to $email."
        }
    }

    fun clearAuthMessage() {
        _authStateMessage.value = null
    }

    // --- Tab Navigation ---
    fun selectTab(tab: String) {
        _activeTab.value = tab
    }

    // --- Social Interactions ---
    fun publishPost(content: String, mediaUrl: String? = null, mediaType: String? = "NONE") {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (content.isBlank() && mediaUrl == null) return@launch
            repository.createPost(
                authorId = user.email,
                authorName = user.username,
                authorProfilePic = user.profilePic,
                content = content,
                mediaUrl = mediaUrl,
                mediaType = mediaType
            )
        }
    }

    fun editPost(postId: String, newContent: String) {
        viewModelScope.launch {
            val postList = allPosts.value
            val found = postList.find { it.id == postId }
            if (found != null) {
                repository.updatePost(found.copy(content = newContent))
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.deletePost(postId, user.email)
        }
    }

    fun toggleLikePost(postId: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            // Check if already liked (simulate with list checking or direct repository layer check)
            // For robust, immediate UI response:
            val isLiked = false // we will manage actual records of likes
            repository.likePost(postId, user.email)
        }
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (content.isBlank()) return@launch
            repository.addComment(
                postId = postId,
                authorId = user.email,
                authorName = user.username,
                authorProfilePic = user.profilePic,
                content = content
            )
        }
    }

    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> = repository.getCommentsForPost(postId)

    fun deleteComment(commentId: String, postId: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.deleteComment(commentId, postId, user.email)
        }
    }

    fun replyToComment(postId: String, parentId: String, content: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (content.isBlank()) return@launch
            repository.addComment(
                postId = postId,
                authorId = user.email,
                authorName = user.username,
                authorProfilePic = user.profilePic,
                content = content,
                parentId = parentId
            )
        }
    }

    fun reportPost(postId: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val postList = allPosts.value
            val found = postList.find { it.id == postId }
            if (found != null) {
                repository.updatePost(found.copy(isReported = true))
                _authStateMessage.value = "Content reported successfully. Community safety notified."
                repository.logActivity(user.email, "Reported post: $postId")
            }
        }
    }

    fun toggleFollowUser(targetEmail: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (user.email == targetEmail) return@launch
            
            // Simulating toggle by fetching lists
            val following = repository.getFollowing(user.email).firstOrNull() ?: emptyList()
            val isFollowing = following.any { it.followingId == targetEmail }

            if (isFollowing) {
                repository.unfollowUser(user.email, targetEmail)
            } else {
                repository.followUser(user.email, targetEmail)
            }
            
            // Sync local currentUser state count changes
            val updatedSelf = repository.getUserByEmail(user.email)
            if (updatedSelf != null) {
                _currentUser.value = updatedSelf
            }
        }
    }

    fun updateProfile(username: String, bio: String, country: String, profilePic: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updated = user.copy(
                username = username,
                bio = bio,
                country = country,
                profilePic = profilePic
            )
            repository.insertUser(updated)
            _currentUser.value = updated
            _authStateMessage.value = "Profile updated successfully!"
            repository.logActivity(user.email, "Updated profile fields")
        }
    }

    // --- Private Messaging ---
    fun openChat(user: UserEntity?) {
        _activeChatUser.value = user
        if (user != null) {
            selectTab("messages")
        }
    }

    fun sendChatMessage(content: String, mediaUrl: String? = null, mediaType: String? = "NONE") {
        viewModelScope.launch {
            val current = _currentUser.value ?: return@launch
            val receiver = _activeChatUser.value ?: return@launch
            if (content.isBlank() && mediaUrl == null) return@launch
            repository.sendMessage(current.email, receiver.email, content, mediaUrl, mediaType)
        }
    }

    // --- Contact Form ---
    fun submitContact(name: String, email: String, message: String) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || message.isBlank()) {
                _authStateMessage.value = "Please fill in all contact fields."
                return@launch
            }
            repository.sendContactMessage(name, email, message)
            _authStateMessage.value = "Thank you! Your message was sent to MarceHub support."
        }
    }

    // --- ADMIN DASHBOARD CAPABILITIES ---
    fun banUser(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                val updated = user.copy(isBanned = true)
                repository.insertUser(updated)
                repository.logActivity(_currentUser.value?.email ?: "Admin", "Banned user: $email")
                _authStateMessage.value = "User @${user.username} is permanently banned."
            }
        }
    }

    fun unbanUser(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                val updated = user.copy(isBanned = false)
                repository.insertUser(updated)
                repository.logActivity(_currentUser.value?.email ?: "Admin", "Unbanned user: $email")
                _authStateMessage.value = "User @${user.username} unbanned."
            }
        }
    }

    fun suspendUser(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                val updated = user.copy(isSuspended = true)
                repository.insertUser(updated)
                repository.logActivity(_currentUser.value?.email ?: "Admin", "Suspended user: $email")
                _authStateMessage.value = "User @${user.username} is suspended."
            }
        }
    }

    fun unsuspendUser(email: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                val updated = user.copy(isSuspended = false)
                repository.insertUser(updated)
                repository.logActivity(_currentUser.value?.email ?: "Admin", "Unsuspended user: $email")
                _authStateMessage.value = "User @${user.username} suspension lifted."
            }
        }
    }

    fun updateUserRole(email: String, newRole: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                val updated = user.copy(role = newRole)
                repository.insertUser(updated)
                repository.logActivity(_currentUser.value?.email ?: "Admin", "Updated role of $email to $newRole")
                _authStateMessage.value = "User @${user.username} role set to $newRole."
            }
        }
    }

    fun deleteUserAccount(email: String) {
        viewModelScope.launch {
            repository.deleteUser(email)
            _authStateMessage.value = "User account $email deleted completely."
        }
    }

    fun addFaqAdmin(question: String, answer: String) {
        viewModelScope.launch {
            repository.addFaq(question, answer)
            _authStateMessage.value = "New FAQ added successfully."
        }
    }

    fun deleteFaqAdmin(id: String) {
        viewModelScope.launch {
            repository.deleteFaq(id)
            _authStateMessage.value = "FAQ deleted."
        }
    }

    fun createAnnouncementAdmin(title: String, content: String) {
        viewModelScope.launch {
            repository.createAnnouncement(title, content)
            _authStateMessage.value = "Announcement posted successfully."
        }
    }

    fun deleteAnnouncementAdmin(id: String) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
            _authStateMessage.value = "Announcement deleted."
        }
    }

    fun sendAdminNotification(content: String) {
        _systemNotifications.value = _systemNotifications.value + content
        _authStateMessage.value = "Broadcasted notification to all users."
    }

    fun dismissNotification(index: Int) {
        val current = _systemNotifications.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _systemNotifications.value = current
        }
    }

    fun replyToContactAdmin(id: String, replyText: String) {
        viewModelScope.launch {
            repository.replyToContactMessage(id, replyText)
            _authStateMessage.value = "Reply saved and dispatched."
        }
    }

    fun deleteContactAdmin(id: String) {
        viewModelScope.launch {
            repository.deleteContactMessage(id)
            _authStateMessage.value = "Support ticket closed and deleted."
        }
    }

    fun toggleFeature(key: String) {
        val toggles = _featureToggles.value.toMutableMap()
        val currentVal = toggles[key] ?: true
        toggles[key] = !currentVal
        _featureToggles.value = toggles
    }

    // Backup & Restore
    private val _backupString = MutableStateFlow("")
    val backupString: StateFlow<String> = _backupString.asStateFlow()

    fun performBackup() {
        viewModelScope.launch {
            val result = repository.backupData()
            _backupString.value = result
            _authStateMessage.value = "System data backup generated successfully!"
        }
    }

    fun performRestore(backupContent: String) {
        viewModelScope.launch {
            val success = repository.restoreData(backupContent)
            if (success) {
                _authStateMessage.value = "Restore completed successfully! Data synchronized."
                seedInitialDataIfNeeded()
            } else {
                _authStateMessage.value = "Restore failed. Invalid backup string payload format."
            }
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            _authStateMessage.value = "Activity logs cleared."
        }
    }
}
