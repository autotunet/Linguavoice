package com.example.ads

import android.app.Activity
import android.content.Context
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

object AdMobManager {

    // Google's official sample / test Ad Unit IDs
    const val BANNER_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var lastAdShowTime = 0L
    private const val AD_COOLDOWN_MS = 15000L // 15s cooldown to respect Google Play policies

    fun initialize(context: Context) {
        try {
            val appContext = context.applicationContext
            // Configure emulator test device to avoid Chromium seed signature errors and invalid test impressions
            val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            MobileAds.initialize(appContext) {
                loadInterstitialAd(appContext)
            }
        } catch (e: Exception) {
            // Graceful fallback
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isAdLoading) return
        isAdLoading = true

        try {
            val appContext = context.applicationContext
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                appContext,
                INTERSTITIAL_TEST_AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isAdLoading = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isAdLoading = false
                    }
                }
            )
        } catch (e: Exception) {
            isAdLoading = false
        }
    }

    fun showAutomaticInterstitial(activity: Activity, onDismiss: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        val currentAd = interstitialAd

        if (currentAd != null && (now - lastAdShowTime >= AD_COOLDOWN_MS)) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastAdShowTime = System.currentTimeMillis()
                    loadInterstitialAd(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
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
            // If ad is not ready or in cooldown, proceed directly without blocking user
            loadInterstitialAd(activity.applicationContext)
            onDismiss()
        }
    }
}

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier
) {
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, DuoCardBorder, RoundedCornerShape(10.dp))
            .testTag("admob_banner_view"),
        contentAlignment = Alignment.Center
    ) {
        // Fallback test placeholder when ad creative is loading or in offline emulator
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "Google AdMob • Anuncio de prueba",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdMobManager.BANNER_TEST_AD_UNIT_ID
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            isAdLoaded = true
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            isAdLoaded = false
                        }
                    }
                    try {
                        loadAd(AdRequest.Builder().build())
                    } catch (e: Exception) {
                        isAdLoaded = false
                    }
                }
            },
            onRelease = { adView ->
                try {
                    adView.destroy()
                } catch (e: Exception) {
                    // Safe cleanup to prevent Chromium WebView leaks
                }
            }
        )
    }
}
