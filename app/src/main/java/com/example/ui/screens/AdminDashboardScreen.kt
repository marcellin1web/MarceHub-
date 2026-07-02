package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.*
import com.example.data.model.*
import com.example.ui.viewmodel.MarceHubViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: MarceHubViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val posts by viewModel.allPosts.collectAsState()
    val faqs by viewModel.allFaqs.collectAsState()
    val contactMessages by viewModel.allContactMessages.collectAsState()
    val toggles by viewModel.featureToggles.collectAsState()
    val backupString by viewModel.backupString.collectAsState()

    var activeAdminTab by remember { mutableStateOf("users") } // "users", "content", "settings", "backup"

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    if (currentUser == null || (currentUser?.role != "Admin" && currentUser?.role != "Moderator")) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Access Denied. Administrative credentials required.", color = Color.Red, fontWeight = FontWeight.Bold)
        }
        return
    }

    val self = currentUser!!

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Dashboard Title and Role indicator
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "🛡️ ADMIN SECURITY COMMAND",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEC4899),
                    letterSpacing = 2.sp
                )
                Text(
                    text = "System Operator Matrix",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f))
            ) {
                Text(
                    text = "Operator: ${self.role}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Horizontal admin sub-tab bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminSubTabButton("Users", activeAdminTab == "users", Modifier.weight(1f)) { activeAdminTab = "users" }
            AdminSubTabButton("Moderation", activeAdminTab == "content", Modifier.weight(1f)) { activeAdminTab = "content" }
            AdminSubTabButton("Homepage", activeAdminTab == "settings", Modifier.weight(1f)) { activeAdminTab = "settings" }
            AdminSubTabButton("Backups", activeAdminTab == "backup", Modifier.weight(1f)) { activeAdminTab = "backup" }
        }

        Divider(color = if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.1f), modifier = Modifier.padding(bottom = 16.dp))

        // VIEW RENDERING BASED ON SELECTED SUB-TAB
        Box(modifier = Modifier.weight(1f)) {
            when (activeAdminTab) {
                "users" -> UserManagementView(viewModel, users, isDark)
                "content" -> ModerationView(viewModel, posts, contactMessages, isDark)
                "settings" -> HomepageSettingsView(viewModel, toggles, faqs, isDark)
                "backup" -> DataBackupView(viewModel, backupString, isDark)
            }
        }
    }
}

@Composable
fun AdminSubTabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF6366F1) else Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(38.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}

// --- SUB-VIEW 1: USER MANAGEMENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementView(
    viewModel: MarceHubViewModel,
    users: List<UserEntity>,
    isDark: Boolean
) {
    var query by remember { mutableStateOf("") }
    
    val filteredUsers = users.filter {
        it.username.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search users to modify...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("admin_user_search"),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black),
            singleLine = true
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredUsers) { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    modifier = Modifier.fillMaxWidth().testTag("admin_user_item_${user.email}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = user.profilePic,
                                contentDescription = "Pic",
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Gray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${user.role})",
                                        fontSize = 11.sp,
                                        color = Color(0xFF6366F1),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(user.email, fontSize = 11.sp, color = Color.Gray)
                            }

                            // Block indicator status
                            if (user.isBanned) {
                                Text("Banned", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else if (user.isSuspended) {
                                Text("Suspended", color = Color.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = if (isDark) Color.White.copy(0.05f) else Color.Black.copy(0.05f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Admin actions row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Role toggle
                            TextButton(
                                onClick = {
                                    val nextRole = when (user.role) {
                                        "User" -> "Moderator"
                                        "Moderator" -> "Admin"
                                        else -> "User"
                                    }
                                    viewModel.updateUserRole(user.email, nextRole)
                                },
                                modifier = Modifier.testTag("admin_user_toggle_role_${user.email}")
                            ) {
                                Text("Role: ${user.role}", fontSize = 11.sp, color = Color(0xFF8B5CF6))
                            }

                            // Ban / Unban
                            TextButton(
                                onClick = {
                                    if (user.isBanned) viewModel.unbanUser(user.email)
                                    else viewModel.banUser(user.email)
                                },
                                modifier = Modifier.testTag("admin_user_ban_${user.email}")
                            ) {
                                Text(
                                    text = if (user.isBanned) "Lift Ban" else "Permanently Ban",
                                    fontSize = 11.sp,
                                    color = Color.Red
                                )
                            }

                            // Suspend / Unsuspend
                            TextButton(
                                onClick = {
                                    if (user.isSuspended) viewModel.unsuspendUser(user.email)
                                    else viewModel.suspendUser(user.email)
                                },
                                modifier = Modifier.testTag("admin_user_suspend_${user.email}")
                            ) {
                                Text(
                                    text = if (user.isSuspended) "Lift Suspend" else "Suspend",
                                    fontSize = 11.sp,
                                    color = Color(0xFFF59E0B)
                                )
                            }

                            // Permanent deletion
                            IconButton(
                                onClick = { viewModel.deleteUserAccount(user.email) },
                                modifier = Modifier.testTag("admin_user_delete_${user.email}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Account", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-VIEW 2: MODERATION OF CONTENT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationView(
    viewModel: MarceHubViewModel,
    posts: List<PostEntity>,
    contactMessages: List<com.example.data.model.ContactMessageEntity>,
    isDark: Boolean
) {
    val reportedPosts = posts.filter { it.isReported }
    var replyText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Segment A: Reported Posts
        item {
            Text("⚠️ REPORTED SOCIAL FEEDS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
        }

        if (reportedPosts.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.02f)), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("All feeds are safe. No content currently flagged.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(reportedPosts) { post ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth().testTag("reported_item_${post.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("@${post.authorName}: ${post.content}", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    // Remove report status (Ignored)
                                    viewModel.publishPost("Content cleared safety check by moderator: ${post.id}")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Ignore Flag", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = { viewModel.deletePost(post.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.height(32.dp).testTag("moderation_delete_post_${post.id}")
                            ) {
                                Text("Delete Content", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Segment B: Support / Contact inquiries
        item {
            Text("📬 USER SUPPORT TICKETS INBOUND", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
        }

        if (contactMessages.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.02f)), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No pending support tickets.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(contactMessages) { ticket ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.03f)),
                    modifier = Modifier.fillMaxWidth().testTag("ticket_item_${ticket.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("From: ${ticket.name} (${ticket.email})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("\"${ticket.message}\"", fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
                        
                        if (ticket.reply != null) {
                            Text("Your Reply: ${ticket.reply}", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = { Text("Compose official response...", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().height(48.dp).padding(vertical = 4.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        if (replyText.isNotBlank()) {
                                            viewModel.replyToContactAdmin(ticket.id, replyText)
                                            replyText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                    modifier = Modifier.height(32.dp).testTag("ticket_reply_${ticket.id}")
                                ) {
                                    Text("Dispatch Response", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { viewModel.deleteContactAdmin(ticket.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    modifier = Modifier.height(32.dp).testTag("ticket_delete_${ticket.id}")
                                ) {
                                    Text("Delete Ticket", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-VIEW 3: HOMEPAGE SETTINGS & TOGGLES ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomepageSettingsView(
    viewModel: MarceHubViewModel,
    toggles: Map<String, Boolean>,
    faqs: List<com.example.data.model.FaqEntity>,
    isDark: Boolean
) {
    var faqQuestion by remember { mutableStateOf("") }
    var faqAnswer by remember { mutableStateOf("") }
    var systemAnnTitle by remember { mutableStateOf("") }
    var systemAnnContent by remember { mutableStateOf("") }
    var broadcastNotificationText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feature toggles segment
        item {
            Text("🎚️ HOMEPAGE COMPONENT TOGGLES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                FeatureToggleRow("Services section", toggles["show_services"] == true) { viewModel.toggleFeature("show_services") }
                Divider(color = Color.Gray.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
                FeatureToggleRow("Live Statistics section", toggles["show_stats"] == true) { viewModel.toggleFeature("show_stats") }
                Divider(color = Color.Gray.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
                FeatureToggleRow("Testimonials block", toggles["show_testimonials"] == true) { viewModel.toggleFeature("show_testimonials") }
                Divider(color = Color.Gray.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
                FeatureToggleRow("Frequently Asked Questions", toggles["show_faq"] == true) { viewModel.toggleFeature("show_faq") }
                Divider(color = Color.Gray.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
                FeatureToggleRow("Support Contacts container", toggles["show_contact"] == true) { viewModel.toggleFeature("show_contact") }
            }
        }

        // Add FAQ segment
        item {
            Text("➕ FAQ MATRIX MANAGER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = faqQuestion,
                    onValueChange = { faqQuestion = it },
                    label = { Text("New FAQ Question") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                )
                OutlinedTextField(
                    value = faqAnswer,
                    onValueChange = { faqAnswer = it },
                    label = { Text("Answer payload details") },
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                )
                Button(
                    onClick = {
                        if (faqQuestion.isNotBlank() && faqAnswer.isNotBlank()) {
                            viewModel.addFaqAdmin(faqQuestion, faqAnswer)
                            faqQuestion = ""
                            faqAnswer = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    modifier = Modifier.fillMaxWidth().testTag("add_faq_button")
                ) {
                    Text("Append FAQ Card", color = Color.White)
                }
            }
        }

        // Add system announcement segment
        item {
            Text("📢 BROADCAST ANNOUNCEMENTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = systemAnnTitle,
                    onValueChange = { systemAnnTitle = it },
                    label = { Text("Announcement Heading") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                )
                OutlinedTextField(
                    value = systemAnnContent,
                    onValueChange = { systemAnnContent = it },
                    label = { Text("Announcement body text") },
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                )
                Button(
                    onClick = {
                        if (systemAnnTitle.isNotBlank() && systemAnnContent.isNotBlank()) {
                            viewModel.createAnnouncementAdmin(systemAnnTitle, systemAnnContent)
                            systemAnnTitle = ""
                            systemAnnContent = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    modifier = Modifier.fillMaxWidth().testTag("add_announcement_button")
                ) {
                    Text("Publish Announcement", color = Color.White)
                }
            }
        }

        // Broadcast notifications banner
        item {
            Text("🔔 DISPATCH SYSTEM-WIDE ALERTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = broadcastNotificationText,
                    onValueChange = { broadcastNotificationText = it },
                    placeholder = { Text("Type alert banner content...") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                )
                Button(
                    onClick = {
                        if (broadcastNotificationText.isNotBlank()) {
                            viewModel.sendAdminNotification(broadcastNotificationText)
                            broadcastNotificationText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    modifier = Modifier.fillMaxWidth().testTag("send_system_notification")
                ) {
                    Text("Broadcast Alert Now", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FeatureToggleRow(label: String, isActive: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        Switch(
            checked = isActive,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6366F1))
        )
    }
}

// --- SUB-VIEW 4: BACKUP & RESTORE DATA CONTROLS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataBackupView(
    viewModel: MarceHubViewModel,
    backupString: String,
    isDark: Boolean
) {
    var restoreInputString by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("🗄️ DATABASE BACKUP MATRIX", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Generate a full system backup of the Room database. The output can be stored safely or shared.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(
                    onClick = { viewModel.performBackup() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    modifier = Modifier.fillMaxWidth().testTag("generate_backup_button")
                ) {
                    Text("Trigger System Backup", color = Color.White)
                }

                if (backupString.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Backup Output:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = backupString,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 4.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                    )
                }
            }
        }

        item {
            Text("📤 RECONSTRUCT & RESTORE DATA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Paste a valid MarceHub backup string below to reconstruct database state instantly.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = restoreInputString,
                    onValueChange = { restoreInputString = it },
                    placeholder = { Text("Paste backup payload string here...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 12.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black)
                )
                Button(
                    onClick = {
                        if (restoreInputString.isNotBlank()) {
                            viewModel.performRestore(restoreInputString)
                            restoreInputString = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    modifier = Modifier.fillMaxWidth().testTag("trigger_restore_button")
                ) {
                    Text("Initiate Reconstruction Restore", color = Color.White)
                }
            }
        }
    }
}
