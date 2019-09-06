package com.app.kotlinmessenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        login_login_button.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = login_email_textfield.text.toString()
        val password = login_password_textfield.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {

            }
            .addOnFailureListener {

            }
    }

}