package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapplication.databinding.ActivityHomePageBinding

class HomePage : AppCompatActivity() {
    private lateinit var userUid: String
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the user ID from the intent extras or any other source
        userUid = intent.getStringExtra("userUid") ?: ""
        db = FirebaseFirestore.getInstance()

        val userName = db.collection("users").document(userUid)

        userName.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userName = documentSnapshot.getString("name")
                    if (userName != null) {
                        // Set the retrieved name to the TextView
                        binding.showName.text = "Halo, $userName"
                    } else {
                        // Handle the case where the name field is null
                        binding.showName.text = "Name not available"
                    }
                } else {
                    // Handle the case where the document doesn't exist
                    binding.showName.text = "User not found"
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors that occurred during the retrieval
                binding.showName.text = "Error: ${e.message}"
            }

        // Set click listeners for the buttons
        binding.measure.setOnClickListener {
            // Start MeasurePage activity
            val intent = Intent(this, MeasurePage::class.java)
            intent.putExtra("userUid", userUid) // Pass the userUid as an extra
            startActivity(intent)
        }

        binding.History.setOnClickListener {
            // Start History activity
            val intent = Intent(this, History::class.java)
            intent.putExtra("userUid", userUid) // Pass the userUid as an extra
            startActivity(intent)
        }

        binding.feature.setOnClickListener {
            // Start SymptompsPage activity
            val intent = Intent(this, SymptompsPage::class.java)
            intent.putExtra("userUid", userUid) // Pass the userUid as an extra

            startActivity(intent)
        }
    }
}
