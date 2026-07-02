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
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.viewmodel.MarceHubViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MarceHubViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.allPosts.collectAsState()
    val announcements by viewModel.allAnnouncements.collectAsState()
    val faqs by viewModel.allFaqs.collectAsState()
    val toggles by viewModel.featureToggles.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.allUsers.collectAsState()

    // Post creation inputs
    var postContent by remember { mutableStateOf("") }
    var selectedMediaUrl by remember { mutableStateOf<String?>(null) }
    var selectedMediaType by remember { mutableStateOf("NONE") }

    // Contact inputs
    var contactName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactMessage by remember { mutableStateOf("") }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_feed_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. HERO SECTION ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFF8B5CF6))
                        )
                    )
                    .padding(24.dp)
                    .testTag("hero_section")
            ) {
                Column {
                    Text(
                        text = "EXPLORE THE NEXT GEN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Empowering Professional Social Synergy",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 34.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Build real-time channels, manage dynamic user authorization, customize system matrices, and secure professional connections.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.selectTab("messages") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Connect Instantly", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // --- 2. HERO ANNOUNCEMENTS (FEATURED CONTENT) ---
        if (announcements.isNotEmpty()) {
            item {
                Text(
                    text = "📢 FEATURED NEWS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            items(announcements.take(2)) { ann ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("announcement_item_${ann.id}")
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Campaign, contentDescription = "Announcement", tint = Color(0xFFEC4899), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(ann.title, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ann.content, color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(ann.timestamp)),
                                fontSize = 10.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. DYNAMIC SERVICES (TOGGLEABLE) ---
        if (toggles["show_services"] == true) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("services_section")
                ) {
                    Text(
                        text = "🛠️ OUR PREMIUM SERVICES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ServiceCard("Cloud Storage", Icons.Default.CloudQueue, Modifier.weight(1f))
                        ServiceCard("Encrypted Chats", Icons.Default.Forum, Modifier.weight(1f))
                        ServiceCard("Admin Controls", Icons.Default.AdminPanelSettings, Modifier.weight(1f))
                    }
                }
            }
        }

        // --- 4. SYSTEM STATISTICS (TOGGLEABLE) ---
        if (toggles["show_stats"] == true) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("stats_section")
                ) {
                    Text(
                        text = "📊 LIVE ECOSYSTEM METRICS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBadge("Ecosystem Users", "${users.size}", Icons.Default.PeopleAlt, Modifier.weight(1f))
                        StatBadge("Global Posts", "${posts.size}", Icons.Default.DynamicFeed, Modifier.weight(1f))
                    }
                }
            }
        }

        // --- 5. SOCIAL WORKSPACE: CREATE NEW POST ---
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_creator_card")
            ) {
                Text("Share your professional update", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = postContent,
                    onValueChange = { postContent = it },
                    placeholder = { Text("What's on your mind?", color = if (isDark) Color.White.copy(0.5f) else Color.Black.copy(0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("post_creator_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f),
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                // Simulate selecting a premium gradient illustration as post media
                                selectedMediaUrl = "https://images.unsplash.com/photo-1634017839464-5c339ebe3cb4?auto=format&fit=crop&w=500&q=80"
                                selectedMediaType = "IMAGE"
                            },
                            modifier = Modifier.testTag("post_add_photo")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add image", tint = Color(0xFFEC4899))
                        }
                        IconButton(
                            onClick = {
                                selectedMediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=500&q=80"
                                selectedMediaType = "VIDEO" // represent mock simulation
                            },
                            modifier = Modifier.testTag("post_add_video")
                        ) {
                            Icon(Icons.Default.VideoCall, contentDescription = "Add video simulation", tint = Color(0xFF8B5CF6))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedMediaUrl != null) {
                            Text(
                                text = "Media Selected",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            IconButton(onClick = {
                                selectedMediaUrl = null
                                selectedMediaType = "NONE"
                            }) {
                                Icon(Icons.Default.Cancel, contentDescription = "Clear media", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                        Button(
                            onClick = {
                                if (postContent.isNotBlank() || selectedMediaUrl != null) {
                                    viewModel.publishPost(postContent, selectedMediaUrl, selectedMediaType)
                                    postContent = ""
                                    selectedMediaUrl = null
                                    selectedMediaType = "NONE"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            modifier = Modifier.testTag("post_publish_button")
                        ) {
                            Text("Publish", color = Color.White)
                        }
                    }
                }
            }
        }

        // --- 6. POST FEED ---
        item {
            Text(
                text = "💬 RECENT DISCUSSION FEED",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        if (posts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.DynamicFeed, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active posts yet on the hub.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(posts) { post ->
                PostItemView(post, viewModel, isDark)
            }
        }

        // --- 7. TESTIMONIALS SLIDER (TOGGLEABLE) ---
        if (toggles["show_testimonials"] == true) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("testimonials_section")
                ) {
                    Text(
                        text = "⭐ TRUSTED TESTIMONIALS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "\"MarceHub has completely streamlined how our technical team shares architectural updates. The real-time messaging latency is incredibly low, and role-based permissions work flawlessly.\"",
                                fontSize = 13.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEC4899))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Elena Rostova", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDark) Color.White else Color.Black)
                                    Text("Lead Infrastructure Architect", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 8. FAQ ACCORDION (TOGGLEABLE) ---
        if (toggles["show_faq"] == true && faqs.isNotEmpty()) {
            item {
                Text(
                    text = "❓ FREQUENTLY ASKED QUESTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 12.dp)
                )
            }
            items(faqs) { faq ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .testTag("faq_item_${faq.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(faq.question, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color(0xFF6366F1)
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.1f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(faq.answer, color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- 9. SUPPORT & CONTACT (TOGGLEABLE) ---
        if (toggles["show_contact"] == true) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("contact_section")
                ) {
                    Text("📬 Connect with Support & Inquiry", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Direct Support Cards (Email & WhatsApp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Email Support Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF6366F1).copy(alpha = 0.15f) else Color(0xFF6366F1).copy(alpha = 0.08f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    try {
                                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:support.marcehub@gmail.com")
                                            putExtra(Intent.EXTRA_SUBJECT, "MarceHub Support Request")
                                        }
                                        context.startActivity(emailIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open email app.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .testTag("support_email_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Support",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "E-mail",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "support.marcehub@gmail.com",
                                    fontSize = 10.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // WhatsApp Support Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.08f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    try {
                                        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("https://api.whatsapp.com/send?phone=2250701820663")
                                        }
                                        context.startActivity(whatsappIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open WhatsApp.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .testTag("support_whatsapp_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "WhatsApp Support",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "WhatsApp",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+225 0701820663",
                                    fontSize = 10.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Text(
                        text = "Or submit an inquiry ticket:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("Your Name", color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("contact_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedTextColor = if (isDark) Color.White else Color.Black
                        )
                    )

                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("Email address", color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("contact_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedTextColor = if (isDark) Color.White else Color.Black
                        )
                    )

                    OutlinedTextField(
                        value = contactMessage,
                        onValueChange = { contactMessage = it },
                        label = { Text("How can we assist you?", color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(bottom = 12.dp)
                            .testTag("contact_message_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedTextColor = if (isDark) Color.White else Color.Black
                        )
                    )

                    Button(
                        onClick = {
                            viewModel.submitContact(contactName, contactEmail, contactMessage)
                            contactName = ""
                            contactEmail = ""
                            contactMessage = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_submit_button")
                    ) {
                        Text("Send Support Message", color = Color.White)
                    }
                }
            }
        }

        // --- 10. PLATFORM FOOTER ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .testTag("footer_section"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "© 2026 MarceHub Ecosystem. All rights reserved.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Ecosystem Time: UTC 2026-07-01 10:35:50",
                    fontSize = 10.sp,
                    color = Color.Gray.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ServiceCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StatBadge(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text(label, fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun PostItemView(
    post: PostEntity,
    viewModel: MarceHubViewModel,
    isDark: Boolean
) {
    var commentText by remember { mutableStateOf("") }
    var viewComments by remember { mutableStateOf(false) }
    
    val comments by viewModel.getCommentsForPost(post.id).collectAsState(initial = emptyList())
    val currentUser by viewModel.currentUser.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_item_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Author details & Follow option
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.authorProfilePic,
                    contentDescription = "Profile Pic",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("@${post.authorName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(post.timestamp)),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                // Follow button (simulated toggle if not self)
                if (currentUser != null && currentUser?.email != post.authorId) {
                    IconButton(
                        onClick = { viewModel.toggleFollowUser(post.authorId) },
                        modifier = Modifier.testTag("post_follow_author_${post.id}")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Follow/Unfollow", tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                    }
                }

                // Delete Post if Admin or Author
                if (currentUser != null && (currentUser?.role == "Admin" || currentUser?.email == post.authorId)) {
                    IconButton(
                        onClick = { viewModel.deletePost(post.id) },
                        modifier = Modifier.testTag("post_delete_${post.id}")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Post", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            // Content text
            Text(post.content, fontSize = 13.sp, lineHeight = 18.sp)

            // Post Media (Image/Video mock render)
            if (post.mediaUrl != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray)
                ) {
                    AsyncImage(
                        model = post.mediaUrl,
                        contentDescription = "Post Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Video indicator overlay
                    if (post.mediaType == "VIDEO") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayCircleFilled, contentDescription = "Video", tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = if (isDark) Color.White.copy(0.08f) else Color.Black.copy(0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Like, Comment, Report, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.toggleLikePost(post.id) }
                        .testTag("post_like_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = Color(0xFFEC4899),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.likesCount}", fontSize = 12.sp)
                }

                // Comment Trigger
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewComments = !viewComments }
                        .testTag("post_comment_trigger_${post.id}")
                ) {
                    Icon(Icons.Outlined.Comment, contentDescription = "Comment", tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.commentsCount}", fontSize = 12.sp)
                }

                // Report content
                IconButton(
                    onClick = { viewModel.reportPost(post.id) },
                    modifier = Modifier.testTag("post_report_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Report",
                        tint = if (post.isReported) Color.Red else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Share (copy clipboard simulated alert)
                IconButton(
                    onClick = { viewModel.sendAdminNotification("Post shared by user: ${post.id}") },
                    modifier = Modifier.testTag("post_share_${post.id}")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share link", tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                }
            }

            // Expanded comments panel
            AnimatedVisibility(visible = viewComments) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = if (isDark) Color.White.copy(0.08f) else Color.Black.copy(0.08f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Comments list
                    comments.forEach { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AsyncImage(
                                model = comment.authorProfilePic,
                                contentDescription = "Author Pic",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("@${comment.authorName}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(comment.timestamp)),
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(comment.content, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.85f))
                            }
                            // Delete comment if author or admin
                            if (currentUser != null && (currentUser?.role == "Admin" || currentUser?.email == comment.authorId)) {
                                IconButton(
                                    onClick = { viewModel.deleteComment(comment.id, post.id) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete Comment", tint = Color.Red, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Add new comment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Write a comment...", fontSize = 12.sp, color = Color.Gray) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("post_comment_input_${post.id}"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.addComment(post.id, commentText)
                                    commentText = ""
                                }
                            },
                            modifier = Modifier.testTag("post_comment_submit_${post.id}")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Submit", tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
