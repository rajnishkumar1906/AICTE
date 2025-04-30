package com.example.aicte

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.pwd)
        val cnfrmpassword = findViewById<EditText>(R.id.cnfrmpwd)
        val signupBtn = findViewById<Button>(R.id.signUp)
        val loginText = findViewById<TextView>(R.id.login)
        val back = findViewById<ImageButton>(R.id.back)

        signupBtn.setOnClickListener {
            val userEmail = email.text.toString().trim()
            val userPassword = password.text.toString().trim()
            val confirmPassword = cnfrmpassword.text.toString().trim()
            val userName = findViewById<EditText>(R.id.fullname).text.toString().trim() // Get username

            if (userEmail.isEmpty() || userPassword.isEmpty() || confirmPassword.isEmpty() || userName.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (userPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val uid = user?.uid ?: ""
                            val userMap = mapOf(
                                "email" to userEmail,
                                "username" to userName // Save username here
                            )

                            FirebaseDatabase.getInstance().reference
                                .child("users")
                                .child(uid)
                                .child("info")
                                .setValue(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Signed Up Successfully", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, User::class.java)
                                    intent.putExtra("fromSignup", true)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        back.setOnClickListener {
            finish()
        }
    }
}
