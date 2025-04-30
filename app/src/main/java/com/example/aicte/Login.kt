package com.example.aicte

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.login)
        val signupText = findViewById<TextView>(R.id.signup)
        val back = findViewById<ImageButton>(R.id.back)

        loginBtn.setOnClickListener {
            val userEmail = email.text.toString()
            val userPassword = password.text.toString()

            if (userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
                auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, User::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, Signup::class.java))
            finish()
        }

        back.setOnClickListener {
            finish()
        }
    }
}
