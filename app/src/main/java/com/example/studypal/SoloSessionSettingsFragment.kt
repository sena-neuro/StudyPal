package com.example.studypal


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_solo_session_setings.*
import kotlinx.android.synthetic.main.fragment_stream_settings.*

/**
 * A simple [Fragment] subclass.
 */

class SoloSessionSettingsFragment : Fragment(),View.OnClickListener {

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        startSoloSessionButton.setOnClickListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stream_settings, container, false)
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.startSoloSessionButton -> navController.navigate(R.id.action_soloSessionSetingsFragment_to_soloSessionFragment)

        }
    }
}