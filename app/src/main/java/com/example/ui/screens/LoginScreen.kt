package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.viewmodel.MarceHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MarceHubViewModel,
    modifier: Modifier = Modifier
) {
    var isSignUp by remember { mutableStateOf(false) }
    var isForgotMode by remember { mutableStateOf(false) }
    
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("******") } // Simulated password
    var country by remember { mutableStateOf("France") }
    var bio by remember { mutableStateOf("") }

    val stateMessage by viewModel.authStateMessage.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PremiumDarkGradient)
    ) {
        // Rotating elegant watermark background
        RotatingWatermark()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Branding Icon and Title
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Logo",
                tint = Color(0xFF8B5CF6),
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp)
            )
            
            Text(
                text = "MarceHub",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "The Premium Professional Social Ecosystem",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Auth Error or Status Message card
            AnimatedVisibility(
                visible = stateMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                stateMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1).copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .testTag("status_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Status", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearAuthMessage() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Glassmorphism login/signup container
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_container")
            ) {
                Text(
                    text = when {
                        isForgotMode -> "Forgot Password"
                        isSignUp -> "Create Premium Account"
                        else -> "Sign In to Hub"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Common Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.White.copy(alpha = 0.8f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Color(0xFF8B5CF6)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("email_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                if (!isForgotMode) {
                    if (isSignUp) {
                        // Username
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username", color = Color.White.copy(alpha = 0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User", tint = Color.White.copy(alpha = 0.8f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = Color(0xFF8B5CF6)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("username_input"),
                            singleLine = true
                        )

                        // Country
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Country", color = Color.White.copy(alpha = 0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = "Country", tint = Color.White.copy(alpha = 0.8f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = Color(0xFF8B5CF6)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("country_input"),
                            singleLine = true
                        )

                        // Bio
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio (Optional)", color = Color.White.copy(alpha = 0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Bio", tint = Color.White.copy(alpha = 0.8f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = Color(0xFF8B5CF6)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("bio_input"),
                            maxLines = 3
                        )
                    } else {
                        // Password (with masking visual transformation)
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = Color.White.copy(alpha = 0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color.White.copy(alpha = 0.8f)) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = Color(0xFF8B5CF6)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("password_input"),
                            singleLine = true
                        )
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        when {
                            isForgotMode -> {
                                viewModel.forgotPassword(email)
                                isForgotMode = false
                            }
                            isSignUp -> {
                                viewModel.signUp(email, username, country, bio)
                            }
                            else -> {
                                viewModel.login(email)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_button")
                ) {
                    Text(
                        text = when {
                            isForgotMode -> "Send Instructions"
                            isSignUp -> "Create Premium Access"
                            else -> "Log In"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Auth Mode Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUp) "Already have an account?" else "New to MarceHub?",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isSignUp) "Log In" else "Sign Up",
                        color = Color(0xFFEC4899),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable {
                                isSignUp = !isSignUp
                                isForgotMode = false
                                viewModel.clearAuthMessage()
                            }
                            .testTag("toggle_auth_mode")
                    )
                }

                if (!isSignUp) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isForgotMode) "Back to Login" else "Forgot password?",
                        color = Color(0xFF8B5CF6),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable {
                                isForgotMode = !isForgotMode
                                viewModel.clearAuthMessage()
                            }
                            .testTag("forgot_password_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Fast Switch Accounts for reviewers/testers
            Text(
                text = "⚡ QUICK AUTHENTICATION CHANNELS",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickUserBadge("Admin", "admin@marcehub.com", Color(0xFFEF4444)) {
                    email = "admin@marcehub.com"
                    viewModel.login("admin@marcehub.com")
                }
                QuickUserBadge("Moderator", "mod@marcehub.com", Color(0xFFF59E0B)) {
                    email = "mod@marcehub.com"
                    viewModel.login("mod@marcehub.com")
                }
                QuickUserBadge("User", "user@marcehub.com", Color(0xFF10B981)) {
                    email = "user@marcehub.com"
                    viewModel.login("user@marcehub.com")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickUserBadge(
    label: String,
    email: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("quick_${label.lowercase()}"),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, shape = androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(email.substringBefore("@"), color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
        }
    }
}
