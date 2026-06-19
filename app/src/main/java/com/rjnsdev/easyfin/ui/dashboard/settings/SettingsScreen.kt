package com.rjnsdev.easyfin.ui.dashboard.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogoutAll: () -> Unit,
    onAddNewServer: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val profiles by viewModel.serverProfiles.collectAsState()
    val activeId by viewModel.activeServerId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Manage Servers",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            items(profiles) { profile ->
                val isActive = profile.id == activeId
                ServerCard(
                    profileName = profile.name,
                    serverUrl = profile.url,
                    username = profile.username,
                    isActive = isActive,
                    onClick = { if (!isActive) viewModel.switchServer(profile.id) },
                    onDelete = { viewModel.deleteServer(profile.id) }
                )
            }

            item {
                OutlinedButton(
                    onClick = onAddNewServer,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Server")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Server")
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        viewModel.logoutAll()
                        onLogoutAll()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout All Servers", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ServerCard(
    profileName: String,
    serverUrl: String,
    username: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF1E1E1E)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profileName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "$username @ $serverUrl",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
            
            if (isActive) {
                Icon(Icons.Filled.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.secondary)
            } else {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
