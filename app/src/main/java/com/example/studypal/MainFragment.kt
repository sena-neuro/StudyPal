package com.example.studypal


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment(), View.OnClickListener {
    private lateinit var googleSignInClient: GoogleSignInClient
    // Initialize Firebase Auth
    val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var provider: String? = null
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inflate the layout for this fragment
        val WEB_CLIENT_ID =
            "977955202582-5ml9vjf3j4joh0etcstlit4dl1450n27.apps.googleusercontent.com"
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = activity?.let { GoogleSignIn.getClient(it, gso) }!!
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
        google_sign_in_button.setOnClickListener(this)
        email_sign_in_button.setOnClickListener(this)
        signUpButton.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        val args = arguments?.let { MainFragmentArgs.fromBundle(it) }
        Log.d(TAG, args!!.action)
        if (args.action.equals("signOut")) {
            signOut()
        }
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navController.navigate(R.id.action_mainFragment_to_homeFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fUser = auth.currentUser!!
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    Log.d(TAG, fUser.uid)
                    val docRef = db.collection("users").document(fUser.uid)
                    docRef.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                Log.d(TAG, "No such document")
                                val newUser = User(fUser.displayName,
                                    fUser.email,
                                    fUser.phoneNumber,
                                    null
                                )
                                db.collection("users").document(fUser.uid).set(newUser)
                                Log.d(TAG, "Adding new user $newUser")
                            }
                        else
                            Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        navController.navigate(R.id.action_mainFragment_to_homeFragment)
                    }.addOnFailureListener { exception ->
                        Log.d(TAG, "get failed with ", exception)
                    }
                }
                else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toast.makeText(activity,   "Authorization failed", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun signOut() {
        // Firebase sign out
        auth.signOut()

        // Google sign out
        googleSignInClient.signOut()
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.email_sign_in_button -> navController.navigate(R.id.action_mainFragment_to_signInFragment)
            R.id.signUpButton -> navController.navigate(R.id.action_mainFragment_to_signUpFragment)
            R.id.google_sign_in_button -> signIn()
        }
    }
    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}
