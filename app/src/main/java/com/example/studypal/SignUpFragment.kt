package com.example.studypal


import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_sign_up.*
import java.util.*
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.android.synthetic.main.activity_main.*


/**
 * A simple [Fragment] subclass.
 */
class SignUpFragment : Fragment(), View.OnClickListener {
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore?=null
    private var navController:NavController?=null

    val c = Calendar.getInstance()
    val year = c.get(Calendar.YEAR)
    val month = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        register_button.setOnClickListener(this)
        birthDate.setOnClickListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

    }
    override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }
    fun showDatePicker(view: View) {
        val dpd = context?.let {
            DatePickerDialog(
                it,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    // Display Selected date in Toast
                    Toast.makeText(
                        activity,
                        """$dayOfMonth-${monthOfYear + 1}-$year""",
                        Toast.LENGTH_LONG
                    ).show()
                    birthDate.setText("""$dayOfMonth - ${monthOfYear + 1} - $year""")
                },
                year,
                month,
                day
            )
        }
        c.set(year,month,day)
        dpd?.show()
    }
    private fun register(email:String, password:String)
    {
        if(!validate()) {
            return
        }
        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "task success")
                val user = mAuth!!.currentUser
                val newUser = User(userName.text.toString(),
                    email,
                    c.time,
                    phoneNumber.text.toString())
                db!!.collection("users").document(user!!.uid).set(newUser)
                Log.d(TAG,newUser.toString())
                navController!!.navigate(R.id.action_signUpFragment_to_homeFragment)
            }
            else {
                Log.d(TAG, "Failed to register")
                Toast.makeText(activity,   "Failed to register", Toast.LENGTH_LONG).show()
                val e = it.getException() as FirebaseAuthException
                Log.e(TAG, "Failed Register" ,e)
            }
        }
    }
    private fun validate(): Boolean{
        val valid = true
        /*// Empty check
        nonEmptyList(userName, email, password, password, birthDate) { view, msg ->
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
            // The view will contain the exact view which is empty
            // The msg will contain the error message
        }
        email.validEmail() {
            // This method will be called when myEmailStr is not a valid email.
            Toast.makeText(activity, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
        }
        // Check password validity
        password.validator()
            .minLength(5)            //.atleastOneNumber() //.atleastOneSpecialCharacters() //.atleastOneUpperCase()
            .addErrorCallback {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                // it will contain the right message.
                // For example, if edit text is empty,
                // then 'it' will show "Can't be Empty" message
            }
            .check()
        // Check password and confirm password fields
        password.validator()
            .textEqualTo(repeatPassword.text.toString())
            .addErrorCallback{
                Toast.makeText(activity, getString(R.string.password_confirm_failed), Toast.LENGTH_SHORT).show()
            }*/
        return valid
    }
    override fun onClick(v: View?) {
        val i = v!!.id
        when (i) {
            R.id.register_button -> register(email.text.toString(), password.text.toString())
            R.id.birthDate -> showDatePicker(v)
        }
    }
    companion object {
        private const val TAG = "SignUpActivity"
    }
}
