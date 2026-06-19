package com.rjnsdev.easyfin.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.rjnsdev.easyfin.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var customHeader by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    // A subtle gradient background as seen in "Cinematic Noir"
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            Color(0xFF1E112A) // subtle dark purple at the bottom
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // App Logo
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Easyfin",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Your Jellyfin Library, Simplified.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    Text("Jellyfin Server URL", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = null, tint = Color.Gray) },
                        placeholder = { Text("https://jellyfin.example.com", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF2C2C2C),
                            focusedContainerColor = Color(0xFF2C2C2C),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        )
                    )

                    Text("Username", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray) },
                        placeholder = { Text("Enter username", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF2C2C2C),
                            focusedContainerColor = Color(0xFF2C2C2C),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        )
                    )

                    Text("Password", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility",
                                    tint = Color.Gray
                                )
                            }
                        },
                        placeholder = { Text("Enter password", color = Color.Gray) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF2C2C2C),
                            focusedContainerColor = Color(0xFF2C2C2C),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvanced = !showAdvanced }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (showAdvanced) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Advanced Options",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Advanced Options",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (showAdvanced) {
                        OutlinedTextField(
                            value = customHeader,
                            onValueChange = { customHeader = it },
                            placeholder = { Text("Custom Header (e.g. X-Proxy-User: user)", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFF2C2C2C),
                                focusedContainerColor = Color(0xFF2C2C2C),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            )
                        )
                    }

                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.login(serverUrl, username, password, customHeader) },
                        enabled = authState !is AuthState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { /* TODO: Test Connection */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Filled.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Connection", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
