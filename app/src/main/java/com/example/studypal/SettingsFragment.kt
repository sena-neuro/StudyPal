package com.example.studypal


import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationResponse
import com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE


/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

    }



    private val isSpotifyInstalled :Boolean
        get() {
            val pm = activity!!.packageManager
            try {
                pm.getPackageInfo("com.spotify.music", 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                return false
            }
        }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    fun installSpotify() {
        val appPackageName = "com.spotify.music"
        val referrer =
            "adjust_campaign=PACKAGE_NAME&adjust_tracker=ndjczk&utm_source=adjust_preinstall"

        try {
            val uri: Uri = Uri.parse("market://details")
                .buildUpon()
                .appendQueryParameter("id", appPackageName)
                .appendQueryParameter("referrer", referrer)
                .build()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (ignored: ActivityNotFoundException) {
            val uri: Uri = Uri.parse("https://play.google.com/store/apps/details")
                .buildUpon()
                .appendQueryParameter("id", appPackageName)
                .appendQueryParameter("referrer", referrer)
                .build()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            "spotify" -> {
                 val spotifyPref = findPreference<SwitchPreferenceCompat>(key)
                 Log.d("Preferences", "spotify connect: ${spotifyPref!!.isChecked}");
                if (spotifyPref.isChecked) {
                    /*
                    val CLIENT_ID = "your_client_id"
                    val REDIRECT_URI = "http://com.example.studypal/callback"
                    lateinit var mSpotifyAppRemote : SpotifyAppRemote


                    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
                    val REQUEST_CODE = 1337
                    // val REDIRECT_URI = "com.example.studypal://callback"

                    val builder = AuthenticationRequest.Builder(
                        CLIENT_ID,
                        AuthenticationResponse.Type.TOKEN,
                        REDIRECT_URI
                    )

                    builder.setScopes(arrayOf("streaming"))
                    val request = builder.build()

                    AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request)
                    */

                }

            }
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response =
                AuthenticationClient.getResponse(resultCode, data)
            when (response.type) {
                // Response was successful and contains auth token
                AuthenticationResponse.Type.TOKEN -> {

                    // Handle successful response
                }

                // Auth flow returned an error
                AuthenticationResponse.Type.ERROR -> {
                    // Handle error response
                }

                // Most likely auth flow was cancelled
                else -> {
                    // Handle other cases
                }
            }
        }
    }


}
