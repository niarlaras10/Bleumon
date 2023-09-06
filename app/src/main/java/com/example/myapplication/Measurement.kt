package com.example.myapplication
// Measurement.kt
import java.text.SimpleDateFormat
import java.util.*

data class Measurement(
    val category: String? = null,
    val glucoseLevel: Int? = null,
    val timestamp: Long? = null,
    val type: String? = null,
) {
    // Add a property to store the formatted timestamp
    val formattedTimestamp: String?
        get() {
            timestamp?.let {
                val date = Date(it)
                val format = SimpleDateFormat("dd-MM-yyyy 'pada' HH:mm:ss", Locale.getDefault())
                return format.format(date)
            }
            return ""
        }
}
