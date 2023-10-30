package com.example.birdspotterapppoe


import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegistrationActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

    }

    fun register(view: View) {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            // Check if the username already exists in the database
            if (checkUsernameExists(username)) {
                showToast("Username already exists. Choose a different username.")
            } else {
                // Insert the new user's credentials into the database
                if (insertUser(username, password)) {
                    showToast("Registration successful. You can now log in.")
                    val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    showToast("Registration failed. Please try again.")
                }
            }
        } else {
            showToast("Please enter both a username and a password.")
        }
    }

    fun navigateToMainActivity(view: View) {
        val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkUsernameExists(username: String): Boolean {
        val dbHelper = DatabaseHelper(this, null)
        val db = dbHelper.readableDatabase

        val tableName = "users"
        val usernameColumn = "username"

        val query = "SELECT * FROM $tableName WHERE $usernameColumn = ?"
        val selectionArgs = arrayOf(username)

        val cursor = db.rawQuery(query, selectionArgs)

        val exists = cursor.count > 0

        cursor.close()
        db.close()

        return exists
    }

    private fun insertUser(username: String, password: String): Boolean {
        val dbHelper = DatabaseHelper(this, null)
        val db = dbHelper.writableDatabase

        val tableName = "users"
        val usernameColumn = "username"
        val passwordColumn = "password"

        val values = ContentValues()
        values.put(usernameColumn, username)
        values.put(passwordColumn, password)

        val result = db.insert(tableName, null, values)

        db.close()

        return result != -1L
    }
}
