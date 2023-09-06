package com.example.myapplication.data

import android.graphics.Color
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.myapplication.Measurement
import com.example.myapplication.R
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class MeasurementGroup(
    val measurements: List<Measurement>,
    val db: FirebaseFirestore,
    val userUid: String
) {
    suspend fun retrieveMeasurements(startTime: Long, endTime: Long): List<Measurement> {
        val retrievedMeasurements = mutableListOf<Measurement>()

        try {
            val userRef = db.collection("users").document(userUid)

            // Retrieve the user document and listen for changes
            val userDocument = userRef.get().await()

            if (userDocument.exists()) {
                // Access the measurements map from the user document
                val measurements = userDocument.get("measurements") as? Map<String, Any>

                if (measurements != null) {
                    // Filter measurements based on the timestamp within the time period
                    val measurementsWithinTimePeriod = measurements.filterValues { measurementData ->
                        val timestamp = (measurementData as Map<String, Any>)["timestamp"] as? Long
                        timestamp in startTime..endTime
                    }

                    // Iterate through the measurementsWithinTimePeriod map
                    for ((measurementId, measurementData) in measurementsWithinTimePeriod) {
                        val category = (measurementData as Map<String, Any>)["category"] as? String
                        val glucoseLevel = (measurementData as Map<String, Any>)["glucoseLevel"] as? Long
                        val timestamp = (measurementData as Map<String, Any>)["timestamp"] as? Long
                        val type =
                            (measurementData as Map<String, Any>)["type"] as? String

                        if (category != null && glucoseLevel != null && timestamp != null) {
                            val measurement = Measurement(category, glucoseLevel.toInt(), timestamp, type)
                            retrievedMeasurements.add(measurement)
                        }
                    }

                    // Sort the measurements by timestamp in descending order (newest to oldest)
                    val sortedMeasurements = retrievedMeasurements.sortedByDescending { it.timestamp }
                    Log.d("History", "query snapshot done")
                    return sortedMeasurements
                } else {
                    // Handle the case where the measurements map is null
                    Log.d("History", "No measurements found")
                }
            } else {
                // Handle the case where the user document doesn't exist
                Log.d("History", "User document does not exist")
            }
        } catch (e: Exception) {
            // Handle any exceptions here
            Log.d("History", "Error fetching measurements: ${e.message}", e)
        }

        // Return an empty list if there was an error or no data was found
        return emptyList()
    }


    fun formatTimestamp(timestamp: Long?): String {
        if (timestamp != null) {
            val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            return dateFormat.format(Date(timestamp))
        }
        return "N/A" // You can replace "N/A" with any default value you prefer
    }
}