package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySymptompsPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Date
import java.util.Calendar
import java.io.Serializable
import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.data.Symptom



class SymptompsPage : AppCompatActivity() {

    private lateinit var binding: ActivitySymptompsPageBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var userUid: String
    private lateinit var auth: FirebaseAuth
    private var lastRecordedTimestamp: Date? = null
    private var lastRecordedTimestamp1: Date? = null
    private var lastRecordedTimestamp2: Date? = null
    private var lastRecordedTimestamp3: Date? = null
    private var lastRecordedTimestamp4: Date? = null
    private var lastRecordedTimestamp5: Date? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymptompsPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the user ID from the Intent extras or any other source
        userUid = intent.getStringExtra("userUid") ?: ""
        database = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("SymptomData_$userUid", Context.MODE_PRIVATE)

        val currentDate = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val storedDate = sharedPreferences.getInt("$userUid lastRecordedDate", -1)

        if (currentDate == storedDate) {
            // Retrieve and populate the numberTextViews with the stored symptom data
            val symptom1Count = sharedPreferences.getInt("$userUid symptom1count", 0)
            val symptom2Count = sharedPreferences.getInt("$userUid symptom2count", 0)
            val symptom3Count = sharedPreferences.getInt("$userUid symptom3count", 0)
            val symptom4Count = sharedPreferences.getInt("$userUid symptom4count", 0)
            val symptom5Count = sharedPreferences.getInt("$userUid symptom5count", 0)

            // Use the binding object to set the text for numberTextViews
            binding.numberTextView1.text = symptom1Count.toString()
            binding.numberTextView2.text = symptom2Count.toString()
            binding.numberTextView3.text = symptom3Count.toString()
            binding.numberTextView4.text = symptom4Count.toString()
            binding.numberTextView5.text = symptom5Count.toString()
        }


        // Set click listeners for the buttons to increment symptom counts
        setupButtonListeners()
        // Add click listeners for other symptom buttons

        // Set a click listener for a button to navigate to another activity
        val goToHistoryButton = findViewById<Button>(R.id.symptomHistory) // Adjust with your button ID
        goToHistoryButton.setOnClickListener {
            val intent = Intent(this, SymptomsHistory::class.java)
            intent.putExtra("userUid", userUid) // Pass the userUid as an extra
            startActivity(intent)
        }
    }


    private fun getCurrentDayOfYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun isDateChanged(): Boolean {
        val currentDay = getCurrentDayOfYear()
        val lastRecordedDay = lastRecordedTimestamp?.let { convertToDay(it) }
        return currentDay != lastRecordedDay
    }


    private fun setupButtonListeners() = with(binding) {
        plusButton1.setOnClickListener {
            incrementSymptomCount(numberTextView1, "symptom1count")
            lastRecordedTimestamp1 = Date() // Update the timestamp when the button is clicked
            sendDataToFirebase(createSymptomFromTextViews())
        }
        minusButton1.setOnClickListener {
            decrementSymptomCount(numberTextView1, "symptom1count")
            lastRecordedTimestamp1 = Date() // Update the timestamp when the button is clicked
            sendDataToFirebase(createSymptomFromTextViews())
        }
        plusButton2.setOnClickListener {
            incrementSymptomCount(numberTextView2, "symptom2count")
            lastRecordedTimestamp2 = Date() // Update the timestamp when the button is clicked
            sendDataToFirebase(createSymptomFromTextViews())
        }
        minusButton2.setOnClickListener {
            decrementSymptomCount(numberTextView2, "symptom2count")
            lastRecordedTimestamp2 = Date() // Update the timestamp when the button is clicked
            sendDataToFirebase(createSymptomFromTextViews())
        }
        plusButton3.setOnClickListener {
            incrementSymptomCount(numberTextView3, "symptom3count")
            lastRecordedTimestamp3 = Date() // Update the timestamp when the button is clicked
            sendDataToFirebase(createSymptomFromTextViews())
        }
        minusButton3.setOnClickListener {
            decrementSymptomCount(numberTextView3, "symptom3count")
            lastRecordedTimestamp3 = Date() // Update the timestamp when the button is clicked
            sendDataToFirebase(createSymptomFromTextViews())
        }
        plusButton4.setOnClickListener {
            incrementSymptomCount(numberTextView4, "symptom4count")
            lastRecordedTimestamp4 = Date() // Update the timestamp when the button is clicked
            sendDataToFirebase(createSymptomFromTextViews())
        }
        minusButton4.setOnClickListener {
            decrementSymptomCount(numberTextView4, "symptom4count")
            lastRecordedTimestamp5 = Date() // Update the timestamp when the button is clicked
            sendDataToFirebase(createSymptomFromTextViews())
        }
        plusButton5.setOnClickListener {
            incrementSymptomCount(numberTextView5,"symptom5count")
            lastRecordedTimestamp5 = Date() // Update the timestamp when the button is clicked
            sendDataToFirebase(createSymptomFromTextViews())
        }
        minusButton5.setOnClickListener {
            decrementSymptomCount(numberTextView5, "symptom5count")
            lastRecordedTimestamp5 = Date() // Update the timestamp when the button is clicked
            sendDataToFirebase(createSymptomFromTextViews())
        }
    }


    private fun convertToDay(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun incrementSymptomCount(numberTextView: TextView, key: String) {
        // Increment the symptom count and update the TextView
        val currentCount = numberTextView.text.toString().toInt()
        if (currentCount < 4) { // Limit to a maximum of 4
            val newCount = currentCount + 1
            numberTextView.text = newCount.toString()

            // Prefix the key with userUid to keep data separate for different users
            val userSpecificKey = "$userUid$key"

            // Store the updated symptom count and date in SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putInt(userSpecificKey, newCount)
            editor.putInt("$userUid lastRecordedDate", Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
            editor.apply()
        } else {
            Toast.makeText(this, "Tidak bisa melewati nilai 4", Toast.LENGTH_SHORT).show()
        }
    }


    private fun decrementSymptomCount(numberTextView: TextView, key: String) {
        // Decrement the symptom count and update the TextView
        val currentCount = numberTextView.text.toString().toInt()
        if (currentCount > 0) {
            val newCount = currentCount - 1
            numberTextView.text = newCount.toString()

            // Prefix the key with userUid to keep data separate for different users
            val userSpecificKey = "$userUid$key"

            // Store the updated symptom count
            val editor = sharedPreferences.edit()
            editor.putInt(userSpecificKey, newCount)
            editor.putInt("$userUid lastRecordedDate", Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
            editor.apply()
        } else {
            Toast.makeText(this, "Tidak bisa kurang dari 0", Toast.LENGTH_SHORT).show()
        }
    }


    private fun isDateChanged(currentDate: Int, lastRecordedDate: Date?): Boolean {
        val lastDate = lastRecordedDate?.let { convertToDay(it) }
        return currentDate != lastDate
    }


    private fun createSymptomFromTextViews(): Symptom {
        return Symptom(
            symptom1 = binding.numberTextView1.text.toString().toInt(),
            symptom2 = binding.numberTextView2.text.toString().toInt(),
            symptom3 = binding.numberTextView3.text.toString().toInt(),
            symptom4 = binding.numberTextView4.text.toString().toInt(),
            symptom5 = binding.numberTextView5.text.toString().toInt(),
            timestamp1 = lastRecordedTimestamp1, // Use the updated timestamp
            timestamp2 = lastRecordedTimestamp2,
            timestamp3 = lastRecordedTimestamp3,
            timestamp4 = lastRecordedTimestamp4,
            timestamp5 = lastRecordedTimestamp5
        )
    }


    private fun createSymptomMap(symptom: Symptom): Map<String, Any?> {
        return mapOf(
            "symptom1" to symptom.symptom1,
            "symptom2" to symptom.symptom2,
            "symptom3" to symptom.symptom3,
            "symptom4" to symptom.symptom4,
            "symptom5" to symptom.symptom5,
            "timestamp1" to symptom.timestamp1,
            "timestamp2" to symptom.timestamp2,
            "timestamp3" to symptom.timestamp3,
            "timestamp4" to symptom.timestamp4,
            "timestamp5" to symptom.timestamp5
        )
    }

    private fun convertToDay(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_YEAR)
    }


    private fun sendDataToFirebase(symptom: Symptom) {
        val currentUser = Firebase.auth.currentUser

        if (currentUser == null) {
            // User is not authenticated. Handle this case (e.g., show login screen).
            return
        }

        val userRef = database.collection("users").document(userUid)
        val symptomsCollection = userRef.collection("symptoms")

        val symptomData = createSymptomMap(symptom)

        // Check if the user has permission to write to the symptoms subcollection
        // Here, you validate that the user has the correct userId as per your security rules
        if (currentUser.uid != userUid) {
            Toast.makeText(
                this,
                "You are not authorized to write to this user's symptoms",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // After confirming the user has permission, proceed with updating or adding the symptom data
        symptomsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val latestDocument = querySnapshot.documents[0]
                    latestDocument.reference.set(symptomData)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Data telah ditambahkan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Data gagal ditambahkan: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("SymptomsPage", "Error updating symptom data", exception)
                        }
                } else {
                    // If there are no documents, create a new one
                    symptomsCollection.add(symptomData)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Data telah ditambahkan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Data gagal ditambahkan: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("SymptomsPage", "Data gagal ditambahkana", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to retrieve symptoms: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("SymptomsPage", "Error retrieving symptoms", exception)
            }
    }
}
