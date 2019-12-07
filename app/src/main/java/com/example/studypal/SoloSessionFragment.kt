package com.example.studypal


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
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
    private lateinit var sessionCountDownTimer: CountDownTimer
    private lateinit var breakCountDownTimer: CountDownTimer
    private lateinit var mBackgroundSound: BackgroundSound
    private var onSession:Boolean = false
    val args: SoloSessionFragmentArgs by navArgs()
    private var remainingSessions = 0
    private lateinit var navController: NavController
    private var minsLeftInSession:Long = 0


        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBackgroundSound = BackgroundSound()

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
        remainingSessions = args.sessionCount
        minsLeftInSession = args.sessionMins
        navController = Navigation.findNavController(view)
        checkCameraPermission()
        createTimers()
        sessionCountDownTimer.start()
        onSession = true
    }
    private fun createTimers(){
        sessionCountDownTimer = object : CountDownTimer(args.sessionMins*60*1000,1000){
            override fun onTick(milisUntilFinished: Long) {
                minsLeftInSession = TimeUnit.MILLISECONDS.toMinutes(milisUntilFinished)
                val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(milisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minsLeftInSession)
                val timeLeftText:String? = String.format(resources.getString(R.string.timer_text), minsLeftInSession, secondsLeft)
                timeLeftTextWiev.text = timeLeftText
            }
            override fun onFinish() {

                Log.d("timer", "timer finished decrement counter and reset values")
                remainingSessions--
                if(remainingSessions == 0){
                    Log.d("SOLO", "sessions finished")
                    showNotification("Congratulations!!", "You have finished your last planned sessions")
                    closeCall()
                }
                else{
                    showNotification("Congratulations", "You have finished a session, take a break")
                    stateTextView.text = getString(R.string.break_text)
                    onSession = false
                    breakCountDownTimer.start()
                }
            }
        }
        breakCountDownTimer = object : CountDownTimer(args.breakMins*60*1000,1000){
            override fun onTick(milisUntilFinished: Long) {
                val minsLeft = TimeUnit.MILLISECONDS.toMinutes(milisUntilFinished)
                val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(milisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minsLeft)
                val timeLeftText:String? = String.format(resources.getString(R.string.timer_text), minsLeft, secondsLeft)
                timeLeftTextWiev.text = timeLeftText
            }
            override fun onFinish() {
                showNotification("Break End!!", "Your break has ended, Start a new session")
                Log.d("timer", " break timer finished decrement counter and reset values")
                Toast.makeText(context, "Break has ended, start your session", Toast.LENGTH_LONG).show()
                stateTextView.text = getString(R.string.session_text)
                onSession = true
                sessionCountDownTimer.start()
            }
        }
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
        sessionCountDownTimer.cancel()
        breakCountDownTimer.cancel()
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
        sessionCountDownTimer.cancel()
        breakCountDownTimer.cancel()
        // Both of these values will be transferred to the next fragment
        val completedSessions = args.sessionCount - remainingSessions
        val totalMinsInSession = completedSessions * args.sessionMins
        if (onSession){
            totalMinsInSession + minsLeftInSession
        }
        val action = SoloSessionFragmentDirections.actionSoloSessionFragmentToEndSessionFragment(onSession,totalMinsInSession,args.sessionMins.toInt(),args.sessionCount,args.breakMins.toInt())
        navController.navigate(action)
        // TODO: stop webrtc
    }
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.closeCallButton -> confirmExit()
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
