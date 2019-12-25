package com.example.studypal

import android.Manifest
import android.app.AlertDialog
import android.media.MediaPlayer
import android.opengl.GLSurfaceView
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.opentok.android.*
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import kotlinx.android.synthetic.main.fragment_one2_one_session.*
import org.json.JSONException
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class One2OneSessionFragment: Fragment(), Session.SessionListener,
    PublisherKit.PublisherListener, View.OnClickListener {
    private var connected:Boolean = false
    private var mSession: Session? = null
    private val sessionType:String = "One2One"
    private var mPublisher: Publisher? = null
    private var navController: NavController? = null
    val args: One2OneSessionFragmentArgs by navArgs()
    private var mPublisherViewContainer: FrameLayout? = null
    private var mSubscriberViewContainer: FrameLayout? = null
    private var mSubscriber: Subscriber? = null
    private var inSession = false
    private var totalMinsInSession: Long = 0
    private var sessionCount = 0
    private lateinit var mBackgroundSound: BackgroundSound
    private var spotifyAppRemote : SpotifyAppRemote? = null
    val spotifyMusic : Boolean
        get() {
            return args.backgroundMusic.contains("Spotify")
        }
    val offlineMusic : Boolean
    get(){
        return  args.backgroundMusic.contains("Offline")
    }

    private var firstTimeFlag = true
    private var remainingMilis: Long=0
    private val sessionViewModel: SessionViewModel by lazy {
        ViewModelProviders.of(this).get(SessionViewModel::class.java)
    }
    private val milisChangeObserver =
        Observer<Long> { milisRemaining -> displayTime(milisRemaining)
            remainingMilis = milisRemaining}
    private val sessionObserver = Observer<Boolean> { onSession ->
        if (onSession) {
            firstTimeFlag = false
            val stateTextView = activity!!.findViewById<TextView>(R.id.stateTextView)
            stateTextView.text = "In study Session"
            inSession = true
        } else {
            val stateTextView = activity!!.findViewById<TextView>(R.id.stateTextView)
            stateTextView.text = "In Break"
            totalMinsInSession += args.sessionMins
            sessionCount += 1
            inSession = false
        }
    }
    private val remainingSessionObserver = Observer<Int> { sessionsRemaining ->
        if (sessionsRemaining == 0) {
            closeCall()
        }
    }
    val playlistURI : String
        get() {
            if( args.backgroundMusic.contains("Apply") ) {
                return "spotify:playlist:37i9dQZF1DXe1cC3XInKct"
            }
            else if (args.backgroundMusic.contains("Instrumental")) {
                return "spotify:playlist:37i9dQZF1DX9sIqqvKsjG8"
            }
            return ""
        }
    private fun displayTime(milisRemaining: Long?) {
        val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(milisRemaining!!)
        val remainingSeconds =
            TimeUnit.MILLISECONDS.toSeconds(milisRemaining) - TimeUnit.MINUTES.toSeconds(
                remainingMinutes
            )
        val timeLeftText = String.format(
            resources.getString(R.string.timer_text),
            remainingMinutes,
            remainingSeconds
        )
        val timeLeftTextView = activity!!.findViewById<TextView>(R.id.timeLeftTextView)
        timeLeftTextView.text = timeLeftText
    }

    fun fetchSessionConnectionData() {
        val reqQueue = Volley.newRequestQueue(context!!)
        reqQueue.add(JsonObjectRequest(Request.Method.GET,
            "https://studypall.herokuapp.com" + "/session", null,
            Response.Listener { response ->
                try {
                    val API_KEY = response.getString("apiKey")
                    val SESSION_ID = response.getString("sessionId")
                    val TOKEN = response.getString("token")

                    Log.i(LOG_TAG, "API_KEY: $API_KEY")
                    Log.i(LOG_TAG, "SESSION_ID: $SESSION_ID")
                    Log.i(LOG_TAG, "TOKEN: $TOKEN")

                    mSession = Session.Builder(context, API_KEY, SESSION_ID).build()
                    mSession!!.setSessionListener(this@One2OneSessionFragment)
                    mSession!!.connect(TOKEN)
                } catch (error: JSONException) {
                    Log.e(LOG_TAG, "Web Service error: " + error.message)
                }
            }, Response.ErrorListener{ error -> Log.e(LOG_TAG, "Web Service error: " + error.message) })
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBackgroundSound = BackgroundSound()
        sessionViewModel.milisChangeNotifier.observe(this, milisChangeObserver)
        sessionViewModel.onSession.observe(this, sessionObserver)
        sessionViewModel.remainingSessionCount.observe(this, remainingSessionObserver)
        // This callback will only be called when MyFragment is at least Started.
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button
            confirmExit()
        }
    }
    override fun onStart() {
        super.onStart()
        if(spotifyMusic)
            initSpotify()
        else if(offlineMusic)
            mBackgroundSound.execute(null)
    }

    private fun playFromSpotify () {
        spotifyAppRemote?.let {
            Log.d(LOG_TAG, "in let")
            // Play a playlist
            it.playerApi.play(playlistURI)
            // Subscribe to PlayerState
            it.playerApi.subscribeToPlayerState().setEventCallback {
                val track: Track = it.track
                Log.d(LOG_TAG, track.name + " by " + track.artist.name)
            }
        }

    }


    private fun initSpotify () {
        val CLIENT_ID = "bee89f0e61db4516ab215e7fa380df62" // StudyPal client ID
        val REDIRECT_URI = "http://com.example.studypal/callback/"
        Log.d(LOG_TAG,"initSpotify")
        // Set the connection parameters
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect( context, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    Log.d(LOG_TAG, "Connected! Yay!")
                    // Now you can start interacting with App Remote
                    playFromSpotify()
                }
                override fun onFailure(throwable: Throwable) {
                    Log.d(LOG_TAG, throwable.message, throwable)
                    // Something went wrong when attempting to connect! Handle errors here
                    // TODO log in to spotify
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_one2_one_session, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        constraintLayoutOne2OneSession.visibility = View.GONE
        findingPalTextView.visibility = View.VISIBLE
        finding_pal_animation_view.visibility = View.VISIBLE
        finding_pal_animation_view.setAnimation(R.raw.findingpal)
        finding_pal_animation_view.playAnimation()
        val bMilis =args.breakMins
        val sessionCount = args.sessionCount
        val sMilis = args.sessionMins
        sessionViewModel.createTimers(sessionCount, sMilis, bMilis)
        navController = Navigation.findNavController(view)
        requestPermissions()
        exitOne2OneSessionButton.setOnClickListener(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    private fun requestPermissions() {
        val perms = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (EasyPermissions.hasPermissions(context!!, *perms)) {
            // initialize view objects from your layout
            mPublisherViewContainer = activity!!.findViewById(R.id.publisher_container)
            mSubscriberViewContainer = activity!!.findViewById(R.id.subscriber_container)

            // initialize and connect to the session
            fetchSessionConnectionData()

        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to your camera and mic to make video calls",
                RC_VIDEO_APP_PERM,
                *perms
            )
        }
    }
    private fun stopMusic() {
        if(spotifyMusic){
            spotifyAppRemote?.let {
                Log.d(LOG_TAG, "in let stop music")
                it.playerApi.pause()
                // Subscribe to PlayerState
            }
        }
        else{
            mBackgroundSound.cancel(true)
        }

    }
    private fun confirmExit() {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("If you exit during a session, your focus score will be affected negatively")
            .setIcon(android.R.drawable.ic_dialog_alert)
            //set title
            .setTitle("Are you sure to exit this session?")
            .setPositiveButton("Yes") { dialog, id ->
                dialog.dismiss()
                closeCall()
            }
            .setNegativeButton("No") { dialog, id ->
                // User cancelled the dialog
                dialog.dismiss()
            }.show()
    }

    private fun closeCall() {
        Log.d(LOG_TAG, "Close Call")
        sessionViewModel.closeCall()
        if(connected)
            mSession!!.disconnect()
        stopMusic()
        if (inSession) {
            totalMinsInSession = totalMinsInSession + args.sessionMins - TimeUnit.MILLISECONDS.toSeconds(remainingMilis)
        }
        else if (firstTimeFlag)
        {
            totalMinsInSession = 0
        }
        Log.d("Totalmins",totalMinsInSession.toString())
        val sessionMins = args.sessionMins
        val breakMins = args.breakMins
        val action =
            One2OneSessionFragmentDirections.actionOne2OneSessionFragmentToEndSessionFragment(
                inSession,
                totalMinsInSession,
                sessionMins.toInt(),
                sessionCount,
                breakMins.toInt(),
                sessionType)
        navController!!.navigate(action)
    }


    //SessionListener methods
    override fun onConnected(session: Session) {
        Log.i(LOG_TAG, "Session Connected")
        Log.i(LOG_TAG, "Session Connected")
        mPublisher = Publisher.Builder(context).build()
        mPublisher!!.setPublisherListener(this)
        mPublisher!!.publishAudio = false

        mPublisherViewContainer!!.addView(mPublisher!!.view)

        if (mPublisher!!.view is GLSurfaceView) {
            (mPublisher!!.view as GLSurfaceView).setZOrderOnTop(true)
        }
        mSession!!.publish(mPublisher)
        connected = true
    }

    override fun onDisconnected(session: Session) {
        Log.i(LOG_TAG, "Session Disconnected")
    }

    override fun onStreamReceived(session: Session, stream: Stream) {
        Log.i(LOG_TAG, "Stream Received")
        if (mSubscriber == null) {
            finding_pal_animation_view.visibility = View.GONE
            findingPalTextView.visibility = View.GONE
            constraintLayoutOne2OneSession.visibility = View.VISIBLE
            mSubscriber = Subscriber.Builder(context, stream).build()
            mSession!!.subscribe(mSubscriber)
            mSubscriber!!.subscribeToAudio = false
            mSubscriberViewContainer!!.addView(mSubscriber!!.view)
            sessionViewModel.startTimers()
        }
    }

    override fun onStreamDropped(session: Session, stream: Stream) {
        Log.i(LOG_TAG, "Stream Dropped")
        if (mSubscriber != null) {
            mSubscriber = null
            mSubscriberViewContainer!!.removeAllViews()
            mPublisherViewContainer!!.removeAllViews()
            mSubscriberViewContainer!!.addView(mPublisher!!.view)
            mPublisherViewContainer!!.visibility = View.GONE
            Toast.makeText(context, "Your Study Pal Has Left The Session, You Can Continue Working.", Toast.LENGTH_SHORT)
        }
    }

    override fun onError(session: Session, opentokError: OpentokError) {
        Log.e(LOG_TAG, "Session error: " + opentokError.message)
    }
    // PublisherListener methods

    override fun onStreamCreated(publisherKit: PublisherKit, stream: Stream) {
        Log.i(LOG_TAG, "Publisher onStreamCreated")
    }

    override fun onStreamDestroyed(publisherKit: PublisherKit, stream: Stream) {
        Log.i(LOG_TAG, "Publisher onStreamDestroyed")
        mPublisherViewContainer!!.removeAllViews()
    }

    override fun onError(publisherKit: PublisherKit, opentokError: OpentokError) {
        Log.e(LOG_TAG, "Publisher error: " + opentokError.message)
    }

    override fun onClick(view: View) {
        // code block
        if (view.id == R.id.exitOne2OneSessionButton) {
            confirmExit()
        }
    }

    companion object {
        private val LOG_TAG = "One2OneFragment"
        private val RC_SETTINGS_SCREEN_PERM = 123
        private val RC_VIDEO_APP_PERM = 124
    }
    inner  class BackgroundSound : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            val player = MediaPlayer.create(activity, R.raw.piano_theme)
            player.isLooping = true // Set looping
            player.setVolume(1.0f, 1.0f)
            player.start()
            return null
        }
    }
}// Required empty public constructor
