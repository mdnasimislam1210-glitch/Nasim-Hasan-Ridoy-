package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.CreatureCompanion
import com.example.data.LevelProgress
import com.example.data.UserProgress
import com.example.game.CreatureType
import com.example.game.GameViewModel
import com.example.game.MatchPiece
import com.example.game.SpecialType
import com.example.ui.components.CreatureBallRenderer
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.roundToInt

// ==========================================
// SHARED BACKGROUND COMPONENT: ANIMATED LAVA CHREMS
// ==========================================
@Composable
fun LavaBackground(content: @Composable BoxScope.() -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_lava")
    val lavaGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "lava_glow"
    )

    // Lava Ash Particles Simulation
    val particles = remember {
        List(15) {
            mutableStateOf(
                Triple(
                    (0..100).random().toFloat() / 100f, // random x
                    (0..100).random().toFloat() / 100f, // random y
                    (5..15).random().toFloat()           // size
                )
            )
        }
    }

    // Move particles upwards periodically
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            particles.forEach { p ->
                val current = p.value
                var nextY = current.second - 0.008f
                if (nextY < 0) {
                    nextY = 1.0f
                }
                p.value = Triple(current.first, nextY, current.third)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBlack,
                        Color(0xFF140102),
                        Color(0xFF280104) * lavaGlow
                    )
                )
            )
    ) {
        // Draw fire ash particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val valState = p.value
                drawCircle(
                    color = Color(0xFFFF5722),
                    radius = valState.third,
                    center = Offset(valState.first * size.width, valState.second * size.height),
                    alpha = 0.35f
                )
            }
        }

        content()
    }
}

operator fun Color.times(factor: Float): Color {
    return Color(
        red = (this.red * factor).coerceIn(0f, 1f),
        green = (this.green * factor).coerceIn(0f, 1f),
        blue = (this.blue * factor).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(onLoadingFinished: () -> Unit) {
    var loadingProgress by remember { mutableStateOf(0f) }
    var captionText by remember { mutableStateOf("Wakening sleeping baby dragons...") }

    LaunchedEffect(Unit) {
        val captions = listOf(
            "Incubating fire dragon eggs...",
            "Polishing gold cobra crowns...",
            "Freezing crystal kitten mountains...",
            "Gathering void shadows...",
            "Stirring crimson magma lakes...",
            "Unleasining DragonBlitz powers..."
        )
        // Loading animation loop
        for (i in 1..40) {
            delay(100)
            loadingProgress = i / 40f
            if (i % 8 == 0) {
                captionText = captions[(i / 8) % captions.size]
            }
        }
        onLoadingFinished()
    }

    LavaBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated breathing custom app logo card
            val infiniteTrans = rememberInfiniteTransition(label = "logo_breathe")
            val logoScale by infiniteTrans.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "logo_breath"
            )

            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(3.dp, Brush.horizontalGradient(listOf(AmberGold, NeonRedRune)), RoundedCornerShape(32.dp))
                    .background(DarkLava)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_dragon_logo),
                    contentDescription = "Dragon Head logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Game Heading
            Text(
                text = "DRAGONBLITZ",
                style = Typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = NeonRedRune,
                    shadow = Shadow(
                        color = AmberGold,
                        offset = Offset(3f, 3f),
                        blurRadius = 8f
                    ),
                    letterSpacing = 2.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "EPIC DEFENDERS OF DUNGEONS",
                style = Typography.labelLarge.copy(
                    color = AmberGold,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Loading state details
            Text(
                text = captionText,
                style = Typography.bodyMedium.copy(color = MutedSlate),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Linear Progress indicators
            LinearProgressIndicator(
                progress = { loadingProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = NeonRedRune,
                trackColor = PanelGray,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${(loadingProgress * 100).toInt()}%",
                style = Typography.labelLarge.copy(color = AmberGold),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// 2. HOME SCREEN (AND SHOP, MAP, STATS TAB INTERFACE)
// ==========================================
@Composable
fun HomeScreen(
    progress: UserProgress?,
    onAdventurePressed: () -> Unit,
    onCollectionPressed: () -> Unit,
    onShopPressed: () -> Unit,
    onSettingsPressed: () -> Unit,
    viewModel: GameViewModel
) {
    val showLuckyWheel = remember { mutableStateOf(false) }
    val showDailyStreak = remember { mutableStateOf(false) }

    LavaBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP STATUS BADGE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold Coin box
                Card(
                    colors = CardDefaults.cardColors(containerColor = PanelGray),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.border(1.dp, BloodCrimson, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Gold Coins",
                            tint = AmberGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${progress?.coins ?: 0}",
                            style = Typography.titleMedium.copy(color = TextWhite),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // VIP Badge
                if (progress?.vipActive == true) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkLava),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.border(1.dp, AmberGold, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "VIP Active",
                                tint = AmberGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VIP MEMBER",
                                style = Typography.labelLarge.copy(color = AmberGold, fontSize = 11.sp)
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onShopPressed,
                        border = BorderStroke(1.dp, AmberGold),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = "Get VIP", modifier = Modifier.size(16.dp), tint = AmberGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("BUY VIP", fontSize = 11.sp, style = Typography.labelLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // BIG BREATHING HERO COMPANION IMAGE (3D looking)
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(BloodCrimson * 1.5f, Color.Transparent),
                            radius = 350f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Draws our beautiful Fire Dragon Pyro head centered here
                Image(
                    painter = painterResource(id = R.drawable.ic_dragon_logo),
                    contentDescription = "Pyro",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .border(4.dp, AmberGold, CircleShape)
                )

                // Level badge superimposed
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(AmberGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    val comp = viewModel.activeCompanion.collectAsState().value
                    Text(
                        text = "COMPANION: ${comp?.name ?: "Pyro"} (STG ${comp?.evolutionStage ?: 1})",
                        style = Typography.labelLarge.copy(color = ObsidianBlack, fontSize = 11.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // PLAY BUTTON: ADVENTURE MODE
            Button(
                onClick = onAdventurePressed,
                colors = ButtonDefaults.buttonColors(containerColor = NeonRedRune),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                border = BorderStroke(2.dp, AmberGold)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Adventure",
                    tint = TextWhite,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ENTER DUNGEONS",
                    style = Typography.displayMedium.copy(
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PRIMARY SUBMENUS: (Creatures, Wheel, Streak, Shop)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Creatures list
                Button(
                    onClick = onCollectionPressed,
                    colors = ButtonDefaults.buttonColors(containerColor = PanelGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(1.dp, BloodCrimson, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Pets, contentDescription = "Collection", tint = NeonRedRune)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CREATURES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }

                // 2. Lucky wheel
                Button(
                    onClick = { showLuckyWheel.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PanelGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(1.dp, BloodCrimson, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Autorenew, contentDescription = "Lucky Wheel", tint = AmberGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("LUCKY SPIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 3. Shop items
                Button(
                    onClick = onShopPressed,
                    colors = ButtonDefaults.buttonColors(containerColor = PanelGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(1.dp, BloodCrimson, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Shop", tint = AmberGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BREEDER SHOP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }

                // 4. Daily Streak Check
                Button(
                    onClick = { showDailyStreak.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PanelGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(1.dp, BloodCrimson, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Daily Streak", tint = NeonRedRune)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("STREAK LOGIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings button
            IconButton(
                onClick = onSettingsPressed,
                modifier = Modifier
                    .size(48.dp)
                    .background(PanelGray, CircleShape)
                    .border(1.dp, BloodCrimson, CircleShape)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextWhite)
            }
        }

        // ==========================================
        // LUCKY SPINNING WHEEL SYSTEM DIALOG
        // ==========================================
        if (showLuckyWheel.value) {
            Dialog(onDismissRequest = { showLuckyWheel.value = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PanelGray),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .border(2.dp, AmberGold, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "LUCKY WHEEL",
                            style = Typography.displayMedium.copy(color = AmberGold, fontSize = 22.sp),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Spin daily for free dragon coins!",
                            style = Typography.bodyMedium.copy(color = MutedSlate),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Drawing Wheel graphics which rotates of degrees
                        val rotationDegree by viewModel.wheelSelectionDegrees.collectAsState()
                        val animatedRotation by animateFloatAsState(
                            targetValue = rotationDegree,
                            animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
                            label = "wheel"
                        )

                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .rotate(animatedRotation)
                                .border(8.dp, BloodCrimson, CircleShape)
                                .background(DarkLava, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Sector spokes drawing inside Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = size.minDimension / 2
                                val center = Offset(size.width / 2, size.height / 2)
                                val colors = listOf(Color(0xFF210003), Color(0xFF5D0009), Color(0xFF420006), Color(0xFF8D010C), Color(0xFF160002), Color(0xFFC60012))
                                for (i in 0 until 6) {
                                    drawArc(
                                        color = colors[i],
                                        startAngle = i * 60f,
                                        sweepAngle = 60f,
                                        useCenter = true,
                                        size = size
                                    )
                                }
                            }

                            // Items markers
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text("50G", color = Color.White, modifier = Modifier.align(Alignment.TopCenter).padding(16.dp), fontWeight = FontWeight.Bold)
                                Text("150G", color = Color.White, modifier = Modifier.align(Alignment.CenterEnd).padding(16.dp), fontWeight = FontWeight.Bold)
                                Text("300G", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), fontWeight = FontWeight.Bold)
                                Text("0G", color = Color.White, modifier = Modifier.align(Alignment.CenterStart).padding(16.dp), fontWeight = FontWeight.Bold)
                            }

                            // Center pin pointer node
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AmberGold, CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Pointer", tint = ObsidianBlack, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val isWheelSpinning by viewModel.isWheelSpinning.collectAsState()
                        Button(
                            onClick = { viewModel.spinLuckyWheel() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRedRune),
                            enabled = !isWheelSpinning,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(if (isWheelSpinning) "SPINNING..." else "SPIN FOR 100 COINS", style = Typography.labelLarge, color = TextWhite)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = { showLuckyWheel.value = false }) {
                            Text("CLOSE", color = MutedSlate, style = Typography.labelLarge)
                        }
                    }
                }
            }
        }

        // ==========================================
        // DAILY LOGIN REWARD POPUP
        // ==========================================
        if (showDailyStreak.value) {
            Dialog(onDismissRequest = { showDailyStreak.value = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PanelGray),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .border(2.dp, NeonRedRune, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "DAILY REWARDS",
                            style = Typography.displayMedium.copy(color = NeonRedRune, fontSize = 22.sp),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Claim coin multiplier matching your streak!",
                            style = Typography.bodyMedium.copy(color = MutedSlate),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        val currentStreak = progress?.dailyStreak ?: 1
                        // List days 1..5 matching chest progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (day in 1..4) {
                                val activeDay = day <= currentStreak
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                        .background(
                                            if (activeDay) DarkLava else Color.Black,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = if (day == currentStreak) 2.dp else 1.dp,
                                            color = if (day == currentStreak) AmberGold else BloodCrimson,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(8.rbValue())
                                ) {
                                    Text("Day $day", fontSize = 10.sp, color = MutedSlate)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Icon(
                                        imageVector = if (day < currentStreak) Icons.Default.CheckCircle else Icons.Default.MonetizationOn,
                                        contentDescription = "Day $day Status",
                                        tint = if (day < currentStreak) HealthGreen else AmberGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${day * 100}G", fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.claimDailyReward(currentStreak)
                                showDailyStreak.value = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRedRune),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("CLAIM DAY $currentStreak GIFT: ${currentStreak * 100} COINS", style = Typography.labelLarge, color = TextWhite, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = { showDailyStreak.value = false }) {
                            Text("BACK", color = MutedSlate, style = Typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

private fun Int.rbValue(): PaddingValues = PaddingValues(this.dp)

// ==========================================
// 3. WORLD MAP & LEVEL SELECTION
// ==========================================
@Composable
fun WorldMapScreen(
    levels: List<LevelProgress>,
    onLevelSelected: (Int) -> Unit,
    onBackPressed: () -> Unit
) {
    LavaBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PanelGray, CircleShape)
                        .border(1.dp, BloodCrimson, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return", tint = TextWhite)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "CAMPAIGN DUNGEON MAP",
                    style = Typography.titleLarge.copy(color = NeonRedRune, fontSize = 18.sp),
                    fontWeight = FontWeight.Bold
                )
            }

            // Path Scroll list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
            ) {
                // Levels Pins
                items(10) { idx ->
                    val lvlNum = idx + 1
                    val levelProgressObj = levels.find { it.levelId == lvlNum }
                    val unlocked = levelProgressObj != null || lvlNum == 1
                    val stars = levelProgressObj?.stars ?: 0
                    val isBoss = lvlNum == 5 || lvlNum == 10

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (unlocked) PanelGray else Color(0xFF0F0708))
                            .border(
                                1.dp,
                                if (isBoss && unlocked) NeonRedRune else if (unlocked) BloodCrimson else Color(0xFF2C191C),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable(enabled = unlocked) { onLevelSelected(lvlNum) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Badge node
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        if (isBoss && unlocked) NeonRedRune else if (unlocked) DarkLava else Color.Black,
                                        CircleShape
                                    )
                                    .border(
                                        2.dp,
                                        if (isBoss) AmberGold else if (unlocked) NeonRedRune else Color.DarkGray,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (unlocked) {
                                    if (isBoss) {
                                        Icon(Icons.Default.Whatshot, contentDescription = "Boss Level", tint = AmberGold, modifier = Modifier.size(28.dp))
                                    } else {
                                        Text(
                                            text = "$lvlNum",
                                            style = Typography.displayMedium.copy(color = TextWhite, fontSize = 20.sp),
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.DarkGray)
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = if (isBoss) "BOSS CAGE: LEVEL $lvlNum" else "DUNGEON CELL $lvlNum",
                                    style = Typography.titleMedium.copy(
                                        color = if (unlocked) TextWhite else Color.Gray,
                                        fontSize = 15.sp
                                    ),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (lvlNum) {
                                        1 -> "Pyro Fire Hatchery (Easy)"
                                        2 -> "Frosty Snow Caverns"
                                        3 -> "Sparky Storm Spines"
                                        4 -> "Shadow Mist Cages"
                                        5 -> "VALKOR'S MAGMA CAGE (BOSS)"
                                        6 -> "Gilded Snake Vaults"
                                        7 -> "Lightning Ridge Ruins"
                                        8 -> "Dreadfiend Caves"
                                        9 -> "Cosmic Fire Spires"
                                        10 -> "MALAKAR'S VOID KEEP (FINAL)"
                                        else -> ""
                                    },
                                    style = Typography.bodyMedium.copy(color = if (unlocked) MutedSlate else Color.DarkGray, fontSize = 12.sp)
                                )

                                if (unlocked && levelProgressObj != null && levelProgressObj.highscore > 0) {
                                    Text(
                                        text = "High score: ${levelProgressObj.highscore}",
                                        fontSize = 11.sp,
                                        color = AmberGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Gold Stars completed row
                        if (unlocked) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                for (starIdx in 1..3) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star $starIdx",
                                        tint = if (starIdx <= stars) AmberGold else Color.DarkGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. GAMEPLAY & BOSS SCREENS (MERGED FOR GAMEPLAY EXPERIENCE)
// ==========================================
@Composable
fun GameplayScreen(
    viewModel: GameViewModel,
    onBackToMapPressed: () -> Unit
) {
    val context = LocalContext.current
    val boardData by viewModel.boardState.collectAsState()
    val score by viewModel.currentScore.collectAsState()
    val moves by viewModel.movesLeft.collectAsState()
    val targetCleared by viewModel.targetClearedCount.collectAsState()
    val targetReq = viewModel.targetCountRequired.collectAsState().value
    val targetElement = viewModel.targetType.collectAsState().value
    val levelName = viewModel.currentLevelId.collectAsState().value

    // Boss attributes
    val bossActive by viewModel.isBossLevel.collectAsState()
    val bossCurrentHp by viewModel.bossHp.collectAsState()
    val bossMaxHpVal = viewModel.bossMaxHp.collectAsState().value
    val bossTitleName = viewModel.bossName.collectAsState().value

    // Gesture selection positions state
    var firstSelectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    LavaBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ACTION TOP PANEL: Moves Left & Target matching displays
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(
                    onClick = onBackToMapPressed,
                    modifier = Modifier
                        .size(38.dp)
                        .background(PanelGray, CircleShape)
                        .border(1.dp, BloodCrimson, CircleShape)
                ) {
                    Icon(Icons.Default.Map, contentDescription = "Return to map", tint = TextWhite, modifier = Modifier.size(18.dp))
                }

                // Level text Name
                Text(
                    text = "LEVEL $levelName",
                    style = Typography.titleMedium.copy(color = TextWhite),
                    fontWeight = FontWeight.Bold
                )

                // Gold Coin meter
                val userState = viewModel.userProgress.collectAsState().value
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(PanelGray, RoundedCornerShape(12.dp))
                        .border(1.dp, BloodCrimson, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = AmberGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${userState?.coins ?: 0}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // TARGET STATUS CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, DragTargetBorderColor(targetElement), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TARGET INJECT", fontSize = 10.sp, color = MutedSlate, style = Typography.labelLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(24.dp)) {
                                CreatureBallRenderer(type = targetElement, special = SpecialType.NONE, isMatched = false)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${targetElement.displayName}s",
                                color = TextWhite,
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Progress counts
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$targetCleared",
                            style = Typography.displayLarge.copy(color = GetElementColor(targetElement), fontSize = 24.sp),
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " / $targetReq",
                            style = Typography.titleMedium.copy(color = MutedSlate),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Score indicator on right side
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SCORE", fontSize = 10.sp, color = MutedSlate, style = Typography.labelLarge)
                        Text("$score", style = Typography.titleLarge.copy(color = AmberGold), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ==========================================
            // BOSS HEALTH METER (IF ACTIVE BOSS CELL FIGHT)
            // ==========================================
            AnimatedVisibility(
                visible = bossActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkLava),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(1.dp, NeonRedRune, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Whatshot, contentDescription = "Boss", tint = NeonRedRune, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = bossTitleName,
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = Typography.labelLarge
                                )
                            }
                            Text(
                                text = "HP: $bossCurrentHp / $bossMaxHpVal",
                                color = NeonRedRune,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val hpRatio = (bossCurrentHp.toFloat() / bossMaxHpVal.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { hpRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = NeonRedRune,
                            trackColor = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // MOVES REMAINING LARGE CARD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PanelGray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, BloodCrimson, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MOVES LEFT: ", fontSize = 11.sp, color = MutedSlate, style = Typography.labelLarge)
                        Text("$moves", fontSize = 15.sp, color = NeonRedRune, fontWeight = FontWeight.Black)
                    }
                }

                val comboState by viewModel.comboMultiplier.collectAsState()
                if (comboState > 1) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .background(AmberGold, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("COMBO X$comboState!", color = ObsidianBlack, style = Typography.labelLarge, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // MATCH-3 GRID GRAPHIC CONTAINER
            // ==========================================
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(3.dp, BloodCrimson, RoundedCornerShape(20.dp))
                    .background(Color(0xFF0C0204))
                    .padding(6.dp)
            ) {
                if (boardData.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        for (r in 0 until 6) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                for (c in 0 until 6) {
                                    val piece = boardData[r][c]
                                    val isSelected = firstSelectedCell?.first == r && firstSelectedCell?.second == c

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .padding(3.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) Color(0x3DFFC107) else Color(0x14FFF2F2)
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) AmberGold else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .pointerInput(Unit) {
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        firstSelectedCell = Pair(r, c)
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        // Track swipes
                                                        val threshold = 25f
                                                        if (firstSelectedCell != null) {
                                                            var targetR = r
                                                            var targetC = c
                                                            if (dragAmount.x > threshold) {
                                                                targetC = c + 1
                                                            } else if (dragAmount.x < -threshold) {
                                                                targetC = c - 1
                                                            } else if (dragAmount.y > threshold) {
                                                                targetR = r + 1
                                                            } else if (dragAmount.y < -threshold) {
                                                                targetR = r - 1
                                                            }
                                                            if (targetR != r || targetC != c) {
                                                                if (targetR in 0..5 && targetC in 0..5) {
                                                                    viewModel.swapPieces(r, c, targetR, targetC)
                                                                    firstSelectedCell = null
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        firstSelectedCell = null
                                                    }
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Draw our cute dynamic canvas graphic
                                        CreatureBallRenderer(
                                            type = piece.type,
                                            special = piece.special,
                                            isMatched = piece.isMatched
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // ==========================================
            // DRAGON POWERS: BOOSTERS
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Shuffle Booster
                OutlinedButton(
                    onClick = { viewModel.useShuffleBooster() },
                    border = BorderStroke(1.dp, BloodCrimson),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = PanelGray),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = AmberGold)
                        Text("SHUFFLE", fontSize = 9.sp, color = TextWhite)
                        Text("100G", fontSize = 8.sp, color = AmberGold)
                    }
                }

                // Rainbow booster Strike
                OutlinedButton(
                    onClick = { viewModel.useRainbowStrikeBooster() },
                    border = BorderStroke(1.dp, BloodCrimson),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = PanelGray),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Stars, contentDescription = "Rainbow", tint = NeonRedRune)
                        Text("RAINBOW", fontSize = 9.sp, color = TextWhite)
                        Text("200G", fontSize = 8.sp, color = AmberGold)
                    }
                }
            }
        }

        // ==========================================
        // LEVEL CLEARED / POPUP SYSTEM OVERLAY
        // ==========================================
        val isCleared by viewModel.isLevelCleared.collectAsState()
        val isFailed by viewModel.isLevelFailed.collectAsState()

        if (isCleared) {
            Dialog(onDismissRequest = {}) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PanelGray),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .border(3.dp, AmberGold, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Success",
                            tint = AmberGold,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "LEVEL CLEARED!",
                            style = Typography.displayLarge.copy(color = AmberGold, fontSize = 24.sp),
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "You smashed the dungeons cell!",
                            style = Typography.bodyMedium.copy(color = MutedSlate),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Star ratings
                        val earnedStars by viewModel.rewardStarsEarned.collectAsState()
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 1..3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star $i",
                                    tint = if (i <= earnedStars) AmberGold else Color.DarkGray,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Coin rewards
                        val coinsGained by viewModel.rewardCoinsEarned.collectAsState()
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Loot reward:", color = MutedSlate, style = Typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = AmberGold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+$coinsGained COINS", color = AmberGold, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onBackToMapPressed,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRedRune),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("PROCEED", style = Typography.labelLarge, color = TextWhite)
                        }
                    }
                }
            }
        }

        if (isFailed) {
            Dialog(onDismissRequest = {}) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PanelGray),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .border(3.dp, BloodCrimson, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Icon(
                            imageVector = Icons.Default.SentimentVeryDissatisfied,
                            contentDescription = "Failed",
                            tint = NeonRedRune,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "OUT OF MOVES!",
                            style = Typography.displayLarge.copy(color = NeonRedRune, fontSize = 24.sp),
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "The dungeon got the upper hand.",
                            style = Typography.bodyMedium.copy(color = MutedSlate),
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        // Retry actions
                        Button(
                            onClick = { viewModel.startLevel(levelName) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRedRune),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("TRY AGAIN", style = Typography.labelLarge, color = TextWhite)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(onClick = onBackToMapPressed) {
                            Text("RETURN TO DUNGEONS MAP", color = MutedSlate, style = Typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

private fun DragTargetBorderColor(elem: CreatureType): Color {
    return when (elem) {
        CreatureType.FIRE -> NeonRedRune
        CreatureType.ICE -> FrostIceBlue
        CreatureType.THUNDER -> SparkThunderYellow
        CreatureType.SHADOW -> ShadowPurple
        CreatureType.GOLDEN_SNAKE -> AmberGold
        CreatureType.RAINBOW -> MythicPink
    }
}

private fun GetElementColor(elem: CreatureType): Color {
    return when (elem) {
        CreatureType.FIRE -> NeonRedRune
        CreatureType.ICE -> FrostIceBlue
        CreatureType.THUNDER -> SparkThunderYellow
        CreatureType.SHADOW -> ShadowPurple
        CreatureType.GOLDEN_SNAKE -> AmberGold
        CreatureType.RAINBOW -> MythicPink
    }
}

// ==========================================
// 5. CREATURE COLLECTION SCREEN
// ==========================================
@Composable
fun CollectionScreen(
    companions: List<CreatureCompanion>,
    userProgress: UserProgress?,
    onCompanionSelected: (String) -> Unit,
    onEvolvePressed: (String) -> Unit,
    onHatchPressed: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    LavaBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PanelGray, CircleShape)
                        .border(1.dp, BloodCrimson, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return", tint = TextWhite)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "CREATURE COVEN",
                    style = Typography.titleLarge.copy(color = NeonRedRune, fontSize = 18.sp),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Wallet
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(PanelGray, RoundedCornerShape(12.dp))
                        .border(1.dp, BloodCrimson, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = AmberGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${userProgress?.coins ?: 0}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Cards scrollable layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
            ) {
                items(companions) { companion ->
                    val isEquipped = userProgress?.selectedCompanionId == companion.companionId
                    val isUnlocked = companion.isUnlocked
                    val costToEvolve = companion.evolutionStage * 400

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isEquipped) DarkLava else PanelGray)
                            .border(
                                1.dp,
                                if (isEquipped) AmberGold else BloodCrimson,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            // Mini icon Canvas
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.Black, CircleShape)
                                    .border(1.dp, Color.DarkGray, CircleShape)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUnlocked) {
                                    val elemType = getCompanionElement(companion.companionId)
                                    CreatureBallRenderer(type = elemType, special = SpecialType.NONE, isMatched = false)
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = "Locked Egg", tint = Color.DarkGray)
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = if (isUnlocked) companion.name else "Mysterious Rare Egg",
                                    style = Typography.titleMedium.copy(color = TextWhite),
                                    fontWeight = FontWeight.Bold
                                )

                                if (isUnlocked) {
                                    Text(
                                        text = "Stage ${companion.evolutionStage} ${companion.element} Class",
                                        style = Typography.bodyMedium.copy(color = MutedSlate, fontSize = 12.sp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // XP bar
                                    Text(
                                        text = "LVL ${companion.level} (XP ${companion.xp}/100)",
                                        fontSize = 10.sp,
                                        style = Typography.labelLarge,
                                        color = AmberGold
                                    )
                                } else {
                                    Text(
                                        text = "Element class: ${companion.element}",
                                        style = Typography.bodyMedium.copy(color = MutedSlate, fontSize = 12.sp)
                                    )
                                }
                            }
                        }

                        // Actions logic
                        Column(horizontalAlignment = Alignment.End) {
                            if (isUnlocked) {
                                if (isEquipped) {
                                    Box(
                                        modifier = Modifier
                                            .background(AmberGold, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("ACTIVE", color = ObsidianBlack, style = Typography.labelLarge, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { onCompanionSelected(companion.companionId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack),
                                        modifier = Modifier.height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("EQUIP", fontSize = 11.sp, color = TextWhite)
                                    }
                                }

                                if (companion.evolutionStage < 3) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onEvolvePressed(companion.companionId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonRedRune),
                                        modifier = Modifier.height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("EVOLVE (${costToEvolve}G)", fontSize = 10.sp, color = TextWhite)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onHatchPressed(companion.companionId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text("HATCH (250G)", fontSize = 10.sp, color = ObsidianBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getCompanionElement(id: String): CreatureType {
    return when (id) {
        "pyro" -> CreatureType.FIRE
        "frosty" -> CreatureType.ICE
        "sparky" -> CreatureType.THUNDER
        "shadow" -> CreatureType.SHADOW
        "serpy" -> CreatureType.GOLDEN_SNAKE
        "iris" -> CreatureType.RAINBOW
        else -> CreatureType.FIRE
    }
}

// ==========================================
// 6. BREEDER SHOP SCREEN
// ==========================================
@Composable
fun ShopScreen(
    userProgress: UserProgress?,
    viewModel: GameViewModel,
    onBackPressed: () -> Unit
) {
    LavaBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PanelGray, CircleShape)
                        .border(1.dp, BloodCrimson, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return", tint = TextWhite)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "GOLD & BREEDERS SHOP",
                    style = Typography.titleLarge.copy(color = NeonRedRune, fontSize = 18.sp),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Wallet
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(PanelGray, RoundedCornerShape(12.dp))
                        .border(1.dp, BloodCrimson, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = AmberGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${userProgress?.coins ?: 0}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
            ) {
                // WATCH ADS TO EARN COINS
                item {
                    val context = LocalContext.current
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PanelGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonRedRune, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.OndemandVideo, contentDescription = "Watch Ad", tint = NeonRedRune, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("REWARD VIDEO SPONSOR", style = Typography.titleLarge, color = NeonRedRune)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Watch a short promotional video and get +150 Dragon Gold instantly to breed your creatures!",
                                style = Typography.bodyMedium,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val activity = context as? android.app.Activity
                                    if (activity != null) {
                                        com.example.game.AdManager.showRewardedAd(activity) { rewardCoins ->
                                            viewModel.earnCoinsFromAd(rewardCoins)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRedRune),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayCircleFilled, contentDescription = "Watch", tint = TextWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("WATCH SPONSOR AD (+150G)", color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // VIP PASS PROMOTION
                item {
                    val hasVip = userProgress?.vipActive ?: false
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkLava),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, AmberGold, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Stars, contentDescription = "VIP", tint = AmberGold, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ROYAL ELDER VIP PASS", style = Typography.titleLarge, color = AmberGold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Instantly unlock +2000 Dragon Gold coins and receive 2X Evolve multiplier bonuses on all dungeon quests!",
                                style = Typography.bodyMedium,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (hasVip) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text("ACTIVE ROYAL VIP BONUS (2X XP ACTIVE)", color = AmberGold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.buyVipPass() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("ACTIVATE NOW ($4.99)", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Coin packs listings
                item {
                    Text("COIN CRYPTS STORE", style = Typography.labelLarge, color = MutedSlate, modifier = Modifier.padding(vertical = 4.dp))
                }

                item {
                    ShopItemRow(title = "Baby Dragon Sack", amount = 800, cost = "$1.99", icon = Icons.Default.MonetizationOn, tintColor = AmberGold) {
                        viewModel.buyShopItem(800, 1.99)
                    }
                }

                item {
                    ShopItemRow(title = "Hydra Treasure Chest", amount = 2500, cost = "$4.99", icon = Icons.Default.CardGiftcard, tintColor = NeonRedRune) {
                        viewModel.buyShopItem(2500, 4.99)
                    }
                }

                item {
                    ShopItemRow(title = "Elder Wyrm Magma Vault", amount = 7000, cost = "$9.99", icon = Icons.Default.AllInbox, tintColor = AmberGold) {
                        viewModel.buyShopItem(7000, 9.99)
                    }
                }
            }
        }
    }
}

@Composable
fun ShopItemRow(
    title: String,
    amount: Int,
    cost: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tintColor: Color,
    onBuy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PanelGray)
            .border(1.dp, BloodCrimson, RoundedCornerShape(20.dp))
            .clickable { onBuy() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = tintColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = Typography.titleMedium, color = TextWhite)
                Text("+$amount gold coins load", style = Typography.bodyMedium, color = MutedSlate, fontSize = 12.sp)
            }
        }

        Button(
            onClick = onBuy,
            colors = ButtonDefaults.buttonColors(containerColor = BloodCrimson),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(cost, color = TextWhite, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 7. SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onResetProgress: () -> Unit
) {
    val showResetConfirmation = remember { mutableStateOf(false) }

    LavaBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PanelGray, CircleShape)
                        .border(1.dp, BloodCrimson, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return", tint = TextWhite)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "SETTINGS",
                    style = Typography.titleLarge.copy(color = NeonRedRune, fontSize = 18.sp),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PanelGray)
                    .border(1.dp, BloodCrimson, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Game Sound Effects (SFX)", color = TextWhite, style = Typography.bodyLarge)
                    var sfxEnabled by remember { mutableStateOf(true) }
                    Switch(
                        checked = sfxEnabled,
                        onCheckedChange = { sfxEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AmberGold,
                            checkedTrackColor = NeonRedRune
                        )
                    )
                }

                Divider(color = BloodCrimson, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Haptic Vibe Feedback", color = TextWhite, style = Typography.bodyLarge)
                    var hapticEnabled by remember { mutableStateOf(true) }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = { hapticEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AmberGold,
                            checkedTrackColor = NeonRedRune
                        )
                    )
                }

                Divider(color = BloodCrimson, thickness = 0.5.dp)

                Column(modifier = Modifier.clickable { showResetConfirmation.value = true }) {
                    Text(
                        "Reset Campaign Progress",
                        color = NeonRedRune,
                        style = Typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Wipe all level scores, coins, and collections",
                        color = MutedSlate,
                        style = Typography.bodyMedium,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Credits Block
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelGray),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BloodCrimson, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("DRAGONBLITZ v1.2", style = Typography.labelLarge, color = AmberGold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Designed by AI Studio elite Game Designers & Engineers. All rights reserved.",
                        style = Typography.bodyMedium,
                        color = MutedSlate,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Reset Progression Dialogue
        if (showResetConfirmation.value) {
            Dialog(onDismissRequest = { showResetConfirmation.value = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PanelGray),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .border(2.dp, NeonRedRune, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = NeonRedRune, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "WIPE ALL PROGRESS?",
                            style = Typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This action cannot be undone. You will lose Pyro and all unlocked companion elements.",
                            style = Typography.bodyMedium,
                            color = MutedSlate,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                onResetProgress()
                                showResetConfirmation.value = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRedRune),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("YES, WIPE DATA", color = TextWhite)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = { showResetConfirmation.value = false }) {
                            Text("CANCEL", color = MutedSlate, style = Typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}
