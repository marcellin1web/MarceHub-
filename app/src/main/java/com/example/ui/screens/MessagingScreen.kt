package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.model.MessageEntity
import com.example.data.model.UserEntity
import com.example.ui.components.*
import com.example.data.model.*
import com.example.ui.viewmodel.MarceHubViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    viewModel: MarceHubViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeChatUser by viewModel.activeChatUser.collectAsState()
    val activeMessages by viewModel.activeChatMessages.collectAsState()
    
    var searchContactQuery by remember { mutableStateOf("") }
    var inputMessageText by remember { mutableStateOf("") }
    var attachedUrl by remember { mutableStateOf<String?>(null) }
    var attachedType by remember { mutableStateOf("NONE") } // "IMAGE", "VIDEO", "FILE", "NONE"

    val listState = rememberLazyListState()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Scroll to bottom when message list updates
    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            listState.animateScrollToItem(activeMessages.size - 1)
        }
    }

    if (currentUser == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Please login to access real-time messaging.", color = Color.Gray)
        }
        return
    }

    val self = currentUser!!

    // Filter contacts based on search query
    val filteredContacts = users.filter {
        it.email != self.email &&
        (it.username.contains(searchContactQuery, ignoreCase = true) || it.email.contains(searchContactQuery, ignoreCase = true))
    }

    Row(modifier = modifier.fillMaxSize()) {
        // LEFT PANE: User Directory List (Compact or Side list)
        if (activeChatUser == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("messaging_directory")
            ) {
                Text(
                    text = "💬 REAL-TIME MESSAGING CHANNELS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Search Box
                OutlinedTextField(
                    value = searchContactQuery,
                    onValueChange = { searchContactQuery = it },
                    placeholder = { Text("Search system users...", fontSize = 13.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("contact_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black
                    ),
                    singleLine = true
                )

                if (filteredContacts.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No contacts found in MarceHub directory.", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredContacts) { contact ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openChat(contact) }
                                    .testTag("contact_item_${contact.email}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = contact.profilePic,
                                        contentDescription = "Profile",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.Gray),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(contact.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (contact.isEmailVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text(contact.bio, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // RIGHT PANE: Active Chat Panel
            val chatUser = activeChatUser!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("active_chat_window")
            ) {
                // Chat Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { viewModel.openChat(null) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF6366F1))
                        }
                        
                        AsyncImage(
                            model = chatUser.profilePic,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(chatUser.username, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (chatUser.isEmailVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(text = "Country: ${chatUser.country} | Role: ${chatUser.role}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }

                // Chat Messages Feed
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeMessages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No communications yet. Send an invite message below!",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    } else {
                        items(activeMessages) { message ->
                            val isMe = message.senderId == self.email
                            ChatBubble(message = message, isMe = isMe, isDark = isDark)
                        }
                    }
                }

                // Media Attachment Preview Row
                if (attachedUrl != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red.copy(0.08f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (attachedType) {
                                    "IMAGE" -> Icons.Default.Image
                                    "VIDEO" -> Icons.Default.VideoCall
                                    else -> Icons.Default.AttachFile
                                },
                                contentDescription = null,
                                tint = Color(0xFFEC4899)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Attached simulated $attachedType asset",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        IconButton(onClick = {
                            attachedUrl = null
                            attachedType = "NONE"
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Message Input Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Attachment Triggers
                        IconButton(onClick = {
                            attachedUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=300&q=80"
                            attachedType = "IMAGE"
                        }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Attach Pic", tint = Color(0xFFEC4899))
                        }

                        IconButton(onClick = {
                            attachedUrl = "https://images.unsplash.com/photo-1634017839464-5c339ebe3cb4?auto=format&fit=crop&w=300&q=80"
                            attachedType = "VIDEO"
                        }) {
                            Icon(Icons.Default.VideoCall, contentDescription = "Attach Movie", tint = Color(0xFF8B5CF6))
                        }

                        IconButton(onClick = {
                            attachedUrl = "report_file.pdf"
                            attachedType = "FILE"
                        }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach Document", tint = Color(0xFF3B82F6))
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        OutlinedTextField(
                            value = inputMessageText,
                            onValueChange = { inputMessageText = it },
                            placeholder = { Text("Compose secure message...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("message_input_box"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputMessageText.isNotBlank() || attachedUrl != null) {
                                    viewModel.sendChatMessage(inputMessageText, attachedUrl, attachedType)
                                    inputMessageText = ""
                                    attachedUrl = null
                                    attachedType = "NONE"
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1))
                                .testTag("send_message_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: MessageEntity, isMe: Boolean, isDark: Boolean) {
    val bubbleColor = if (isMe) {
        Color(0xFF6366F1) // Indigo Primary
    } else {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    }

    val alignment = if (isMe) Alignment.End else Alignment.Start
    val shape = if (isMe) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    val textColor = if (isMe) {
        Color.White
    } else {
        if (isDark) Color.White else Color.Black
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("msg_bubble_${message.id}"),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
                .widthIn(max = 260.dp)
        ) {
            Column {
                if (message.content.isNotBlank()) {
                    Text(text = message.content, color = textColor, fontSize = 13.sp)
                }

                // Attachments Render
                if (message.mediaUrl != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    when (message.mediaType) {
                        "IMAGE" -> {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = "Received Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                        "VIDEO" -> {
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayCircleOutline, contentDescription = "Video preview", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                        "FILE" -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(message.mediaUrl.substringAfterLast("/"), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                fontSize = 8.sp,
                color = Color.Gray
            )
            if (isMe) {
                Spacer(modifier = Modifier.width(4.dp))
                // Simulated blue read double receipt tick
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Read status",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
