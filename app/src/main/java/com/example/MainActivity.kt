package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.game.GameViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            com.google.android.gms.ads.MobileAds.initialize(this) {}
            com.example.game.AdManager.loadAd(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                val viewModel: GameViewModel = viewModel()
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onLoadingFinished = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            val progress by viewModel.userProgress.collectAsState()
                            HomeScreen(
                                progress = progress,
                                onAdventurePressed = { navController.navigate("map") },
                                onCollectionPressed = { navController.navigate("collection") },
                                onShopPressed = { navController.navigate("shop") },
                                onSettingsPressed = { navController.navigate("settings") },
                                viewModel = viewModel
                            )
                        }
                        composable("map") {
                            val levelsProgress by viewModel.levelsProgress.collectAsState()
                            WorldMapScreen(
                                levels = levelsProgress,
                                onLevelSelected = { lvlId ->
                                    viewModel.startLevel(lvlId)
                                    navController.navigate("game")
                                },
                                onBackPressed = { navController.navigateUp() }
                            )
                        }
                        composable("game") {
                            GameplayScreen(
                                viewModel = viewModel,
                                onBackToMapPressed = { navController.navigateUp() }
                            )
                        }
                        composable("collection") {
                            val companionList by viewModel.companions.collectAsState()
                            val progress by viewModel.userProgress.collectAsState()
                            CollectionScreen(
                                companions = companionList,
                                userProgress = progress,
                                onCompanionSelected = { id -> viewModel.selectCompanionCompanion(id) },
                                onEvolvePressed = { id -> viewModel.evolveCompanion(id, companionList) },
                                onHatchPressed = { id -> viewModel.hatchEgg(id) },
                                onBackPressed = { navController.navigateUp() }
                            )
                        }
                        composable("shop") {
                            val progress by viewModel.userProgress.collectAsState()
                            ShopScreen(
                                userProgress = progress,
                                viewModel = viewModel,
                                onBackPressed = { navController.navigateUp() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBackPressed = { navController.navigateUp() },
                                onResetProgress = { viewModel.wipeAndResetAllProgress() }
                            )
                        }
                    }
                }
            }
        }
    }
}
