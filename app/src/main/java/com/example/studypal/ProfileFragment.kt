package com.example.studypal


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    var listOfSessions: ArrayList<SessionData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        loadSessionData(auth.currentUser!!, object:SessionDataCallBack{
            override fun onCallback(sessionHistory: ArrayList<SessionData>) {
                viewManager = LinearLayoutManager(context)
                viewAdapter = profileAdaptor(listOfSessions)
                recyclerView = myRecyclerView
                var mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                recyclerView.layoutManager = mLayoutManager
                viewAdapter = profileAdaptor(listOfSessions)
                recyclerView.adapter = viewAdapter
            }
        })
    }
    private fun loadSessionData(user: FirebaseUser, sessionDataCallBack:SessionDataCallBack){
        db.collection("users").document(user.uid).collection("sessionHistory")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    listOfSessions.add(document.toObject(SessionData::class.java))
                }
                listOfSessions.sortWith(compareBy {it.sessionDate})
                sessionDataCallBack.onCallback(listOfSessions)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    companion object {
        private const val TAG = "profileFragment"
    }
}
interface SessionDataCallBack {
    fun onCallback(sessionHistory: ArrayList<SessionData>)
}
