package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import android.app.DatePickerDialog
import android.view.View
import java.util.Calendar
import com.example.myapplication.data.User



class SignUpActivity : AppCompatActivity(), View.OnClickListener { // Implement View.OnClickListener

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnSSigned.setOnClickListener {
            signUpUser()
        }
        binding.tvRedirectLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val userId = auth.currentUser?.uid // Retrieve the user ID
            intent.putExtra("userUid", userId) // Pass the user ID as an extra
            startActivity(intent)
            finish()
        }
        // Set click listener for Date of Birth EditText
        binding.etDateOfBirth.setOnClickListener(this) // Set the click listener to this
    }

    // Handle clicks on views
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.etDateOfBirth -> showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date
                val formattedDate =
                    String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etDateOfBirth.setText(formattedDate)
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }

    private fun signUpUser() {
        val email = binding.etSEmailAddress.text.toString()
        val password = binding.etSPassword.text.toString()
        val confirmPassword = binding.etSConfPassword.text.toString()
        val name = binding.etName.text.toString()
        val dateOfBirth = binding.etDateOfBirth.text.toString()

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || name.isBlank() || dateOfBirth.isBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val dobRegex = Regex("""\d{2}-\d{2}-\d{4}""")
        if (!dateOfBirth.matches(dobRegex)) {
            Toast.makeText(this, "Date of birth should be in dd-mm-yyyy format", Toast.LENGTH_SHORT).show()
            return
        }

        // Create user with email and password using Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User registration successful
                    val user = auth.currentUser
                    // Update user profile with the name (optional)
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user?.updateProfile(profileUpdates)

                    // Add user data to Firestore
                    val userData = User(email, name, dateOfBirth, password)
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users")
                        .document(user?.uid ?: "")
                        .set(userData)
                        .addOnSuccessListener { _ ->
                            // Data added to Firestore successfully
                            Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()

                            // Retrieve the user ID
                            val userId = user?.uid

                            // Proceed to the desired activity
                            val intent = Intent(this, HomePage::class.java)
                            intent.putExtra("userUid", userId) // Pass the user ID as an extra
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            // Error occurred while adding data to Firestore
                            Toast.makeText(this, "Failed to register user", Toast.LENGTH_SHORT)
                                .show()
                            Log.e("SignUpActivity", "Error adding user data to Firestore", e)
                        }
                } else {
                    // User registration failed
                    Toast.makeText(this, "Failed ${task.exception}", Toast.LENGTH_SHORT).show()
                    Log.e("SignUpActivity", "Error creating user profile", task.exception)
                }
            }
    }
}
