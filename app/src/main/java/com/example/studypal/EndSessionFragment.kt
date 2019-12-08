package com.example.studypal


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_end_session.*

/**
 * A simple [Fragment] subclass.
 */
class EndSessionFragment : Fragment(), View.OnClickListener, RatingBar.OnRatingBarChangeListener {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_end_session, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        advance_button.setOnClickListener(this)
        val sessionDuration = 100
        val message = if( sessionDuration >  60) {
            val sessionDurationHrs = sessionDuration / 60
            val sessionDurationMnts = sessionDuration % 60
            "Well done! You have studied for $sessionDurationHrs hours and $sessionDurationMnts minutes."
        } else {
            "Well done! You have studied for $sessionDuration minutes."
        }
        messageText.text = message
        ratingBar.setOnRatingBarChangeListener(this)

    }

    // TODO edit
    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        val sessionRating = rating
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.advance_button -> navController.navigate(R.id.action_endSessionFragment_to_navigation_home)
        }
    }

}
