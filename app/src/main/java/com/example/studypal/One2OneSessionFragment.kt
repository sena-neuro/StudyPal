package com.example.studypal

import android.Manifest
import android.app.AlertDialog
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.opentok.android.*
import kotlinx.android.synthetic.main.fragment_one2_one_session.*
import org.json.JSONException
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class One2OneSessionFragment: Fragment(), Session.SessionListener,
    PublisherKit.PublisherListener, View.OnClickListener {

    private var mSession: Session? = null
    private val sessionType:String = "One2One"
    private var mPublisher: Publisher? = null
    private var navController: NavController? = null
    val args: SoloSessionFragmentArgs by navArgs()
    private var mPublisherViewContainer: FrameLayout? = null
    private var mSubscriberViewContainer: FrameLayout? = null
    private var mSubscriber: Subscriber? = null
    private var inSession = false
    private var totalMinsInSession: Long = 0
    private var sessionCount = 0
    private var firstTimeFlag = true
    private var remainingMinutes: Long=0
    private val sessionViewModel: SessionViewModel by lazy {
        ViewModelProviders.of(this).get(SessionViewModel::class.java)
    }
    private val milisChangeObserver =
        Observer<Long> { milisRemaining -> displayTime(milisRemaining) }
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

    private fun displayTime(milisRemaining: Long?) {
        remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(milisRemaining!!)
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
        sessionViewModel.milisChangeNotifier.observe(this, milisChangeObserver)
        sessionViewModel.onSession.observe(this, sessionObserver)
        sessionViewModel.remainingSessionCount.observe(this, remainingSessionObserver)
        // This callback will only be called when MyFragment is at least Started.
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button
            confirmExit()
        }
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
        finding_pal_animation_view.setAnimation(R.raw.findingpal)
        finding_pal_animation_view.playAnimation()
        val bMilis =args.breakMins
        val sessionCount = args.sessionCount
        val sMilis = args.sessionMins
        sessionViewModel.createTimers(sessionCount, sMilis, bMilis)
        navController = Navigation.findNavController(view)
        val exitButton =
            activity!!.findViewById<FloatingActionButton>(R.id.exitOne2OneSessionButton)
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
        if (inSession) {
            totalMinsInSession += remainingMinutes
            Log.d("Mins:", remainingMinutes.toString())
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
    interface LoadingImplementation {
        fun onFinishedLoading()
    }
}// Required empty public constructor
