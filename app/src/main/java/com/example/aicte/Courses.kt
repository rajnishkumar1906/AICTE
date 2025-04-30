package com.example.aicte

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Courses : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var userId: String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courses)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        userId = auth.currentUser?.uid ?: "anonymous"

        // Back button
        findViewById<ImageButton>(R.id.back).setOnClickListener {
            startActivity(Intent(this, User::class.java))
        }

        findViewById<Button>(R.id.course1).setOnClickListener {
            savePurchase("Machine Learning")
        }

        findViewById<Button>(R.id.course2).setOnClickListener {
            savePurchase("Cyber Security")
        }

        findViewById<Button>(R.id.course3).setOnClickListener {
            savePurchase("Mechanical Engineering")
        }

        findViewById<Button>(R.id.course4).setOnClickListener {
            savePurchase("Business Management")
        }

        findViewById<Button>(R.id.course5).setOnClickListener {
            savePurchase("Financial Accounting")
        }

        findViewById<Button>(R.id.course6).setOnClickListener {
            savePurchase("Food Processing and Preservation")
        }

        findViewById<Button>(R.id.purchased).setOnClickListener {
            showPurchasedCoursesPopup()
        }
    }

    private fun savePurchase(courseName: String) {
        val purchaseRef = database.child("users").child(userId).child("purchases")

        purchaseRef.orderByValue().equalTo(courseName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        purchaseRef.push().setValue(courseName)
                        Toast.makeText(this@Courses, "$courseName purchased", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@Courses, "$courseName already purchased", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Courses, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showPurchasedCoursesPopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.purchased_courses, null)
        val listView = dialogView.findViewById<ListView>(R.id.courses)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val purchaseList = mutableListOf<Pair<String, String>>()
        val adapter = CourseAdapter(this, purchaseList) { key ->
            database.child("users").child(userId).child("purchases").child(key).removeValue()
        }

        listView.adapter = adapter

        val purchaseRef = database.child("users").child(userId).child("purchases")
        purchaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                purchaseList.clear()
                for (item in snapshot.children) {
                    val course = item.getValue(String::class.java)
                    if (course != null) {
                        purchaseList.add(Pair(item.key ?: "", course))
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Courses, "Failed to load purchases", Toast.LENGTH_SHORT).show()
            }
        })

        dialog.show()
    }
}
