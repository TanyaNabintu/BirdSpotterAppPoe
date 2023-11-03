package com.example.birdspotterapppoe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.birdspotterapppoe.Constants.TAG
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFireStore: FirebaseFirestore

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerLink: TextView
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        mFireStore = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerLink = findViewById(R.id.registerLink)
        loginButton = findViewById(R.id.loginButton)

    }

    fun login(view: View) {
        val user = User()
        user.email = emailEditText.text.toString()
        user.password = passwordEditText.text.toString()

        if (user.email.isNotEmpty() && user.password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(user.email, user.password)
                .addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val userLogged = auth.currentUser?.uid
                        Log.e(TAG,"user.email:${user.email} and user.password ${user.password}")
                        Log.e(TAG,"user logged uuid is : $userLogged")
                        showToast("Successfully login")
                        val intent = Intent(this@MainActivity, MainActivity2::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        showToast("Error: ${task.exception.toString()}")
                    }
                }
        } else {
            showToast("Please enter both an email and a password.")
        }

    }

    fun navigateToRegistration(view: View) {
        val intent = Intent(this@MainActivity, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    /*
    private fun authenticateUser(username: String, password: String): Boolean {
        val dbHelper = DatabaseHelper(this, null) // Initialize your SQLite database helper
        val db = dbHelper.readableDatabase // Get a readable database instance

        // Define the table name and column names in your database
        val tableName = "users"
        val usernameColumn = "username"
        val passwordColumn = "password"

        // Construct a query to check if the provided credentials exist in the table
        val query = "SELECT * FROM $tableName WHERE $usernameColumn = ? AND $passwordColumn = ?"
        val selectionArgs = arrayOf(username, password)

        // Execute the query
        val cursor = db.rawQuery(query, selectionArgs)

        val authenticated = cursor.count > 0

        // Close the cursor and database
        cursor.close()
        db.close()

        return authenticated
    }

     */
}
