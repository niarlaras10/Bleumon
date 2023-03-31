package com.example.bleumon
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var historyRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize the Firebase Database instance
        database = FirebaseDatabase.getInstance()

        // Get a reference to the "history" node in the database
        historyRef = database.reference.child("history")

        // Attach a listener to the historyRef to get updates
        historyRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Get the date and time from the snapshot
                val date = snapshot.child("date").value.toString()
                val time = snapshot.child("time").value.toString()

                // TODO: Display the date and time in the UI or save them to a list
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle changes to the data if needed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle removal of data if needed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle moving of data if needed
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors if needed
            }
        })
    }
}
//n this example, the HistoryActivity retrieves the history
// node from the Firebase Realtime Database and attaches a ChildEventListener to it. Whenever a child is added to the history node, the onChildAdded() method is called, and you can retrieve the date and time values from the DataSnapshot object. You can then display the date and time in the UI or save them to a list for later use.
//
//Note that you will need to have the appropriate Firebase dependencies in your project and configure Firebase in your app to be able to access the Realtime Database.
//buat sendiri
//class History {
//}