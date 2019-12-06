package com.example.studypal


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
    private var onSession:Boolean = false
    val args: SoloSessionFragmentArgs by navArgs()
    private var remainingSessions = args.sessionCount

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_solo_session, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraPermission()
        createTimers()
        sessionCountDownTimer.start()
        onSession = true
    }
    private fun createTimers(){
        sessionCountDownTimer = object : CountDownTimer(args.sessionMins*60*1000,1000){
            override fun onTick(milisUntilFinished: Long) {
                val minsLeft = TimeUnit.MILLISECONDS.toMinutes(milisUntilFinished)
                val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(milisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minsLeft)
                val timeLeftText:String? = String.format(resources.getString(R.string.timer_text), minsLeft, secondsLeft)
                timeLeftTextWiev.text = timeLeftText
            }
            override fun onFinish() {
                Log.d("timer", "timer finished decrement counter and reset values")
                remainingSessions--
                Toast.makeText(context, "Your ${remainingSessions}th session has ended, take a break", Toast.LENGTH_LONG).show()
                stateTextView.text = getString(R.string.break_text)
                breakCountDownTimer.start()
                onSession = false
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
                Log.d("timer", " break timer finished decrement counter and reset values")
                Toast.makeText(context, "Break has ended, start your session", Toast.LENGTH_LONG).show()
                stateTextView.text = getString(R.string.session_text)
                sessionCountDownTimer.start()
                onSession = true
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
    override fun onPause() {
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
        Log.d("closeCall", "Not implemented")
    }
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.closeCallButton -> closeCall()
        }
    }
}
