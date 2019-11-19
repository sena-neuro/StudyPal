package com.example.studypal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home_page.*

class homePageActivity : AppCompatActivity(), View.OnClickListener {
    private var db: FirebaseFirestore?=null
    private var userData:User?=null
    private lateinit var textMessage: TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        val user = FirebaseAuth.getInstance().currentUser
        db = FirebaseFirestore.getInstance()
        user?.let {
            val uid = user.uid
            // Name, email address, and profile photo Url
            if (user.getProviderId().equals("google.com")) {
                val name = user.displayName
            }
            else{
                val docRef = db!!.collection("users").document(uid)
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
            }

            val welc_text = "Welcome,${userData?.username}"
            welcomeText.text = welc_text
        }
        sign_out_button.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        Log.d("TAG", "BackIsPressed")
        signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        super.onBackPressed()
    }
    override fun onClick(v:View){
        val i = v.id
        when (i) {
            R.id.sign_out_button -> {
                signOut()
            }
        }
    }
    private fun signOut() {
        // sign out
        FirebaseAuth.getInstance().signOut()
    }
    companion object {
        private const val TAG = "homePageActivity"
    }
}
