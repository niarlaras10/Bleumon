package com.example.myapplication


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var tvRedirectSignUp: TextView
    lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    lateinit var btnLogin: Button

    // Creating firebaseAuth object
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val userUid = intent.getStringExtra("userUid")


        // View Binding
        tvRedirectSignUp = findViewById(R.id.tvRedirectSignUp)
        btnLogin = findViewById(R.id.btnLogin)
        etEmail = findViewById(R.id.etSEmailAddress)
        etPass = findViewById(R.id.etSPassword)

        // initialising Firebase auth object
        auth = FirebaseAuth.getInstance()

        btnLogin.setOnClickListener {
            login()
        }

        tvRedirectSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            // using finish() to end the activity
            finish()
        }
    }

    private fun login() {
        val email = etEmail.text.toString()
        val password = etPass.text.toString()

        // Call the Firebase Authentication signInWithEmailAndPassword() method
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // User login successful
                Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_SHORT).show()

                // Retrieve user data from Firestore based on the email
                val db = FirebaseFirestore.getInstance()
                db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val userData = documents.documents[0].data
                            val name = userData?.get("name") as? String
                            val dateOfBirth = userData?.get("dateOfBirth") as? String

                            if (name != null && dateOfBirth != null) {
                                // You can access other user data from the 'userData' HashMap
                                // Use the retrieved data as needed

                                // Retrieve the user ID passed from SignUpActivity
                                val userUid = intent.getStringExtra("userUid")

                                // Proceed to the desired activity
                                val intent = Intent(this, HomePage::class.java)
                                intent.putExtra("userUid", userUid) // Pass the user ID as an extra
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                                Log.e("LoginActivity", "Error retrieving user data")
                            }
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", "Error retrieving user data", e)
                    }
            } else {
                // User login failed
                Toast.makeText(this, "Log In failed", Toast.LENGTH_SHORT).show()
                Log.d("qwer", "login:${task.exception} ")
            }
        }
    }
}

