package com.example.myapplication.data

import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.myapplication.Measurement
import com.google.firebase.firestore.FirebaseFirestore
class MeasurementViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    fun addMeasurementToUser(
        context: Context,
        userUid: String,
        type: String,
        glucoseLevel: Int,
        showCategory: TextView
    ) {
        val userRef = db.collection("users").document(userUid)
        val timestamp = System.currentTimeMillis() // Use current time in milliseconds

        val category = determineCategory(type, glucoseLevel)

        val measurementData = Measurement(
            category = category,
            glucoseLevel = glucoseLevel,
            timestamp = System.currentTimeMillis(),
            type = type
        )

        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                if (user != null) {
                    val measurements = (user.measurements ?: mutableMapOf()).toMutableMap()
                    val measurementId = measurements.size + 1
                    measurements["measurement$measurementId"] = measurementData
                    userRef.update("measurements", measurements)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Data berhasil ditambahkan",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Use the timestamp to format and set the text
//                            val formattedTimestamp = formatTimestamp(timestamp)
//                            val categoryText = "Category: $category\nTimestamp: $formattedTimestamp"
//                            showCategory.text = categoryText
                            val formattedTimestamp = measurementData.formattedTimestamp

                            // Set the text on the provided showCategory TextView
                            val categoryText = "Kategori: $category\nWaktu: $formattedTimestamp"
                            showCategory.text = categoryText
                        }
                        .addOnFailureListener { exception ->
                            // Failure callback, handle it as needed
                            Log.e("FirestoreError", "Failed to add measurement", exception)
                            Toast.makeText(context, "Penambahan data gagal", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Gagal menemukan pengguna", Toast.LENGTH_SHORT).show()
            }
    }

    fun determineCategory(type: String, glucoseLevel: Int): String {
        return when (type) {
            "Puasa" -> {
                when {
                    glucoseLevel > 126 -> "Tinggi"
                    glucoseLevel in 100..125 -> "Medium"
                    glucoseLevel in 71..99 -> "Normal"
                    else -> "Terlalu rendah"
                }
            }
            "Setelah makan" -> {
                when {
                    glucoseLevel > 200 -> "Tinggi"
                    glucoseLevel in 140..199 -> "Medium"
                    glucoseLevel in 79..139 -> "Normal"
                    else -> "Terlalu rendah"
                }
            }
            else -> "Unknown"
        }
    }
}