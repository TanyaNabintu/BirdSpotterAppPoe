package com.example.birdspotterapppoe


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.birdspotterapppoe.Constants.TAG
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFireStore: FirebaseFirestore

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton=findViewById(R.id.registerButton)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        mFireStore = FirebaseFirestore.getInstance()

    }
    fun register(view: View) {
        val user = User()
         user.email = emailEditText.text.toString()
         user.password = passwordEditText.text.toString()

        if (user.email.isNotEmpty() && user.password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(user.email, user.password)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(Constants.TAG, "createUserWithEmail:success")
                        val firebaseUser = auth.currentUser
                        user.id = firebaseUser?.uid.toString()
                        saveUserToFirestore(user)
                        showToast("Registration successful. You can now log in.")
                        Log.e(TAG,"user.email:${user.email} and user.password ${user.password}")

                        val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                        startActivity(intent)


                    } else {
                        // If sign in fails, display a message to the user.
                        showToast(task.exception.toString())
                        Log.w(Constants.TAG, "createUserWithEmail:failure", task.exception)
                    }
                }
        } else {
            showToast("Please enter both an email and a password.")
        }
    }


    private fun saveUserToFirestore( user: User) {
        mFireStore.collection(Constants.FirebaseCollectionUsers)
            .document(user.id)
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                showToast("User successfully added")
            }.addOnFailureListener { exception ->
                showToast("Fail to add user")
                Log.e(Constants.TAG, exception.message.toString())
            }
    }
    fun navigateToMainActivity(view: View) {
        val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
        startActivity(intent)
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
