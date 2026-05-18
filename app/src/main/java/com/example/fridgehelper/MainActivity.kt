package com.example.fridgehelper
// main activ aplikacji – uruchamia Compose i startuje cały interfejs użytkownika

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import com.example.fridgehelper.ui.FridgeNavHost
import dagger.hilt.android.AndroidEntryPoint
import com.example.fridgehelper.ui.theme.FridgeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // jawna zgoda uzytkownika na powiadomienia
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }

        // status bar w kolorze topbara (#1A3D2B)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.parseColor("#1A3D2B")),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.WHITE,
                android.graphics.Color.WHITE
            )
        )
        setContent {
            FridgeTheme {
                FridgeNavHost()
            }
        }
    }
}