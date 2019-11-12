package com.example.studypal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.wajahatkarim3.easyvalidation.core.collection_ktx.nonEmptyList
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.activity_sign_up.*
class SignUpActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()
        registerButton.setOnClickListener{
            OnRegister()
        }
    }

    private fun OnRegister(){
        // Empty check
        nonEmptyList(userName, email, password, password, birthDate) { view, msg ->
            Toast.makeText(this@SignUpActivity, msg, Toast.LENGTH_SHORT).show()
            // The view will contain the exact view which is empty
            // The msg will contain the error message
        }
        email.validEmail() {
            // This method will be called when myEmailStr is not a valid email.
            Toast.makeText(this@SignUpActivity, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
        }
        // Check password validity
        password.validator()
            .minLength(5)            //.atleastOneNumber() //.atleastOneSpecialCharacters() //.atleastOneUpperCase()
            .addErrorCallback {
                Toast.makeText(this@SignUpActivity, it, Toast.LENGTH_SHORT).show()
                // it will contain the right message.
                // For example, if edit text is empty,
                // then 'it' will show "Can't be Empty" message
            }
            .check()
        // Check password and confirm password fields
        password.validator()
            .textEqualTo(repeatPassword.text.toString())
            .addErrorCallback{
                Toast.makeText(this@SignUpActivity, getString(R.string.password_confirm_failed), Toast.LENGTH_SHORT).show()


            }
        mAuth!!.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in: success
                // update UI for current User
                val user = mAuth!!.currentUser
                updateUI(user)
            } else {
                // Sign in: fail
                updateUI(null)
            }
        }

    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, homePageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        } else {

        }
    }

}
