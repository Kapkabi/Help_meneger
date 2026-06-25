package com.kapkabi.helpmeneger

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kapkabi.helpmeneger.ui.navigation.HelpMenegerNavHost
import com.kapkabi.helpmeneger.ui.theme.HelpMenegerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelpMenegerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HelpMenegerNavHost()
                }
            }
        }
    }
}
