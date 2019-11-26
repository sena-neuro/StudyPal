package com.example.studypal


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_pair_with_pal_settings.*
import kotlinx.android.synthetic.main.fragment_solo_session_setings.*

/**
 * A simple [Fragment] subclass.
 */
class pairWithPalSettingsFragment : Fragment(),View.OnClickListener {

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        pairWithPalButton.setOnClickListener(this)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pair_with_pal_settings, container, false)
    }
    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.pairWithPalButton -> navController.navigate(R.id.action_pairWithPalSettingsFragment_to_pairWithPalFragment3)

        }
    }


}
