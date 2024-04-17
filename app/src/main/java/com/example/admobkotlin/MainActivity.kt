package com.example.admobkotlin


import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.admobkotlin.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var consentInformation: ConsentInformation
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private lateinit var adRequest: AdRequest

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


        // First Method

        // our mobile ads.
        MobileAds.initialize(this)
        // initializing our ad request.
        adRequest = AdRequest.Builder().build()
        // ad view with the ad request
        binding.adView.loadAd(adRequest)



        // Second Method
       // requestConsentInfoUpdate()

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
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this@MainActivity
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
                Toast.makeText(this,requestConsentError.message,Toast.LENGTH_SHORT).show()
            })
    }

    private fun loadAds(){
        if(consentInformation.canRequestAds() && isMobileAdsInitializeCalled.getAndSet(true)){
            MobileAds.initialize(this){
                callBack()
            }

            val request = AdRequest.Builder().build()
            binding.adView.loadAd(request)
        }
    }

    private fun callBack(){
        binding.adView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
    }
}