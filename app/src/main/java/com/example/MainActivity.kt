package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.components.PremiumDarkGradient
import com.example.ui.components.PremiumLightGradient
import com.example.ui.components.RotatingWatermark
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MarceHubViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite Room Database
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "marcehub_ecosystem_database"
        ).fallbackToDestructiveMigration().build()

        // Initialize Repository layer
        val repository = AppRepository(
            userDao = database.userDao(),
            followDao = database.followDao(),
            postDao = database.postDao(),
            postLikeDao = database.postLikeDao(),
            commentDao = database.commentDao(),
            messageDao = database.messageDao(),
            announcementDao = database.announcementDao(),
            faqDao = database.faqDao(),
            contactMessageDao = database.contactMessageDao(),
            systemSettingDao = database.systemSettingDao(),
            activityLogDao = database.activityLogDao()
        )

        // ViewModel Factory definition
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MarceHubViewModel(repository) as T
            }
        }

        setContent {
            // Standard theme state (Light / Dark mode toggle)
            var isDarkMode by remember { mutableStateOf(true) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                val vm: MarceHubViewModel = viewModel(factory = factory)
                val currentUser by vm.currentUser.collectAsState()
                val activeTab by vm.activeTab.collectAsState()
                val systemNotifications by vm.systemNotifications.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDarkMode) PremiumDarkGradient else PremiumLightGradient)
                ) {
                    // Slow rotating watermark backdrop
                    RotatingWatermark()

                    if (currentUser == null) {
                        // Blocking Authentication Portal
                        LoginScreen(viewModel = vm)
                    } else {
                        val user = currentUser!!
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = Color.Transparent,
                            topBar = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .background(Color.White.copy(alpha = if (isDarkMode) 0.04f else 0.5f))
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .testTag("app_top_bar")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Brand Header
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                tint = Color(0xFF8B5CF6),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "MarceHub",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 20.sp,
                                                color = if (isDarkMode) Color.White else Color.Black
                                            )
                                        }

                                        // Dark Mode toggler & details
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { isDarkMode = !isDarkMode }) {
                                                Icon(
                                                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                    contentDescription = "Toggle theme",
                                                    tint = if (isDarkMode) Color.Yellow else Color(0xFF1E1B4B)
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.width(4.dp))
                                            
                                            // Quick Badge Indicator of active identity
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1).copy(0.15f))
                                            ) {
                                                Text(
                                                    text = "@${user.username}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF6366F1),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = Color.White.copy(alpha = if (isDarkMode) 0.04f else 0.6f),
                                    modifier = Modifier.navigationBarsPadding(),
                                    tonalElevation = 8.dp
                                ) {
                                    NavigationBarItem(
                                        selected = activeTab == "home",
                                        onClick = { vm.selectTab("home") },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                        label = { Text("Hub Feed", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_home")
                                    )
                                    NavigationBarItem(
                                        selected = activeTab == "messages",
                                        onClick = { vm.selectTab("messages") },
                                        icon = { Icon(Icons.Default.Forum, contentDescription = "Chats") },
                                        label = { Text("Messages", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_messages")
                                    )
                                    NavigationBarItem(
                                        selected = activeTab == "profile",
                                        onClick = { vm.selectTab("profile") },
                                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                                        label = { Text("Profile", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_profile")
                                    )
                                    // Render Admin matrix tab ONLY if Admin or Moderator credentials
                                    if (user.role == "Admin" || user.role == "Moderator") {
                                        NavigationBarItem(
                                            selected = activeTab == "admin",
                                            onClick = { vm.selectTab("admin") },
                                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Control Panel") },
                                            label = { Text("Admin Panel", fontSize = 11.sp) },
                                            modifier = Modifier.testTag("nav_admin")
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                // Dynamic Tab Renderer
                                when (activeTab) {
                                    "home" -> HomeScreen(viewModel = vm)
                                    "messages" -> MessagingScreen(viewModel = vm)
                                    "profile" -> ProfileScreen(viewModel = vm)
                                    "admin" -> AdminDashboardScreen(viewModel = vm)
                                }

                                // Interactive HUD Alert notification Banners overlays
                                AnimatedVisibility(
                                    visible = systemNotifications.isNotEmpty(),
                                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(16.dp)
                                ) {
                                    if (systemNotifications.isNotEmpty()) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1)),
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = CardDefaults.cardElevation(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { vm.dismissNotification(0) }
                                                .testTag("notification_banner")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = systemNotifications.last(),
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
