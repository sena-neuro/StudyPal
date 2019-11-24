package com.example.studypal



import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.*



/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        streamSettingsButton.setOnClickListener(this)
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
            R.id.streamSettingsButton -> navController.navigate(R.id.action_navigation_home_to_streamSettingsFragment)
        }
    }
    companion object {
        private const val TAG = "homePageActivity"
    }
}
