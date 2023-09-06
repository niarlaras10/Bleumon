package com.example.myapplication


import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MeasurePage : AppCompatActivity() {
    private lateinit var userUid: String
    private val REQUEST_ENABLE_BT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measure_page)

        userUid = intent.getStringExtra("userUid") ?: ""

        val button4 = findViewById<Button>(R.id.automatic_page)
        val button5 = findViewById<Button>(R.id.manual_page)

        // Set click listener for the "Automatic Measure" button
        button4.setOnClickListener {
            // Request Bluetooth permissions when the "Automatic Measure" button is clicked
            checkBluetoothPermissions()
            startAutomaticMeasure()
        }

        // Set click listener for the "Manual Measure" button
        button5.setOnClickListener {
            val intent = Intent(this, ManualMeasure::class.java)
            intent.putExtra("userUid", userUid) // Pass the userUid as an extra
            startActivity(intent)
        }
    }

    private fun checkBluetoothPermissions() {
        val bluetoothPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
        val bluetoothAdminPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)

        if (bluetoothPermission == PackageManager.PERMISSION_GRANTED &&
            bluetoothAdminPermission == PackageManager.PERMISSION_GRANTED
        ) {
            // Bluetooth permissions are granted, proceed with the AutomaticMeasure activity
            startAutomaticMeasure()
        } else {
            // Request Bluetooth permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN),
                REQUEST_ENABLE_BT
            )
        }
    }

    private fun startAutomaticMeasure() {
        val intent = Intent(this, AutomaticMeasure::class.java)
        intent.putExtra("userUid", userUid) // Pass the userUid as an extra
        startActivity(intent)
    }

    // Handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions granted, proceed with the AutomaticMeasure activity
                startAutomaticMeasure()
            } else {
                // Permissions denied, show a message or take appropriate action
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}