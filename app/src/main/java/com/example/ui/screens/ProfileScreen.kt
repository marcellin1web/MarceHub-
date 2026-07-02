package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.*
import com.example.data.model.*
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.viewmodel.MarceHubViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MarceHubViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    if (currentUser == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Please login to view your profile.", color = Color.Gray)
        }
        return
    }

    val user = currentUser!!

    // Edit states
    var editUsername by remember { mutableStateOf(user.username) }
    var editBio by remember { mutableStateOf(user.bio) }
    var editCountry by remember { mutableStateOf(user.country) }
    var editProfilePic by remember { mutableStateOf(user.profilePic) }
    var showSettings by remember { mutableStateOf(false) }

    // Synchronize states if user changes
    LaunchedEffect(user) {
        editUsername = user.username
        editBio = user.bio
        editCountry = user.country
        editProfilePic = user.profilePic
    }

    // Filter logs for this specific user
    val userLogs = logs.filter { it.userId == user.email }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen_list"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Cover Image and Profile Picture Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .testTag("profile_header_box")
            ) {
                // Cover Image
                AsyncImage(
                    model = user.coverImage,
                    contentDescription = "Cover Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            Brush.sweepGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF6366F1))
                            )
                        ),
                    contentScale = ContentScale.Crop
                )

                // Profile Pic overlapping
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                ) {
                    AsyncImage(
                        model = editProfilePic,
                        contentDescription = "Profile Pic",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .testTag("profile_avatar_image"),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // 2. Main Profile Metadata Details
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "@${user.username}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.testTag("profile_username_title")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    // Verified Badge
                    if (user.isEmailVerified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Email Verified Status",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier
                                .size(20.dp)
                                .testTag("verified_badge")
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Unverified Status",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier
                                .size(20.dp)
                                .testTag("unverified_badge")
                        )
                    }
                }

                Text(
                    text = user.email,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = "Country", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = user.country, fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Role", tint = Color(0xFF8B5CF6), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Role: ${user.role}", fontSize = 13.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                }

                // Followers and Following statistics
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${user.followersCount}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color.Black)
                        Text(text = "Followers", fontSize = 11.sp, color = Color.Gray)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.Gray.copy(alpha = 0.3f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${user.followingCount}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color.Black)
                        Text(text = "Following", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                // Bio
                Text(
                    text = user.bio,
                    fontSize = 14.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .testTag("profile_bio_text")
                )

                // Edit Profile / Settings toggler row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showSettings = !showSettings },
                        colors = ButtonDefaults.buttonColors(containerColor = if (showSettings) Color.Gray else Color(0xFF6366F1)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("profile_edit_settings_button")
                    ) {
                        Icon(if (showSettings) Icons.Default.Close else Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showSettings) "Hide Custom Settings" else "Settings & Verification")
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Red.copy(0.1f))
                            .testTag("profile_logout_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = Color.Red)
                    }
                }
            }
        }

        // 3. Dynamic Settings Panel
        if (showSettings) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("settings_form_container")
                ) {
                    Text("⚙️ Profile Customization Hub", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isDark) Color.White else Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Update Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("edit_username_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                    )

                    OutlinedTextField(
                        value = editCountry,
                        onValueChange = { editCountry = it },
                        label = { Text("Update Country") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("edit_country_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                    )

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Update Biography") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(bottom = 12.dp)
                            .testTag("edit_bio_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                    )

                    // Avatar Selection Simulation
                    Text("Select Avatar Template", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
                            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                            "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80",
                            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80"
                        ).forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .clickable { editProfilePic = url }
                                    .background(if (editProfilePic == url) Color(0xFFEC4899) else Color.Transparent)
                                    .padding(3.dp)
                                    .clip(CircleShape)
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.updateProfile(editUsername, editBio, editCountry, editProfilePic)
                            showSettings = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_profile_button")
                    ) {
                        Text("Save Profile Settings", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Verification Action Box
                    Text("📧 Email Verification Suite", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color.White else Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (user.isEmailVerified) "Your email address is verified. Full professional badge active."
                        else "Verify your email address now to enable the verified community crest.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (!user.isEmailVerified) {
                        Button(
                            onClick = { viewModel.requestVerification() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("verify_email_button")
                        ) {
                            Text("Simulate Verification Verification", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 4. Activity Logs History
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "📜 YOUR AUDIT ACTIVITY LOGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (userLogs.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("No recorded actions yet on this account.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        items(userLogs.take(10)) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("log_item_${log.id}")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(log.action, fontSize = 12.sp, color = if (isDark) Color.White else Color.Black)
                    }
                    Text(
                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
