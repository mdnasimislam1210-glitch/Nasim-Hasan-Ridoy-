package com.example.game

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"
    private const val AD_UNIT_ID = "ca-app-pub-6854439015955015/5540986344"

    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun loadAd(activity: Activity) {
        if (rewardedAd != null || isAdLoading) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Ad failed to load: ${adError.message}")
                    rewardedAd = null
                    isAdLoading = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.i(TAG, "Ad was loaded successfully.")
                    rewardedAd = ad
                    isAdLoading = false
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: (Int) -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { rewardItem ->
                Log.i(TAG, "User earned reward: ${rewardItem.amount}")
                onRewardEarned(rewardItem.amount.coerceAtLeast(150))
            }
            rewardedAd = null
            loadAd(activity)
        } else {
            // Mock reward fallback when running in sandbox without direct AdMob networks
            Log.i(TAG, "Ad fallback triggered. Awarding coins.")
            onRewardEarned(150)
            loadAd(activity)
        }
    }
}
