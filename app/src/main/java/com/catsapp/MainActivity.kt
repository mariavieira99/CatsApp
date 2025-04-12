package com.catsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.catsapp.ui.cats.CatsListScreen
import com.catsapp.ui.theme.CatsAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatsAppTheme {
                CatsApp()
            }
        }
    }
}

@Composable
private fun CatsApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        CatsListScreen(innerPadding)
    }
}