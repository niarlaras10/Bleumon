package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.example.myapplication.data.MeasurementViewModel
import com.example.myapplication.databinding.ActivityManualMeasureBinding

class ManualMeasure : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userUid: String
//    private lateinit var showCategory: TextView
    private lateinit var viewModel: MeasurementViewModel
    private lateinit var binding:ActivityManualMeasureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualMeasureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the viewModel
        viewModel = ViewModelProvider(this).get(MeasurementViewModel::class.java)


        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
//        showCategory = findViewById(R.id.showCategory)

        // Get the userUid from the intent
        userUid = intent.getStringExtra("userUid") ?: ""

        if (userUid.isBlank()) {
            // If the userUid is null or empty, handle the case accordingly
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
            // For example, you might want to finish the activity or redirect the user to the previous activity
            finish()
            return
        }
        // Handle fasting button click
        binding.fastingButton.setOnClickListener {
            val glucoseLevel = binding.glucoseLevel.text.toString().toInt()
            if (glucoseLevel != null) {
                viewModel.addMeasurementToUser(this, userUid,"Puasa",glucoseLevel, binding.showCategory)
            } else {
                Toast.makeText(this, "Masukkan angka yang valid", Toast.LENGTH_SHORT).show()
            }
        }
        // Handle non-fasting button click
        binding.nonFastingButton.setOnClickListener {
            val glucoseLevel = binding.glucoseLevel.text.toString().toInt()
            if (glucoseLevel != null) {
                viewModel.addMeasurementToUser(this, userUid,"Setelah makan",glucoseLevel, binding.showCategory)
            } else {
                Toast.makeText(this, "Masukkan angka yang valid", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
