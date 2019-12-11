package com.example.studypal


import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_end_session.*


/**
 * A simple [Fragment] subclass.
 */
class EndSessionFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    val args: EndSessionFragmentArgs by navArgs()
    private var db: FirebaseFirestore?=null
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_end_session, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        advance_button.setOnClickListener(this)
        val time = System.currentTimeMillis()
        val sessionData = SessionData(
            args.sessionMins,
            args.totalMinsInSession.toInt(),
            args.sessionCount,
            args.inSession,
            args.breakMins,
            time)
        // TODO: Add focus to the SessionData and database as well
        val currentUserID = auth?.currentUser!!.uid
        db?.collection("users")!!.document(currentUserID).collection("SessionHistory").document(time.toString()).set(sessionData)

        val message = if( args.totalMinsInSession >  60) {
            val sessionDurationHrs = args.totalMinsInSession / 60
            val sessionDurationMnts = args.totalMinsInSession % 60
            "Well done! You have studied for $sessionDurationHrs hours and $sessionDurationMnts minutes."
        } else {
            "Well done! You have studied for ${args.totalMinsInSession} minutes."
        }
        messageText.text = message

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.advance_button -> navController.navigate(R.id.action_endSessionFragment_to_navigation_home)
        }
    }

}
