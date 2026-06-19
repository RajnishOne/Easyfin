package com.rjnsdev.easyfin.ui.dashboard.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rjnsdev.easyfin.data.remote.BaseItemDto
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(viewModel: ExploreViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Explore", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = Color.White
            )
        )

        when (val state = uiState) {
            is ExploreState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is ExploreState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ExploreState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(state.latestMediaRows) { row ->
                        MediaCarousel(title = row.title, items = row.items, serverUrl = state.serverUrl)
                    }
                }
            }
        }
    }
}

@Composable
fun MediaCarousel(title: String, items: List<BaseItemDto>, serverUrl: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                MediaPosterCard(item = item, serverUrl = serverUrl)
            }
        }
    }
}

@Composable
fun MediaPosterCard(item: BaseItemDto, serverUrl: String) {
    // Basic image path builder.
    val imageUrl = if (item.ImageTags?.containsKey("Primary") == true) {
        "${serverUrl}/Items/${item.Id}/Images/Primary"
    } else null

    Card(
        modifier = Modifier
            .width(120.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { /* TODO: Navigate to detail/player */ },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.Name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                    Text(item.Name ?: "Unknown", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
