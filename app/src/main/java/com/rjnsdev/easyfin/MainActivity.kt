package com.rjnsdev.easyfin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rjnsdev.easyfin.ui.navigation.EasyfinNavGraph
import com.rjnsdev.easyfin.ui.theme.EasyfinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyfinTheme {
                EasyfinNavGraph()
            }
        }
    }
}