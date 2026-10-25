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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Language
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
import androidx.compose.runtime.CompositionLocalProvider
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
import com.example.ui.components.LanguagePairOnboardingDialog
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.screens.DialogueScreen
import com.example.ui.screens.FreePracticeScreen
import com.example.ui.screens.LanguageSelectionScreen
import com.example.ui.screens.PathLearningScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SpeakingExerciseScreen
import com.example.ui.theme.AppStrings
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.LocalAppStrings
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
    val heartsAndStreakState by viewModel.heartsAndStreakState.collectAsState()
    val userLevelInfo by viewModel.userLevelInfo.collectAsState()
    val dailyGoalState by viewModel.dailyGoalState.collectAsState()
    val selectedSpokenLanguage by viewModel.selectedSpokenLanguage.collectAsState()
    val selectedTargetLanguage by viewModel.selectedTargetLanguage.collectAsState()
    val selectedLearningFocus by viewModel.selectedLearningFocus.collectAsState()
    val targetLanguagePreference by viewModel.targetLanguagePreference.collectAsState()
    val personalizedModules by viewModel.personalizedModules.collectAsState()
    val personalizedModulesByLevel by viewModel.personalizedModulesByLevel.collectAsState()
    val showLanguagePairOnboarding by viewModel.showLanguagePairOnboarding.collectAsState()
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

    val appStrings = remember(selectedSpokenLanguage) {
        AppStrings.forSpokenLanguage(selectedSpokenLanguage)
    }

    CompositionLocalProvider(LocalAppStrings provides appStrings) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            if (currentScreen != AppScreen.EXERCISE_SESSION && currentScreen != AppScreen.LANGUAGE_SELECTION) {
                DuoTopBar(
                    userProfile = userProfile.copy(
                        spokenLanguage = selectedSpokenLanguage,
                        selectedLanguage = selectedTargetLanguage,
                        hearts = heartsAndStreakState.hearts,
                        streakDays = maxOf(streakInfo.currentStreak, heartsAndStreakState.currentStreak)
                    ),
                    onLanguageClick = { viewModel.openLanguageSelectionScreen() },
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
                        AppScreen.LANGUAGE_SELECTION -> com.example.ads.AdMobManager.AD_UNIT_ID_6
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
                        userProfile = userProfile.copy(
                            spokenLanguage = selectedSpokenLanguage,
                            selectedLanguage = selectedTargetLanguage,
                            hearts = heartsAndStreakState.hearts,
                            streakDays = maxOf(streakInfo.currentStreak, heartsAndStreakState.currentStreak)
                        ),
                        streakInfo = streakInfo.copy(
                            currentStreak = maxOf(streakInfo.currentStreak, heartsAndStreakState.currentStreak),
                            bestStreak = maxOf(streakInfo.bestStreak, heartsAndStreakState.bestStreak),
                            practicedToday = streakInfo.practicedToday || heartsAndStreakState.practicedToday
                        ),
                        levelInfo = userLevelInfo,
                        dailyGoalState = dailyGoalState,
                        onSelectDailyGoalXp = { targetXp -> viewModel.setDailyXpGoal(targetXp) },
                        onTriggerDailyGoalCelebration = { viewModel.triggerDailyGoalCelebration() },
                        isStreakBannerHidden = isStreakBannerHidden,
                        onHideStreakBanner = { viewModel.setStreakBannerHidden(true) },
                        onOpenStreakPopup = { viewModel.openDailyStreakPopup() },
                        personalizedModules = personalizedModules,
                        personalizedModulesByLevel = personalizedModulesByLevel,
                        onOpenLanguageSelection = { viewModel.openLanguageSelectionScreen() },
                        onStartLesson = { module -> viewModel.startLesson(module) }
                    )
                }
                AppScreen.LANGUAGE_SELECTION -> {
                    LanguageSelectionScreen(
                        currentLanguage = selectedTargetLanguage,
                        currentLearningFocus = selectedLearningFocus,
                        roomPreferenceEntity = targetLanguagePreference,
                        currentSpokenLanguage = selectedSpokenLanguage,
                        onSelectTargetLanguage = { language, focus, goHome ->
                            viewModel.selectLanguage(
                                language = language,
                                learningFocusGoal = focus,
                                navigateToHome = goHome,
                                spokenLanguage = selectedSpokenLanguage
                            )
                        },
                        onSelectLanguagePair = { spoken, target, focus, goHome ->
                            viewModel.selectLanguagePair(
                                spokenLanguage = spoken,
                                targetLanguage = target,
                                learningFocusGoal = focus,
                                navigateToHome = goHome
                            )
                        },
                        onPlayLanguageSample = { language ->
                            viewModel.speakLanguageGreeting(language)
                        },
                        onStartPersonalizedLesson = { module ->
                            viewModel.startLesson(module)
                        },
                        onBack = {
                            viewModel.navigateTo(AppScreen.PATH_LEARN)
                        }
                    )
                }
                AppScreen.EXERCISE_SESSION -> {
                    SpeakingExerciseScreen(
                        viewModel = viewModel
                    )
                }
                AppScreen.FREE_PRACTICE -> {
                    FreePracticeScreen(
                        userProfile = userProfile.copy(
                            spokenLanguage = selectedSpokenLanguage,
                            selectedLanguage = selectedTargetLanguage
                        ),
                        levelInfo = userLevelInfo,
                        onSelectExercise = { exercise -> viewModel.startCustomExercise(exercise) },
                        onPlayAudio = { phrase ->
                            viewModel.ttsManager.speak(phrase, selectedTargetLanguage)
                        }
                    )
                }
                AppScreen.DIALOGUE -> {
                    DialogueScreen(
                        viewModel = viewModel,
                        userProfile = userProfile.copy(
                            spokenLanguage = selectedSpokenLanguage,
                            selectedLanguage = selectedTargetLanguage
                        )
                    )
                }
                AppScreen.PROFILE -> {
                    ProfileScreen(
                        userProfile = userProfile.copy(
                            spokenLanguage = selectedSpokenLanguage,
                            selectedLanguage = selectedTargetLanguage,
                            hearts = heartsAndStreakState.hearts,
                            streakDays = maxOf(streakInfo.currentStreak, heartsAndStreakState.currentStreak)
                        ),
                        streakInfo = streakInfo.copy(
                            currentStreak = maxOf(streakInfo.currentStreak, heartsAndStreakState.currentStreak),
                            bestStreak = maxOf(streakInfo.bestStreak, heartsAndStreakState.bestStreak),
                            practicedToday = streakInfo.practicedToday || heartsAndStreakState.practicedToday
                        ),
                        dailyGoalState = dailyGoalState,
                        onSelectDailyGoalXp = { targetXp -> viewModel.setDailyXpGoal(targetXp) },
                        onTriggerDailyGoalCelebration = { viewModel.triggerDailyGoalCelebration() },
                        onChangeLanguage = { viewModel.openLanguageSelectionScreen() },
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

    // Daily Goal Celebration Animation Overlay ("Animación de celebración al alcanzar metas diarias")
    com.example.ui.components.DailyGoalCelebrationOverlay(
        state = dailyGoalState,
        onReplayCelebration = { viewModel.replayDailyGoalCelebration() },
        onDismiss = { viewModel.dismissDailyGoalCelebration() }
    )

    // Emergent Once-Per-Day Streak Popup
    if (showDailyStreakPopup) {
        com.example.ui.components.StreakDetailDialog(
            streakInfo = streakInfo,
            onDismiss = { viewModel.dismissDailyStreakPopup(hideBannerToo = false) },
            onDismissAndHideBanner = { viewModel.dismissDailyStreakPopup(hideBannerToo = true) }
        )
    }

    // Initial Onboarding Dialog asking what language the user speaks and what language they want to learn
    if (showLanguagePairOnboarding && currentScreen != AppScreen.LANGUAGE_SELECTION) {
        LanguagePairOnboardingDialog(
            initialSpokenLanguage = selectedSpokenLanguage,
            initialTargetLanguage = selectedTargetLanguage,
            onConfirmLanguagePair = { spoken, target ->
                viewModel.selectLanguagePair(
                    spokenLanguage = spoken,
                    targetLanguage = target,
                    learningFocusGoal = selectedLearningFocus,
                    navigateToHome = true
                )
            },
            onOpenFullLanguageScreen = {
                viewModel.dismissLanguagePairOnboarding()
                viewModel.openLanguageSelectionScreen()
            },
            onDismiss = { viewModel.dismissLanguagePairOnboarding() }
        )
    }

    // Dialogs
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = selectedTargetLanguage,
            selectedSpokenLanguage = selectedSpokenLanguage,
            onSelectLanguage = { lang -> viewModel.selectLanguage(lang) },
            onSelectSpokenLanguage = { spoken -> viewModel.selectSpokenLanguage(spoken) },
            onDismiss = { viewModel.setLanguageDialogVisible(false) }
        )
    }

    if (showHeartsDialog) {
        HeartsInfoDialog(
            currentHearts = heartsAndStreakState.hearts,
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
    val strings = LocalAppStrings.current
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
                    contentDescription = strings.navPath
                )
            },
            label = {
                Text(
                    text = strings.navPath,
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
                    contentDescription = strings.navLaboratory
                )
            },
            label = {
                Text(
                    text = strings.navLaboratory,
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
                    contentDescription = strings.navDialogues
                )
            },
            label = {
                Text(
                    text = strings.navDialogues,
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
            selected = currentScreen == AppScreen.LANGUAGE_SELECTION,
            onClick = { onNavigate(AppScreen.LANGUAGE_SELECTION) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.LANGUAGE_SELECTION) Icons.Filled.Language else Icons.Outlined.Language,
                    contentDescription = strings.navLanguages
                )
            },
            label = {
                Text(
                    text = strings.navLanguages,
                    fontWeight = if (currentScreen == AppScreen.LANGUAGE_SELECTION) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DuoBlue,
                selectedTextColor = DuoBlue,
                indicatorColor = DuoBlue.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_tab_language")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.PROFILE,
            onClick = { onNavigate(AppScreen.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = strings.navProgress
                )
            },
            label = {
                Text(
                    text = strings.navProgress,
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
