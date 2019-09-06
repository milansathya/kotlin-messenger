package com.app.kotlinmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*


class RegisterActivity : AppCompatActivity() {

    private val LOG_TAG = "RegisterActivity"
    private var selectedPhotoUri: Uri? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_select_photo_button.setOnClickListener {
            invokePhotoSelector()
        }

        register_confirm_button.setOnClickListener {
            performRegister()
        }

        register_already_have_account_textview.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun invokePhotoSelector() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    private fun performRegister() {
        val email = register_email_textfield.text.toString()
        val password = register_password_textfield.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Log.d(LOG_TAG, "Invalid email or password")
            Toast.makeText(this, "Please enter a valid email and password", Toast.LENGTH_SHORT)
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d(LOG_TAG, "User Created: ${it.result?.user?.uid}")

                uploadImageToFirebase()
            }
            .addOnFailureListener {
                Log.d(LOG_TAG,"Registration failed: ${it.message}")
            }

        Log.d(LOG_TAG, "email: $email")
        Log.d(LOG_TAG, "password: $password")
    }

    private fun uploadImageToFirebase() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val reference = FirebaseStorage.getInstance().getReference("images/$filename")
        reference.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(LOG_TAG, "Photo uploaded successfully!")

                reference.downloadUrl.addOnSuccessListener {
                    Log.d(LOG_TAG, "Profile image saved successfully. $it")
                    saveToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.e(LOG_TAG, "Photo uploaded failed!")
            }
    }

    private fun saveToFirebaseDatabase(imageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val reference = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = UserInfo(uid, register_username_textfield.text.toString(), imageUrl)
        reference.setValue(user)
            .addOnSuccessListener {
                Log.d(LOG_TAG, "User saved to Firebase DB successfully")
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val bitmapDrawable = BitmapDrawable(bitmap)
            register_select_photo_button.setBackgroundDrawable(bitmapDrawable)
        }
    }
}

class UserInfo(uid: String, username: String, profileImageUrl: String)

