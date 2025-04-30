package com.example.aicte

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class User : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        auth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.main_drawer)
        navigationView = findViewById(R.id.nav_view)

        var vp1 = findViewById<ImageView>(R.id.vp1)

        var vp2 = findViewById<ImageView>(R.id.vp2)
        vp2.setOnClickListener {
            startActivity(Intent(this, Courses::class.java))
        }

        val learnmore = findViewById<TextView>(R.id.learn_more)
        learnmore.setOnClickListener {
            startActivity(Intent(this, Bureau::class.java))
        }

        val about = findViewById<Button>(R.id.about)
        about.setOnClickListener {
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show()
        }

        val toggler = findViewById<ImageButton>(R.id.drawertoggler)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggler.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (menuItem.itemId) {
                R.id.profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    true
                }
                R.id.bureau -> {
                    startActivity(Intent(this, Bureau::class.java))
                    true
                }
                R.id.courses -> {
                    startActivity(Intent(this, Courses::class.java))
                    true
                }
                R.id.logout -> {
                    AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes") { _, _ ->
                            auth.signOut()
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        }
                        .setNegativeButton("No", null)
                        .show()
                    true
                }
                else -> false
            }
        }


        loadNavHeaderUserInfo()

        // Show welcome popup if coming from signup
        if (intent.getBooleanExtra("fromSignup", false)) {
            showWelcomePopup("🎉 Congratulations!", "You’ve signed up successfully. Welcome to AICTE!")
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNavHeaderUserInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.notification -> {
                Toast.makeText(this, "Notification popup clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showWelcomePopup(title: String, message: String) {
        val dialogView = layoutInflater.inflate(R.layout.popup_welcome, null)
        val welcomeTextView = dialogView.findViewById<TextView>(R.id.welcome)
        welcomeTextView.text = "$title\n\n$message"

        val mediaPlayer = MediaPlayer.create(this, R.raw.aicte)
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            it.release()
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
            .show()
    }

    private fun loadNavHeaderUserInfo() {
        val headerView = navigationView.getHeaderView(0)
        val headerImage = headerView.findViewById<ImageView>(R.id.userprofile)
        val headerName = headerView.findViewById<TextView>(R.id.nav_header_name)
        val headerEmail = headerView.findViewById<TextView>(R.id.nav_header_email)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("info")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("username").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val profileImage = snapshot.child("profileImage").getValue(String::class.java)

                headerName.text = name ?: "User name"
                headerEmail.text = email ?: "user@gmail.com"

                if (!profileImage.isNullOrEmpty()) {
                    Glide.with(this@User).load(profileImage).into(headerImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@User, "Failed to load header info", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
