package com.example.studypal


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_solo_session_settings.*

/**
 * A simple [Fragment] subclass.
 */
class SoloSessionSettingsFragment : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController

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

}
