package com.trailmedic

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.trailmedic.ui.navigation.Screen
import com.trailmedic.ui.navigation.TrailMedicNavGraph
import com.trailmedic.ui.theme.DeepNavy
import com.trailmedic.ui.theme.TrailMedicTheme
import com.trailmedic.utils.SettingsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val keepScreenOn by settingsManager.keepScreenOn.collectAsState(initial = true)

            // Dynamic Keep Screen Awake Flag for emergency wilderness usage
            if (keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            TrailMedicTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    color = DeepNavy
                ) {
                    val navController = rememberNavController()
                    TrailMedicNavGraph(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    )
                }
            }
        }
    }
}
