package com.rjnsdev.easyfin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rjnsdev.easyfin.ui.navigation.EasyfinNavGraph
import com.rjnsdev.easyfin.ui.theme.EasyfinTheme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyfinTheme {
                val viewModel: MainViewModel = koinViewModel()
                val startDest by viewModel.startDestination.collectAsState()

                if (startDest == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    EasyfinNavGraph(startDestination = startDest!!)
                }
            }
        }
    }
}