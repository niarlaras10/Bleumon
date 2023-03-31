package com.example.bleumon

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance().reference

        btnSave.setOnClickListener {
            val glucoseValue = etGlucose.text.toString().toInt()
            val insulinValue = etInsulin.text.toString().toFloat()

            val diabetesEntry = DiabetesEntry(glucoseValue, insulinValue)

            val key = database.child("diabetesEntries").push().key

            if (key != null) {
                database.child("diabetesEntries").child(key).setValue(diabetesEntry)
            }
        }

        btnRetrieve.setOnClickListener {
            val databaseReference = database.child("diabetesEntries")

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val diabetesEntries = ArrayList<DiabetesEntry>()

                    for (entrySnapshot in dataSnapshot.children) {
                        val entry = entrySnapshot.getValue(DiabetesEntry::class.java)
                        diabetesEntries.add(entry!!)
                    }

                    tvResult.text = diabetesEntries.toString()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    tvResult.text = "Error retrieving data from database."
                }
            })
        }
    }

    data class DiabetesEntry(val glucose: Int, val insulin: Float)

}

// In this code, MainActivity is the main activity of the application, and it contains two buttons and two text fields for entering glucose and insulin values. When the user clicks the Save button, the entered values are saved to the Firebase Realtime Database under the "diabetesEntries" node. When the user clicks the Retrieve button, all the saved entries are retrieved from the database and displayed in the Result text view.
//
//The DiabetesEntry data class represents a single entry in the database, and it contains two fields - glucose (an integer) and insulin (a float).
//
//Again, please note that this is just a sample code, and it might need to be modified to suit your specific requirements.
