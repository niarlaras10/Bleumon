package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class HomePage : AppCompatActivity() {
    private lateinit var userUid: String
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Get the user ID from the intent extras or any other source
        userUid = intent.getStringExtra("userUid") ?: ""
        db = FirebaseFirestore.getInstance()

        val button1 = findViewById<Button>(R.id.measure)
        val button2 = findViewById<Button>(R.id.History)
        val button3 = findViewById<Button>(R.id.feature)
        val nameTextView = findViewById<TextView>(R.id.showName)

        val userName = db.collection("users").document(userUid)

        userName.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userName = documentSnapshot.getString("name")
                    if (userName != null) {
                        // Set the retrieved name to the TextView
                        nameTextView.text = "Halo, $userName"
                    } else {
                        // Handle the case where the name field is null
                        nameTextView.text = "Name not available"
                    }
                } else {
                    // Handle the case where the document doesn't exist
                    nameTextView.text = "User not found"
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors that occurred during the retrieval
                nameTextView.text = "Error: ${e.message}"
            }

        // Set click listeners for the buttons
        button1.setOnClickListener {
            // Start MeasurePage activity
            val intent = Intent(this, MeasurePage::class.java)
            intent.putExtra("userUid", userUid) // Pass the userUid as an extra
            startActivity(intent)
        }

        button2.setOnClickListener {
            // Start History activity
            val intent = Intent(this, History::class.java)
            intent.putExtra("userUid", userUid) // Pass the userUid as an extra
            startActivity(intent)
        }

        button3.setOnClickListener {
            // Start SymptompsPage activity
            val intent = Intent(this, SymptompsPage::class.java)
            intent.putExtra("userUid", userUid) // Pass the userUid as an extra

            startActivity(intent)
        }
    }
}
