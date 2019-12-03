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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    val db = FirebaseFirestore.getInstance()
    private val fuser = FirebaseAuth.getInstance().currentUser


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        streamSettingsButton.setOnClickListener(this)
        soloSessionSettingsButton.setOnClickListener(this)
        pairWithPalSettingsButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        Log.d("onStart", "onstart")
        getUserData(fuser!!.uid)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private fun getUserData(uid:String){
        Log.d(TAG, "inside getUserData")
        val docRef = db.collection("users").document(uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    var userData = document.toObject(User::class.java)
                    if (userData != null) {
                        Log.d(TAG,userData.toString())
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
        .addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }
    private fun signOut() {
        // Sign Out
        FirebaseAuth.getInstance().signOut()

    }
    override fun onClick(v:View){
        val i = v.id
        when (i) {
            /*R.id.sign_out_button -> {
                signOut()
                val action = HomeFragmentDirections.actionHomeFragmentToMainFragment("signOut")
                navController.navigate(action)
            }*/
            R.id.streamSettingsButton -> navController.navigate(R.id.action_navigation_home_to_streamSettingsFragment)
            R.id.soloSessionSettingsButton -> navController.navigate(R.id.action_navigation_home_to_soloSessionSettingsFragment)
            R.id.pairWithPalSettingsButton -> navController.navigate(R.id.action_navigation_home_to_pairWithPalSettingsFragment)

        }
    }
    companion object {
        private const val TAG = "homePageActivity"
    }
}
