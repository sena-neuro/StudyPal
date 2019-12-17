package com.example.studypal


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_solo_session.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class SoloSessionFragment : Fragment(), View.OnClickListener {

    private lateinit var rtcClient: RTCClient
    private lateinit var mBackgroundSound: BackgroundSound
    val args: SoloSessionFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private var inSession: Boolean = false
    private var totalMinsInSession:Long = 0
    private var sessionCount:Int = 0
    private var firstTimeFlag:Boolean = true
    private var milis:Long=0
    private val sessionViewModel: SessionViewModel by lazy {
        ViewModelProviders.of(this).get(SessionViewModel::class.java)
    }
    private val milisChangeObserver = Observer<Long> {
        value -> value?.let{displayTime(value)
        milis= value}
    }

    private val sessionObserver = Observer<Boolean> {
        value -> value?.let{
            if( value ) {
                if (!firstTimeFlag)
                    showNotification("Break End!!", "Your break has ended, Start a new session")
                firstTimeFlag = false
                stateTextView.text = "In study session"
                inSession = true
            }
            else {
                showNotification("Congratulations", "You have finished a session, take a break")
                stateTextView.text = "In Break"
                // Update total mins in session, session count and inSession Flag
                totalMinsInSession += args.sessionMins
                sessionCount += 1
                inSession = false
            }
        }
    }
    private val remainingSessionObserver = Observer<Int> {
            value -> value?.let{
            Log.d(TAG, "Rem Sess:$it")
            if (it == 0){
                closeCall()
            }
    }

    }

    private fun displayTime(value: Long) {
        Log.d(TAG, "oley be observed")
        val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(value)
        val remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(value) -
                TimeUnit.MINUTES.toSeconds(remainingMinutes)
        val timeLeftText:String = String.format(resources.getString(R.string.timer_text), remainingMinutes, remainingSeconds)
        timeLeftTextView.text = timeLeftText

    }
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mBackgroundSound = BackgroundSound()

        // This callback will only be called when MyFragment is at least Started.
    // Create a ViewModel the first time the system calls an activity's onCreate() method.
    // Re-created activities receive the same MyViewModel instance created by the first activity.
    sessionViewModel.milisChangeNotifier.observe(this,milisChangeObserver)
    sessionViewModel.onSession.observe(this, sessionObserver)
    sessionViewModel.remainingSessionCount.observe(this,  remainingSessionObserver)
    //lifecycle.addObserver(sessionViewModel)

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
        return inflater.inflate(R.layout.fragment_solo_session, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        exitSessionButton.setOnClickListener(this)
        checkCameraPermission()
        sessionViewModel.createTimers(args.sessionCount, args.sessionMins, args.breakMins)

    }
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(context!!, CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        } else {
            onCameraPermissionGranted()
        }
    }

    override fun onResume() {
        super.onResume()
        mBackgroundSound.execute(null)
    }
    override fun onPause() {
        mBackgroundSound.cancel(true)
        super.onPause()
    }

    private fun onCameraPermissionGranted() {
        rtcClient = RTCClient(activity!!.application, local_view)
        rtcClient.startLocalVideoCapture()
    }
    private fun requestCameraPermission(dialogShown: Boolean = false) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, CAMERA_PERMISSION) && !dialogShown) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(activity!!, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(context!!)
            .setTitle("Camera Permission Required")
            .setMessage("This app need the camera to function")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestCameraPermission(true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onCameraPermissionDenied()
            }
            .show()
    }

    private fun confirmExit() {
        val alertDialog = AlertDialog.Builder(activity!!)
            //set icon
            .setIcon(android.R.drawable.ic_dialog_alert)
            //set title
            .setTitle("Are you sure to exit this session?")
            //set message
            .setMessage("If you exit during a session, your focus score will be affected negatively")
            //set positive button
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                closeCall()
            }
            //set negative button
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onCameraPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }
    private fun onCameraPermissionDenied() {
        Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_LONG).show()
    }

    private fun closeCall(){
        // TODO stop webrtc
        sessionViewModel.closeCall()
        if(inSession){
            val mins = TimeUnit.MILLISECONDS.toMinutes(milis)
            totalMinsInSession += args.sessionMins - mins
        }
        val action = SoloSessionFragmentDirections.
            actionSoloSessionFragmentToEndSessionFragment(inSession, totalMinsInSession, args.sessionMins.toInt(), sessionCount, args.breakMins.toInt())
        navController.navigate(action)
    }
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val TAG = "SOLO SESSION"

    }
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.exitSessionButton -> confirmExit()
        }
    }
    private fun showNotification(title: String, message: String) {
        val channelID = "${context!!.packageName} - StudyPal"
        val mNotificationManager = activity!!.getSystemService( Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelID,
                "StudyPal",
                NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Timer Notifications"
            channel.canShowBadge()
            mNotificationManager.createNotificationChannel(channel)
        }
        val mBuilder = NotificationCompat.Builder(context!!,channelID)
            .setSmallIcon(R.drawable.ic_timer_black_24dp) // notification icon
            .setContentTitle(title) // title for notification
            .setContentText(message)// message for notification
            .setAutoCancel(true) // clear notification after click
        mBuilder.setAutoCancel(true)
        mNotificationManager.notify(0, mBuilder.build())
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
}
