package com.example.studypal


import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import kotlinx.android.synthetic.main.fragment_solo_session_settings.*


/**
 * A simple [Fragment] subclass.
 */
class SoloSessionSettingsFragment : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    private var spotifyAppRemote : SpotifyAppRemote? = null
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

        if (isSpotifyInstalled) {
            initSpotify()
        }
    }

    private fun initSpotify ( ) {
        val CLIENT_ID = "bee89f0e61db4516ab215e7fa380df62" // StudyPal client ID
        val REDIRECT_URI = "http://com.example.studypal/callback/"


        // Set the connection parameters
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect( context, connectionParams,
           object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    Log.d(TAG, "Connected! Yay!")
                    // Now you can start interacting with App Remote
                    connected()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, throwable.message, throwable)
                    // Something went wrong when attempting to connect! Handle errors here
                    // TODO log in to spotify
                }
            })


    }

    private fun connected () {

        spotifyAppRemote?.let {
            // Play a playlist
            val playlistURI = "spotify:playlist:37i9dQZF1DX9sIqqvKsjG8"//37i9dQZF1DX2sUQwD7tbmL"
            it.playerApi.play(playlistURI)
            // Subscribe to PlayerState
            val playlistName = it.playerApi
            it.playerApi.subscribeToPlayerState().setEventCallback {
                val track: Track = it.track
                Log.d(TAG, track.name + " by " + track.artist.name)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
            initSpotify()
        }
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }


    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.startSoloSessionButton -> {
                val sessionMins = sessionMinutesSeekBar.progress.toLong()
                val breakMins = breakMinutesSeekBar.progress.toLong()
                val sesionCount:Int = sessionCountSeekBar.progress
                val action = SoloSessionSettingsFragmentDirections.actionSoloSessionSettingsFragmentToSoloSessionFragment(sessionMins, breakMins, sesionCount)
                navController.navigate(action)

            }

        }
    }

    companion object {
        private const val TAG = "SOLO SESSION SETTINGS"

    }

}
