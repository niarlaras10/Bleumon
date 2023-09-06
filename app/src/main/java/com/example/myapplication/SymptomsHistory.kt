package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*
import java.text.SimpleDateFormat
import java.util.Date
import com.example.myapplication.data.Symptom

class SymptomsHistory : AppCompatActivity() {

    private lateinit var toggleButton: ToggleButton
    private lateinit var datePicker: DatePicker
    private lateinit var symptomDataTextView1: TextView
    private lateinit var symptomDataTextView2: TextView
    private lateinit var symptomDataTextView3: TextView
    private lateinit var symptomDataTextView4: TextView
    private lateinit var symptomDataTextView5: TextView
    private lateinit var datePickerButton: Button
    private lateinit var dateTextView: TextView // Add this variable




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms_history)

//        toggleButton = findViewById(R.id.toggleButton)
//        datePicker = findViewById(R.id.datePicker)
        symptomDataTextView1 = findViewById(R.id.numberHistoryView1)
        symptomDataTextView2 = findViewById(R.id.numberHistoryView2)
        symptomDataTextView3 = findViewById(R.id.numberHistoryView3)
        symptomDataTextView4 = findViewById(R.id.numberHistoryView4)
        symptomDataTextView5 = findViewById(R.id.numberHistoryView5)
        datePickerButton = findViewById(R.id.datePickerButton)
        dateTextView = findViewById(R.id.dateChosen)



        val symptom = intent.getSerializableExtra("symptomData") as? Symptom

        // Update the UI with symptom data
        updateUIWithSymptomData(symptom)

        datePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            // Create a DatePickerDialog and set its date and click listener
            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    // Handle the selected date
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    val selectedDateInMillis = selectedCalendar.timeInMillis

                    // Call your function to retrieve data using the selected date
                    retrieveSymptomData(selectedDateInMillis)
                },
                currentYear,
                currentMonth,
                currentDay
            )

            // Show the DatePickerDialog
            showDatePickerDialog()
        }

    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Handle the selected date
                val selectedDateMillis = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.timeInMillis

                // Call the function to retrieve symptom data based on the selected date
                retrieveSymptomData(selectedDateMillis)
            },
            currentYear,
            currentMonth,
            currentDay
        )

        datePickerDialog.show()
    }


    private fun retrieveSymptomData(selectedDateMillis: Long) {
        // Convert selectedDateMillis to the format you need for Firestore queries
        val selectedDate = Date(selectedDateMillis)
        val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate)

        dateTextView.text = formattedDate

        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            // User is not authenticated. Handle this case (e.g., show login screen).
            return
        }

        val userUid = currentUser.uid
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userUid)
        val symptomsCollection = userRef.collection("symptoms")

        // Calculate start and end timestamps for the selected day
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate.time
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.time

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val startOfNextDay = calendar.time

        // Query Firestore for symptom data based on the selected date
        // Query Firestore for symptom data based on the selected date
        symptomsCollection
            .whereGreaterThanOrEqualTo("timestamp1", startOfDay)
            .whereLessThan("timestamp1", startOfNextDay)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val symptomDocument = querySnapshot.documents[0]
                    val symptom = symptomDocument.toObject(Symptom::class.java)
                    updateUIWithSymptomData(symptom)
                } else {
                    symptomDataTextView1.text = "No symptom data for $formattedDate"
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to retrieve symptom data", Toast.LENGTH_SHORT)
                    .show()
                Log.e("Symptomps_History", "Error retrieving symptom data", exception)
            }
    }


    fun updateUIWithSymptomData(symptom: Symptom?) {
        if (symptom != null) {
            // Update the TextViews with symptom numbers
            symptomDataTextView1.text = symptom.symptom1.toString()
            symptomDataTextView2.text = symptom.symptom2.toString()
            symptomDataTextView3.text = symptom.symptom3.toString()
            symptomDataTextView4.text = symptom.symptom4.toString()
            symptomDataTextView5.text = symptom.symptom5.toString()
        }
    }
}

