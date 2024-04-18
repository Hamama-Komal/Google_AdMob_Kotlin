package com.example.admobkotlin


import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.admobkotlin.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean
import com.google.android.gms.ads.FullScreenContentCallback as FullScreenContentCallback1


class MainActivity : AppCompatActivity(), OnUserEarnedRewardListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var consentInformation: ConsentInformation
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private lateinit var adRequest: AdRequest
    private var mInterstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var nativeAd: NativeAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        MobileAds.initialize(this)

        loadBannerAd()
        loadInterstitialAd()
        loadRewardAd()
        loadRewardInterAd()


        binding.btnInter.setOnClickListener {
            showInterstitialAd()
        }

        binding.btnReward.setOnClickListener {
            showRewardAd()
        }

        binding.btnInterReward.setOnClickListener {
            showRewardInterAd()
        }

        binding.btnNative.setOnClickListener {
            loadNativeAd()
        }

    }

    private fun loadNativeAd() {


        val adLoader = AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad ->
                // Show the native ad when it's loaded
                nativeAd = ad
                val adView = layoutInflater.inflate(R.layout.native_ad_layout, null)
                val nativeAdView = adView.findViewById<NativeAdView>(R.id.native_ad_view)
                populateNativeAdView(nativeAd!!, nativeAdView)
                binding.adContainer.removeAllViews()
                binding.adContainer.addView(adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    // Handle the failure to load native ad
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to load ad: ${p0.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, nativeAdView: NativeAdView?) {

        val headlineView = nativeAdView?.findViewById<TextView>(R.id.ad_headline)
        headlineView?.text = nativeAd?.headline

        val bodyView = nativeAdView?.findViewById<TextView>(R.id.ad_body)
        bodyView?.text = nativeAd.body




        if (nativeAdView != null) {
            nativeAdView.setNativeAd(nativeAd)
        }
    }


    private fun showRewardInterAd() {

        if (rewardedInterstitialAd != null) {
            rewardedInterstitialAd!!.show(this, this)
        } else {
            startActivity(Intent(this@MainActivity, SecondActivity::class.java))
        }
    }

    private fun loadRewardInterAd() {

        RewardedInterstitialAd.load(this, "ca-app-pub-3940256099942544/5354046379",
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedInterstitialAd = ad
                    rewardedInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            /** Called when the ad failed to show full screen content.  */
                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                //  Log.i(TAG, "onAdFailedToShowFullScreenContent")
                                startActivity(Intent(this@MainActivity, SecondActivity::class.java))
                            }

                            /** Called when ad showed the full screen content.  */
                            override fun onAdShowedFullScreenContent() {
                                //  Log.i(TAG, "onAdShowedFullScreenContent")
                            }

                            /** Called when full screen content is dismissed.  */
                            override fun onAdDismissedFullScreenContent() {
                                // Log.i(TAG, "onAdDismissedFullScreenContent")
                                startActivity(Intent(this@MainActivity, SecondActivity::class.java))
                                loadRewardInterAd() // don't know how useful
                            }
                        }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    //Log.d(TAG, adError?.toString())
                    Toast.makeText(this@MainActivity, adError.message, Toast.LENGTH_SHORT).show()
                    rewardedInterstitialAd = null
                }
            })
    }

    private fun showRewardAd() {

        rewardedAd?.let { ad ->
            ad.show(this, OnUserEarnedRewardListener { rewardItem ->
                // Handle the reward.
                // Log.d(TAG, "User earned the reward.")
                Toast.makeText(this@MainActivity, "User earned the reward.", Toast.LENGTH_SHORT)
                    .show()
            })

            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback1() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    startActivity(Intent(this@MainActivity, SecondActivity::class.java))
                    loadRewardAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    // Called when ad fails to show.
                    startActivity(Intent(this@MainActivity, SecondActivity::class.java))
                    rewardedAd = null
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                }
            }

        } ?: run {
            //Log.d(TAG, "The rewarded ad wasn't ready yet.")
            //Toast.makeText(this@MainActivity,  "The rewarded ad wasn't ready yet.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@MainActivity, SecondActivity::class.java))
        }

    }

    private fun showInterstitialAd() {

        if (mInterstitialAd != null) {
            mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback1() {

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    startActivity(Intent(this@MainActivity, SecondActivity::class.java))
                }
            }
            mInterstitialAd!!.show(this@MainActivity)

        } else {
            startActivity(Intent(this@MainActivity, SecondActivity::class.java))
        }
    }

    private fun loadRewardAd() {
        var adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            "ca-app-pub-3940256099942544/5224354917",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Log.d(TAG, adError?.toString())
                    // Toast.makeText(this@MainActivity, adError.message, Toast.LENGTH_SHORT).show()
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    //Log.d(TAG, "Ad was loaded.")
                    // Toast.makeText(this@MainActivity, "Ad loaded", Toast.LENGTH_SHORT).show()
                    rewardedAd = ad
                }
            })
    }

    private fun loadInterstitialAd() {

        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Toast.makeText(this@MainActivity, adError.message, Toast.LENGTH_SHORT).show()
                    //  Log.d(TAG, adError?.toString())
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Toast.makeText(this@MainActivity, "Ad was loaded", Toast.LENGTH_SHORT).show()
                    // Log.d(TAG, 'Ad was loaded.')
                    mInterstitialAd = interstitialAd
                }
            })

    }

    private fun loadBannerAd() {

        // First Method
        simplyLoadIt()

        // Second Method
        // requestConsentInfoUpdate()

    }

    private fun simplyLoadIt() {
        // initializing our ad request.
        adRequest = AdRequest.Builder().build()
        // ad view with the ad request
        binding.adView.loadAd(adRequest)


    }

    private fun requestConsentInfoUpdate() {

        val debugSettings = ConsentDebugSettings.Builder(this)
            .addTestDeviceHashedId("33BE2250B43518CCDA7DE426D04EE231")
            .build()

        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        // Create a ConsentRequestParameters object.
        // val params = ConsentRequestParameters.Builder().build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this@MainActivity, params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this@MainActivity
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        // Consent gathering failed.
                        // Log.w(TAG, "${loadAndShowError.errorCode}: ${loadAndShowError.message}")
                        Snackbar.make(
                            binding.adView, loadAndShowError.message, Snackbar.LENGTH_SHORT
                        ).apply {
                            setAction("Reload") {
                                requestConsentInfoUpdate()
                            }.setActionTextColor(getColor(R.color.black))
                            show()
                        }

                    } else {
                        isMobileAdsInitializeCalled.getAndSet(true)
                        loadAds()
                    }

                    /*// Consent has been gathered.
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsSdk()
                    }*/
                }
            },
            { requestConsentError ->
                // Consent gathering failed.
                //  Log.w(TAG, "${requestConsentError.errorCode}: ${requestConsentError.message}")
                Toast.makeText(this, requestConsentError.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun loadAds() {
        if (consentInformation.canRequestAds() && isMobileAdsInitializeCalled.getAndSet(true)) {
            MobileAds.initialize(this) {
                callBack()
            }

            val request = AdRequest.Builder().build()
            binding.adView.loadAd(request)
        }
    }

    private fun callBack() {
        binding.adView.adListener = object : AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
                Toast.makeText(this@MainActivity, adError.message, Toast.LENGTH_SHORT).show()
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Toast.makeText(this@MainActivity, "Ad was loaded", Toast.LENGTH_SHORT).show()
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
    }

    override fun onUserEarnedReward(p0: RewardItem) {
        Toast.makeText(this@MainActivity, "User earned reward.", Toast.LENGTH_SHORT).show()
    }
}