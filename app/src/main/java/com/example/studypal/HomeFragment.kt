package com.example.studypal


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment(), View.OnClickListener {
    private var userData:User?=null
    private lateinit var textMessage: TextView
    private lateinit var navController: NavController
    val db = FirebaseFirestore.getInstance()
    private val fuser = FirebaseAuth.getInstance().currentUser


    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                textMessage.setText("Home")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                textMessage.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                textMessage.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val navView: BottomNavigationView = nav_view
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userData = getUserData(fuser!!.uid)
        val welc_text:String
        if (fuser.providerId.equals("google.com")) {
            val name = fuser.displayName.toString()
            welc_text = "Welcome,$name"
        }
        else{
            welc_text = "Welcome,${userData?.username}"

        }
        Toast.makeText(context, welc_text, Toast.LENGTH_SHORT).show()
        welcomeText.text = welc_text

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private fun getUserData(uid:String):User?{
        val docRef = db.collection("users").document(uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    userData = document.toObject(User::class.java)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
        .addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
        return userData
    }
    private fun signOut() {
        // Sign Out
        FirebaseAuth.getInstance().signOut()

    }
    override fun onClick(v:View){
        val i = v.id
        when (i) {
            R.id.sign_out_button -> {
                signOut()
            }
        }
    }
    companion object {
        private const val TAG = "homePageActivity"
    }
}
