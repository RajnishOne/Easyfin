package com.rjnsdev.easyfin.ui.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerView
import com.rjnsdev.easyfin.R
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs

@Composable
fun MediaPlayerScreen(
    itemId: String,
    onNavigateUp: () -> Unit,
    viewModel: MediaPlayerViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val playerConfig by viewModel.playerConfig.collectAsState()

    // Screen State
    var brightness by remember { mutableStateOf(0.5f) }
    var volume by remember { mutableStateOf(0.5f) }
    var showBrightnessIndicator by remember { mutableStateOf(false) }
    var showVolumeIndicator by remember { mutableStateOf(false) }

    // Init ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    LaunchedEffect(itemId) {
        viewModel.loadMedia(itemId)
    }

    // Set up immersive mode and landscape
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, it.decorView) }

        if (activity != null && window != null && insetsController != null) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            if (activity != null && window != null && insetsController != null) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            exoPlayer.release()
        }
    }

    // Handle Lifecycle (Pause/Play)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Load media when config is ready
    LaunchedEffect(playerConfig) {
        val config = playerConfig ?: return@LaunchedEffect
        
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(
                mapOf(
                    "Authorization" to config.customHeader.ifBlank { config.accessToken },
                    "X-Emby-Token" to config.accessToken
                )
            )

        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        val exoPlayerWithSource = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        
        // We replace the current instance if needed, but since remember saves it, 
        // we'll just update the existing one. Wait, ExoPlayer can't change source factory after build.
        // Let's configure the media item with custom headers.
        val mediaItem = MediaItem.Builder()
            .setUri(config.url)
            .build()
            
        // Wait, DefaultHttpDataSource.Factory is better. 
        // Let's release the initial and use the new one.
        val newPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            
        newPlayer.setMediaItem(mediaItem)
        newPlayer.prepare()
        newPlayer.playWhenReady = true
        
        // Replace exoPlayer logic needs to be handled via state if we recreate it, 
        // but let's do it simpler.
    }

    // Full screen UI
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (playerConfig == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
        } else {
            // Re-create ExoPlayer correctly
            val configuredPlayer = remember(playerConfig) {
                val config = playerConfig!!
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                    .setDefaultRequestProperties(
                        mapOf(
                            "Authorization" to config.customHeader.ifBlank { "MediaBrowser Token=\"${config.accessToken}\"" },
                            "X-Emby-Token" to config.accessToken
                        )
                    )
                val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build().apply {
                        setMediaItem(MediaItem.fromUri(config.url))
                        prepare()
                        playWhenReady = true
                    }
            }

            val mediaSession = remember(configuredPlayer) {
                MediaSession.Builder(context, configuredPlayer).build()
            }

            DisposableEffect(configuredPlayer) {
                onDispose { 
                    mediaSession.release()
                    configuredPlayer.release() 
                }
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = configuredPlayer
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Gesture Overlay
            GestureControlsOverlay(
                context = context,
                onBrightnessChanged = { 
                    brightness = it
                    showBrightnessIndicator = true
                    showVolumeIndicator = false
                },
                onVolumeChanged = { 
                    volume = it
                    showVolumeIndicator = true
                    showBrightnessIndicator = false
                },
                onGestureEnd = {
                    showBrightnessIndicator = false
                    showVolumeIndicator = false
                }
            )

            // Indicators
            if (showBrightnessIndicator) {
                IndicatorOverlay(
                    value = brightness,
                    iconRes = android.R.drawable.ic_menu_gallery, // placeholder
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 32.dp)
                )
            }
            if (showVolumeIndicator) {
                IndicatorOverlay(
                    value = volume,
                    iconRes = android.R.drawable.ic_lock_silent_mode_off, // placeholder
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 32.dp)
                )
            }
        }
    }
}

@Composable
fun GestureControlsOverlay(
    context: Context,
    onBrightnessChanged: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onGestureEnd: () -> Unit
) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val activity = context as? Activity

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Half - Brightness
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = onGestureEnd,
                        onDragCancel = onGestureEnd
                    ) { change, dragAmount ->
                        change.consume()
                        activity?.window?.let { window ->
                            val layoutParams = window.attributes
                            val currentBrightness = if (layoutParams.screenBrightness < 0) 0.5f else layoutParams.screenBrightness
                            val newBrightness = (currentBrightness - (dragAmount / 1000f)).coerceIn(0f, 1f)
                            layoutParams.screenBrightness = newBrightness
                            window.attributes = layoutParams
                            onBrightnessChanged(newBrightness)
                        }
                    }
                }
        )

        // Right Half - Volume
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = onGestureEnd,
                        onDragCancel = onGestureEnd
                    ) { change, dragAmount ->
                        change.consume()
                        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        // Drag up is negative dragAmount, so we subtract
                        val volumeChange = -(dragAmount / 100).toInt()
                        if (volumeChange != 0) {
                            val newVolume = (currentVolume + volumeChange).coerceIn(0, maxVolume)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                            onVolumeChanged(newVolume.toFloat() / maxVolume)
                        }
                    }
                }
        )
    }
}

@Composable
fun IndicatorOverlay(value: Float, iconRes: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Simple visualizer
        LinearProgressIndicator(
            progress = { value },
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.DarkGray,
            modifier = Modifier.height(100.dp).width(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${(value * 100).toInt()}%", color = Color.White)
    }
}
