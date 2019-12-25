package com.example.studypal



import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_home.*


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        soloSessionSettingsButton.setOnClickListener(this)
        one2OneSessionButton.setOnClickListener(this)
    }
    override fun onStart() {
        super.onStart()
        Log.d("onStart", "onstart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "On Resume" )
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    override fun onClick(v:View){
        val i = v.id
        when (i) {

            R.id.soloSessionSettingsButton -> {
                val sessionType = "Solo"
                val action = HomeFragmentDirections.actionNavigationHomeToSoloSessionSettingsFragment(sessionType)
                navController.navigate(action)
            }

            R.id.one2OneSessionButton -> {
                val sessionType = "One2One"
                val action = HomeFragmentDirections.actionNavigationHomeToSoloSessionSettingsFragment(sessionType)
                navController.navigate(action)
            }
        }
    }
    companion object {
        private const val TAG = "homePageActivity"
    }
}
