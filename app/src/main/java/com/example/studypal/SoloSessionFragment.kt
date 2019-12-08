package com.example.studypal

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_solo_session.*


import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class SoloSessionFragment : Fragment(), View.OnClickListener {

    private lateinit var rtcClient: RTCClient
    private lateinit var sessionCountDownTimer: CountDownTimer
    private lateinit var breakCountDownTimer: CountDownTimer
    private lateinit var navController: NavController


    // TODO: these will be intput from previous fragment
    private var sessionMins: Long = 1
    private var breakMins: Long =1
    private var sessionCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        createTimers()
        sessionCountDownTimer.start()

    }
    private fun createTimers(){
        sessionCountDownTimer = object : CountDownTimer(sessionMins*60*100,1000){
            override fun onTick(milisUntilFinished: Long) {
                var minsLeft = TimeUnit.MILLISECONDS.toMinutes(milisUntilFinished)
                var secondsLeft = TimeUnit.MILLISECONDS.toSeconds(milisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minsLeft)
                var timeLeftText:String? = String.format(resources.getString(R.string.timer_text), minsLeft, secondsLeft)
                timeLeftTextWiev.text = timeLeftText

            }
            override fun onFinish() {
                Log.d("timer", "timer finished decrement counter and reset values")
                sessionCount--

                if(sessionCount == 0){
                    // TODO: End session, show end screen
                    // onStop() or onDestroy()
                    Log.d("SOLO", "sessions finished")
                    closeCall()


                }
                else{
                    Toast.makeText(context, "Session has ended, take a break", Toast.LENGTH_LONG).show()
                    stateTextView.text = getString(R.string.break_text)
                    breakCountDownTimer.start()
                }
            }
        }
        breakCountDownTimer = object : CountDownTimer(breakMins*60*100,1000){
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

    private fun confirmExit() {
        val alertDialog = AlertDialog.Builder(activity!!)
            //set icon
            .setIcon(android.R.drawable.ic_dialog_alert)
            //set title
            .setTitle("Are you sure to exit this session?")
            //set message
            .setMessage("If yes then application will close")
            //set positive button
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                closeCall()
            }
            //set negative button
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(context, "Nothing Happened", Toast.LENGTH_LONG).show()
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
        // TODO stop webrtc
        navController.navigate(R.id.action_soloSessionFragment_to_endSessionFragment)
    }
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.exitSessionButton -> confirmExit()
        }
    }
}
