package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoButtonColor
import com.example.ui.components.DuoTopBar
import com.example.ui.components.HeartsInfoDialog
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.screens.DialogueScreen
import com.example.ui.screens.FreePracticeScreen
import com.example.ui.screens.PathLearningScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SpeakingExerciseScreen
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.LanguageGameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LinguaVoiceApp()
            }
        }
    }
}

@Composable
fun LinguaVoiceApp(
    viewModel: LanguageGameViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val streakInfo by viewModel.streakInfo.collectAsState()
    val userLevelInfo by viewModel.userLevelInfo.collectAsState()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsState()
    val showHeartsDialog by viewModel.showHeartsDialog.collectAsState()
    val showDailyStreakPopup by viewModel.showDailyStreakPopup.collectAsState()
    val isStreakBannerHidden by viewModel.isStreakBannerHidden.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    LaunchedEffect(Unit) {
        com.example.ads.AdMobManager.initialize(context)
        if (!hasMicPermission) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Handle system back navigation
    BackHandler(enabled = currentScreen != AppScreen.PATH_LEARN) {
        viewModel.navigateTo(AppScreen.PATH_LEARN)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            if (currentScreen != AppScreen.EXERCISE_SESSION) {
                DuoTopBar(
                    userProfile = userProfile.copy(streakDays = streakInfo.currentStreak),
                    onLanguageClick = { viewModel.setLanguageDialogVisible(true) },
                    onHeartsClick = { viewModel.setHeartsDialogVisible(true) },
                    onStreakClick = { viewModel.openDailyStreakPopup() },
                    levelInfo = userLevelInfo
                )
            }
        },
        bottomBar = {
            if (currentScreen != AppScreen.EXERCISE_SESSION) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    val screenBannerAdUnitId = when (currentScreen) {
                        AppScreen.PATH_LEARN -> com.example.ads.AdMobManager.AD_UNIT_ID_1
                        AppScreen.FREE_PRACTICE -> com.example.ads.AdMobManager.AD_UNIT_ID_2
                        AppScreen.DIALOGUE -> com.example.ads.AdMobManager.AD_UNIT_ID_3
                        AppScreen.PROFILE -> com.example.ads.AdMobManager.AD_UNIT_ID_4
                        AppScreen.EXERCISE_SESSION -> com.example.ads.AdMobManager.AD_UNIT_ID_5
                    }
                    com.example.ads.AdMobBanner(
                        adUnitId = screenBannerAdUnitId,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                    LinguaVoiceBottomBar(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Banner if mic not granted yet
            if (!hasMicPermission) {
                MicPermissionBanner(
                    onRequestPermission = {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }

            // Screen Switcher
            when (currentScreen) {
                AppScreen.PATH_LEARN -> {
                    PathLearningScreen(
                        userProfile = userProfile,
                        streakInfo = streakInfo,
                        levelInfo = userLevelInfo,
                        isStreakBannerHidden = isStreakBannerHidden,
                        onHideStreakBanner = { viewModel.setStreakBannerHidden(true) },
                        onOpenStreakPopup = { viewModel.openDailyStreakPopup() },
                        onStartLesson = { module -> viewModel.startLesson(module) }
                    )
                }
                AppScreen.EXERCISE_SESSION -> {
                    SpeakingExerciseScreen(
                        viewModel = viewModel
                    )
                }
                AppScreen.FREE_PRACTICE -> {
                    FreePracticeScreen(
                        userProfile = userProfile,
                        levelInfo = userLevelInfo,
                        onSelectExercise = { exercise -> viewModel.startCustomExercise(exercise) },
                        onPlayAudio = { phrase ->
                            viewModel.ttsManager.speak(phrase, userProfile.selectedLanguage)
                        }
                    )
                }
                AppScreen.DIALOGUE -> {
                    DialogueScreen(
                        viewModel = viewModel,
                        userProfile = userProfile
                    )
                }
                AppScreen.PROFILE -> {
                    ProfileScreen(
                        userProfile = userProfile.copy(streakDays = streakInfo.currentStreak),
                        streakInfo = streakInfo,
                        onChangeLanguage = { viewModel.setLanguageDialogVisible(true) },
                        onRefillHearts = {
                            val act = context as? android.app.Activity
                            if (act != null) {
                                com.example.ads.AdMobManager.showAutomaticInterstitial(act) {
                                    viewModel.refillHearts()
                                }
                            } else {
                                viewModel.refillHearts()
                            }
                        }
                    )
                }
            }
        }
    }

    // Emergent Once-Per-Day Streak Popup
    if (showDailyStreakPopup) {
        com.example.ui.components.StreakDetailDialog(
            streakInfo = streakInfo,
            onDismiss = { viewModel.dismissDailyStreakPopup(hideBannerToo = false) },
            onDismissAndHideBanner = { viewModel.dismissDailyStreakPopup(hideBannerToo = true) }
        )
    }

    // Dialogs
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = userProfile.selectedLanguage,
            onSelectLanguage = { lang -> viewModel.selectLanguage(lang) },
            onDismiss = { viewModel.setLanguageDialogVisible(false) }
        )
    }

    if (showHeartsDialog) {
        HeartsInfoDialog(
            currentHearts = userProfile.hearts,
            onRefill = {
                val act = context as? android.app.Activity
                if (act != null) {
                    com.example.ads.AdMobManager.showAutomaticInterstitial(act) {
                        viewModel.refillHearts()
                    }
                } else {
                    viewModel.refillHearts()
                }
            },
            onDismiss = { viewModel.setHeartsDialogVisible(false) }
        )
    }
}

@Composable
fun MicPermissionBanner(
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFC107))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Color(0xFFB78103),
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = "Permiso de Micrófono requerido",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF664D03)
                )
                Text(
                    text = "Para escuchar y corregir tu pronunciación en tiempo real.",
                    fontSize = 11.sp,
                    color = Color(0xFF664D03)
                )
            }
            DuoButton(
                text = "Activar",
                onClick = onRequestPermission,
                colorStyle = DuoButtonColor.GREEN,
                modifier = Modifier.size(width = 90.dp, height = 44.dp),
                elevation = 2.dp,
                testTag = "activate_mic_permission"
            )
        }
    }
}

@Composable
fun LinguaVoiceBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.PATH_LEARN,
            onClick = { onNavigate(AppScreen.PATH_LEARN) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.PATH_LEARN) Icons.Filled.School else Icons.Outlined.School,
                    contentDescription = "Aprender"
                )
            },
            label = {
                Text(
                    text = "Aprender",
                    fontWeight = if (currentScreen == AppScreen.PATH_LEARN) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DuoGreen,
                selectedTextColor = DuoGreen,
                indicatorColor = DuoGreenLight
            ),
            modifier = Modifier.testTag("nav_tab_learn")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.FREE_PRACTICE,
            onClick = { onNavigate(AppScreen.FREE_PRACTICE) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.FREE_PRACTICE) Icons.Filled.FitnessCenter else Icons.Outlined.FitnessCenter,
                    contentDescription = "Desafíos"
                )
            },
            label = {
                Text(
                    text = "Desafíos",
                    fontWeight = if (currentScreen == AppScreen.FREE_PRACTICE) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DuoGreen,
                selectedTextColor = DuoGreen,
                indicatorColor = DuoGreenLight
            ),
            modifier = Modifier.testTag("nav_tab_practice")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.DIALOGUE,
            onClick = { onNavigate(AppScreen.DIALOGUE) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.DIALOGUE) Icons.Filled.ChatBubbleOutline else Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Diálogo"
                )
            },
            label = {
                Text(
                    text = "Diálogo",
                    fontWeight = if (currentScreen == AppScreen.DIALOGUE) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DuoBlue,
                selectedTextColor = DuoBlue,
                indicatorColor = DuoBlue.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_tab_dialogue")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.PROFILE,
            onClick = { onNavigate(AppScreen.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Progreso"
                )
            },
            label = {
                Text(
                    text = "Progreso",
                    fontWeight = if (currentScreen == AppScreen.PROFILE) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DuoGreen,
                selectedTextColor = DuoGreen,
                indicatorColor = DuoGreenLight
            ),
            modifier = Modifier.testTag("nav_tab_profile")
        )
    }
}
