package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoOrange
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

data class AdMobSdkState(
    val isSdkInitialized: Boolean = false,
    val sdkVersion: String = "23.6.0",
    val publisherId: String = AdMobManager.PUBLISHER_ID,
    val configuredAdUnits: List<String> = AdMobManager.ALL_AD_UNIT_IDS,
    val isInterstitialReady: Boolean = false,
    val isRewardedReady: Boolean = false,
    val totalAdRequestsCount: Int = 0
)

object AdMobManager {

    const val PUBLISHER_ID = "ca-app-pub-1495262574338316"
    const val MANIFEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    // Configured Production Ad Unit IDs (1 through 8)
    const val AD_UNIT_ID_1 = "ca-app-pub-1495262574338316/2803163788"
    const val AD_UNIT_ID_2 = "ca-app-pub-1495262574338316/2903409205"
    const val AD_UNIT_ID_3 = "ca-app-pub-1495262574338316/2452733430"
    const val AD_UNIT_ID_4 = "ca-app-pub-1495262574338316/8970040322"
    const val AD_UNIT_ID_5 = "ca-app-pub-1495262574338316/1182179137"
    const val AD_UNIT_ID_6 = "ca-app-pub-1495262574338316/4962596335"
    const val AD_UNIT_ID_7 = "ca-app-pub-1495262574338316/2714752655"
    const val AD_UNIT_ID_8 = "ca-app-pub-1495262574338316/3503687777"

    val ALL_AD_UNIT_IDS: List<String> = listOf(
        AD_UNIT_ID_1,
        AD_UNIT_ID_2,
        AD_UNIT_ID_3,
        AD_UNIT_ID_4,
        AD_UNIT_ID_5,
        AD_UNIT_ID_6,
        AD_UNIT_ID_7,
        AD_UNIT_ID_8
    )

    // Semantic aliases mapped to the 8 provided Ad Unit IDs
    const val BANNER_MAIN_AD_UNIT_ID = AD_UNIT_ID_1
    const val BANNER_LEARN_AD_UNIT_ID = AD_UNIT_ID_2
    const val BANNER_PRACTICE_AD_UNIT_ID = AD_UNIT_ID_3
    const val BANNER_DIALOGUE_AD_UNIT_ID = AD_UNIT_ID_4
    const val BANNER_PROFILE_AD_UNIT_ID = AD_UNIT_ID_5
    const val INTERSTITIAL_LESSON_AD_UNIT_ID = AD_UNIT_ID_6
    const val REWARDED_HEARTS_AD_UNIT_ID = AD_UNIT_ID_7
    const val INTERSTITIAL_SECONDARY_AD_UNIT_ID = AD_UNIT_ID_8

    private val _sdkState = kotlinx.coroutines.flow.MutableStateFlow(
        AdMobSdkState(
            isSdkInitialized = false,
            sdkVersion = getSdkVersionString(),
            publisherId = PUBLISHER_ID,
            configuredAdUnits = ALL_AD_UNIT_IDS
        )
    )
    val sdkState: kotlinx.coroutines.flow.StateFlow<AdMobSdkState> = _sdkState

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isInterstitialLoading = false
    private var isRewardedLoading = false
    private var currentInterstitialIndex = 5 // Starts at AD_UNIT_ID_6 (index 5)
    private var currentRewardedIndex = 6 // Starts at AD_UNIT_ID_7 (index 6)
    private var lastAdShowTime = 0L
    private const val AD_COOLDOWN_MS = 15000L // 15s cooldown to respect Google Play policies

    fun getSdkVersionString(): String {
        return try {
            MobileAds.getVersion().toString()
        } catch (_: Throwable) {
            "23.6.0"
        }
    }

    fun getAdUnitIdByNumber(slotNumber: Int): String {
        val index = (slotNumber - 1).coerceIn(0, ALL_AD_UNIT_IDS.lastIndex)
        return ALL_AD_UNIT_IDS[index]
    }

    fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.lowercase().contains("emulator")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.lowercase().contains("droid4x")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.MODEL.contains("sdk_gphone")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("emulator"))
    }

    fun initialize(context: Context) {
        val detectedVersion = getSdkVersionString()
        _sdkState.value = _sdkState.value.copy(
            isSdkInitialized = true,
            sdkVersion = detectedVersion,
            configuredAdUnits = ALL_AD_UNIT_IDS
        )

        if (isRunningOnEmulator()) {
            // Avoid binding to missing android.adservices.MEASUREMENT_SERVICE or unconfigured Chromium WebView seed in emulator
            return
        }
        try {
            val appContext = context.applicationContext
            val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            MobileAds.initialize(appContext) {
                _sdkState.value = _sdkState.value.copy(
                    isSdkInitialized = true,
                    sdkVersion = getSdkVersionString()
                )
                loadInterstitialAd(appContext)
                loadRewardedAd(appContext)
            }
        } catch (e: Exception) {
            // Graceful fallback
        }
    }

    fun loadInterstitialAd(
        context: Context,
        adUnitId: String = ALL_AD_UNIT_IDS[currentInterstitialIndex % ALL_AD_UNIT_IDS.size],
        attemptCount: Int = 0
    ) {
        if (isRunningOnEmulator()) return
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        try {
            val appContext = context.applicationContext
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                appContext,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                        // Try next configured Ad Unit ID in rotation if available
                        if (attemptCount + 1 < ALL_AD_UNIT_IDS.size) {
                            currentInterstitialIndex = (currentInterstitialIndex + 1) % ALL_AD_UNIT_IDS.size
                            val nextUnitId = ALL_AD_UNIT_IDS[currentInterstitialIndex]
                            loadInterstitialAd(appContext, nextUnitId, attemptCount + 1)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            isInterstitialLoading = false
        }
    }

    fun loadRewardedAd(
        context: Context,
        adUnitId: String = ALL_AD_UNIT_IDS[currentRewardedIndex % ALL_AD_UNIT_IDS.size],
        attemptCount: Int = 0
    ) {
        if (isRunningOnEmulator()) return
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        try {
            val appContext = context.applicationContext
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                appContext,
                adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading = false
                        if (attemptCount + 1 < ALL_AD_UNIT_IDS.size) {
                            currentRewardedIndex = (currentRewardedIndex + 1) % ALL_AD_UNIT_IDS.size
                            val nextUnitId = ALL_AD_UNIT_IDS[currentRewardedIndex]
                            loadRewardedAd(appContext, nextUnitId, attemptCount + 1)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            isRewardedLoading = false
        }
    }

    fun showAutomaticInterstitial(activity: Activity, onDismiss: () -> Unit = {}) {
        if (isRunningOnEmulator()) {
            onDismiss()
            return
        }
        val now = System.currentTimeMillis()
        val currentAd = interstitialAd

        if (currentAd != null && (now - lastAdShowTime >= AD_COOLDOWN_MS)) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastAdShowTime = System.currentTimeMillis()
                    currentInterstitialIndex = (currentInterstitialIndex + 1) % ALL_AD_UNIT_IDS.size
                    loadInterstitialAd(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    currentInterstitialIndex = (currentInterstitialIndex + 1) % ALL_AD_UNIT_IDS.size
                    loadInterstitialAd(activity.applicationContext)
                    onDismiss()
                }
            }
            try {
                currentAd.show(activity)
            } catch (e: Exception) {
                interstitialAd = null
                loadInterstitialAd(activity.applicationContext)
                onDismiss()
            }
        } else {
            val currentRewarded = rewardedAd
            if (currentRewarded != null && (now - lastAdShowTime >= AD_COOLDOWN_MS)) {
                currentRewarded.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        rewardedAd = null
                        lastAdShowTime = System.currentTimeMillis()
                        currentRewardedIndex = (currentRewardedIndex + 1) % ALL_AD_UNIT_IDS.size
                        loadRewardedAd(activity.applicationContext)
                        onDismiss()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        rewardedAd = null
                        loadRewardedAd(activity.applicationContext)
                        onDismiss()
                    }
                }
                try {
                    currentRewarded.show(activity) { _ -> }
                } catch (e: Exception) {
                    rewardedAd = null
                    loadRewardedAd(activity.applicationContext)
                    onDismiss()
                }
            } else {
                loadInterstitialAd(activity.applicationContext)
                loadRewardedAd(activity.applicationContext)
                onDismiss()
            }
        }
    }
}

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.BANNER_MAIN_AD_UNIT_ID
) {
    val isEmulator = remember { AdMobManager.isRunningOnEmulator() }
    var isAdLoaded by remember(adUnitId) { mutableStateOf(false) }
    val initialIndex = remember(adUnitId) {
        AdMobManager.ALL_AD_UNIT_IDS.indexOf(adUnitId).coerceAtLeast(0)
    }
    var activeUnitIndex by remember(adUnitId) { mutableIntStateOf(initialIndex) }
    var fallbackAttempts by remember(adUnitId) { mutableIntStateOf(0) }
    val currentAdUnitId = AdMobManager.ALL_AD_UNIT_IDS[activeUnitIndex % AdMobManager.ALL_AD_UNIT_IDS.size]
    val shortUnitSuffix = remember(currentAdUnitId) {
        currentAdUnitId.substringAfterLast("/")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, DuoCardBorder, RoundedCornerShape(10.dp))
            .testTag("admob_banner_view"),
        contentAlignment = Alignment.Center
    ) {
        if (!isAdLoaded) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Surface(
                    color = DuoOrange.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "ANUNCIO",
                        color = DuoOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "Google AdMob • Unidad #$shortUnitSuffix",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (!isEmulator) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = currentAdUnitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                isAdLoaded = true
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                super.onAdFailedToLoad(loadAdError)
                                isAdLoaded = false
                                if (fallbackAttempts + 1 < AdMobManager.ALL_AD_UNIT_IDS.size) {
                                    fallbackAttempts += 1
                                    activeUnitIndex = (activeUnitIndex + 1) % AdMobManager.ALL_AD_UNIT_IDS.size
                                }
                            }
                        }
                        try {
                            loadAd(AdRequest.Builder().build())
                        } catch (e: Exception) {
                            isAdLoaded = false
                        }
                    }
                },
                update = { adView ->
                    if (adView.adUnitId != currentAdUnitId) {
                        try {
                            adView.adUnitId = currentAdUnitId
                            adView.loadAd(AdRequest.Builder().build())
                        } catch (e: Exception) {
                            isAdLoaded = false
                        }
                    }
                },
                onRelease = { adView ->
                    try {
                        adView.destroy()
                    } catch (e: Exception) {
                        // Safe cleanup
                    }
                }
            )
        }
    }
}

@Composable
fun AdMobSdkStatusCard(
    modifier: Modifier = Modifier
) {
    val sdkState = AdMobManager.sdkState.value
    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_sdk_status_card"),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DuoCardBorder)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SDK Google AdMob (play-services-ads)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = com.example.ui.theme.DuoGreen.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = if (sdkState.isSdkInitialized) "SDK ACTIVO • v${sdkState.sdkVersion}" else "SDK v${sdkState.sdkVersion}",
                        color = com.example.ui.theme.DuoGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Publisher ID: ${sdkState.publisherId} • ${sdkState.configuredAdUnits.size} unidades configuradas",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )

            sdkState.configuredAdUnits.chunked(2).forEachIndexed { rowIdx, pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEachIndexed { colIdx, unitId ->
                        val slotNum = rowIdx * 2 + colIdx + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, DuoCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("admob_unit_slot_$slotNum")
                        ) {
                            Text(
                                text = "#$slotNum: .../${unitId.substringAfterLast("/")}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

