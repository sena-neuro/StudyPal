package com.example.studypal


import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_solo_session_settings.*


/**
 * A simple [Fragment] subclass.
 */
class SoloSessionSettingsFragment : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    private var backgroundMusic:String = "None"
    private val isSpotifyInstalled :Boolean
        get() {
            val pm = activity!!.packageManager
            return try {
                pm.getPackageInfo("com.spotify.music", 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_solo_session_settings, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        startSoloSessionButton.setOnClickListener(this)
        sessionMinutesSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, mins: Int, b: Boolean) {
                sessionMinutesTextView.text = mins.toString()
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
                // TODO not implemented
            }
        })
        breakMinutesSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onProgressChanged(seekBar: SeekBar, mins: Int, b: Boolean) {
                breakMinutesTextView.text = mins.toString()
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                // not implemented
            }
        })
        sessionCountSeekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                sessionCountTextView.text = p1.toString()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        val _backgroundMusic = arrayOf<String>("None","Offline: Piano music", "Spotify: Instrumental Study", "Spotify: Apply Yourself")
        val musicSpinner = activity!!.findViewById<Spinner>(R.id.background_music_spinner)

        var adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, _backgroundMusic)
        musicSpinner.adapter = adapter

        //LISTENER
        musicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                Toast.makeText(context, _backgroundMusic[i], Toast.LENGTH_SHORT).show()
                if ((i == 2) or (i == 3)){
                    if (!isSpotifyInstalled) {
                        installSpotify()
                    }
                }
                backgroundMusic = _backgroundMusic[i]
            }
            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

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
    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.startSoloSessionButton -> {
                val sessionMins = sessionMinutesSeekBar.progress.toLong()
                val breakMins = breakMinutesSeekBar.progress.toLong()
                val sesionCount:Int = sessionCountSeekBar.progress
                val action = SoloSessionSettingsFragmentDirections.actionSoloSessionSettingsFragmentToSoloSessionFragment(sessionMins, breakMins, sesionCount, backgroundMusic)
                navController.navigate(action)

            }

        }
    }

    companion object {
        private const val TAG = "SOLO SESSION SETTINGS"

    }

}
