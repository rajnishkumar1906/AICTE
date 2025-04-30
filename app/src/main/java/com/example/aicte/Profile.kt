package com.example.aicte

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class Profile : AppCompatActivity() {

    private lateinit var username: TextView
    private lateinit var userEmail: TextView
    private lateinit var mobileNo: TextView
    private lateinit var password: TextView
    private lateinit var age: TextView
    private lateinit var gender: TextView
    private lateinit var state: TextView
    private lateinit var postalCode: TextView
    private lateinit var profilePicture: ImageView
    private lateinit var editProfileButton: Button

    private lateinit var database: DatabaseReference
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var profileImageUrl: String = ""

    private val genderOptions = listOf("Male", "Female", "Other")
    private val stateOptions = listOf("Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
        "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh",
        "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim",
        "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val back = findViewById<ImageButton>(R.id.back_button)
        back.setOnClickListener {
            startActivity(Intent(this, User::class.java))
        }

        database = FirebaseDatabase.getInstance().getReference("users")

        username = findViewById(R.id.username)
        userEmail = findViewById(R.id.userEmail)
        mobileNo = findViewById(R.id.mobile)
        password = findViewById(R.id.password)
        age = findViewById(R.id.age)
        gender = findViewById(R.id.gender)
        state = findViewById(R.id.state)
        postalCode = findViewById(R.id.postalCode)
        profilePicture = findViewById(R.id.profile_picture)
        editProfileButton = findViewById(R.id.editProfile)

        loadUserProfile()

        editProfileButton.setOnClickListener {
            openEditDialog()
        }

        profilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }
    }

    private fun loadUserProfile() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .child("info")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val password = snapshot.child("password").getValue(String::class.java)
                    val mobileNo = snapshot.child("mobileNo").getValue(String::class.java)
                    val age = snapshot.child("age").getValue(String::class.java)
                    val gender = snapshot.child("gender").getValue(String::class.java)
                    val state = snapshot.child("state").getValue(String::class.java)
                    val postalCode = snapshot.child("postalCode").getValue(String::class.java)
                    val profileImage = snapshot.child("profileImage").getValue(String::class.java)

                    this@Profile.username.text = username ?: "Username not available"
                    this@Profile.userEmail.text = email ?: "Email not available"
                    this@Profile.password.text = password ?: ""
                    this@Profile.mobileNo.text = mobileNo ?: "Mobile No. not available"
                    this@Profile.age.text = age ?: "Age not available"
                    this@Profile.gender.text = gender ?: "Gender not available"
                    this@Profile.state.text = state ?: "State not available"
                    this@Profile.postalCode.text = postalCode ?: "Postal Code not available"

                    if (!profileImage.isNullOrEmpty()) {
                        profileImageUrl = profileImage
                        Glide.with(this@Profile).load(profileImage).into(profilePicture)
                    }
                } else {
                    Toast.makeText(this@Profile, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Profile, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openEditDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.edit_profile_popup)

        val etMobile = dialog.findViewById<EditText>(R.id.etmobileNumber)
        val etPassword = dialog.findViewById<EditText>(R.id.etPassword)
        val etAge = dialog.findViewById<EditText>(R.id.etAge)
        val etPostal = dialog.findViewById<EditText>(R.id.etPostalCode)
        val spGender = dialog.findViewById<Spinner>(R.id.spGender)
        val spState = dialog.findViewById<Spinner>(R.id.spState)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)

        spGender.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        spState.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, stateOptions)

        etMobile.setText(mobileNo.text)
        etPassword.setText(password.text)
        etAge.setText(age.text)
        etPostal.setText(postalCode.text)
        spGender.setSelection(genderOptions.indexOf(gender.text.toString()))
        spState.setSelection(stateOptions.indexOf(state.text.toString()))

        btnSave.setOnClickListener {
            val updatedUser = UserProfile(
                username = username.text.toString(),
                email = userEmail.text.toString(),
                mobileNo = etMobile.text.toString(),
                password = etPassword.text.toString(),
                age = etAge.text.toString(),
                gender = spGender.selectedItem.toString(),
                state = spState.selectedItem.toString(),
                postalCode = etPostal.text.toString(),
                profileImage = profileImageUrl
            )

            database.child(uid).child("info")
                .setValue(updatedUser)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    loadUserProfile()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            imageUri?.let {
                profilePicture.setImageURI(it)
                uploadProfileImage(it)
            }
        }
    }

    private fun uploadProfileImage(uri: Uri) {
        val ref = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                profileImageUrl = downloadUri.toString()

                database.child(uid).child("info").child("profileImage").setValue(profileImageUrl)

                Glide.with(this).load(downloadUri).into(profilePicture)
            }
        }
    }
}
