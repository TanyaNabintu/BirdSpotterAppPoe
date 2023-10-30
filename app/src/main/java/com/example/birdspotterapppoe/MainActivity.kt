package com.example.birdspotterapppoe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerLink = findViewById(R.id.registerLink)
    }

    fun login(view: View) {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Authenticate the user using SQLite
        if (authenticateUser(username, password)) {
            // Authentication successful, navigate to the main app screen.
            val intent = Intent(this@MainActivity, MainActivity2::class.java)
            startActivity(intent)
            finish()
        } else {
            // Authentication failed, show an error message.
            showToast("Invalid username or password.")
        }
    }

    fun navigateToRegistration(view: View) {
        val intent = Intent(this@MainActivity, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

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
}
