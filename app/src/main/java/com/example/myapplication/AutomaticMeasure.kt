package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapplication.data.MeasurementViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.myapplication.databinding.ActivityAutomaticMeasureBinding
import com.example.myapplication.databinding.ActivitySignUpBinding


class AutomaticMeasure : AppCompatActivity() {
    private val REQUEST_LOCATION_PERMISSION = 2
    private val PERMISSION_REQUEST_CODE = 3
    private var bluetoothGatt: BluetoothGatt? = null
    private lateinit var measurementTextView: TextView
    private var CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    private var SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    private lateinit var db: FirebaseFirestore
    private lateinit var showCategory: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: MeasurementViewModel
    private var isCharacteristicRead = false
    private var isDeviceConnected = false // Add a flag to track device connection
    private var startMeasureButton: Button? = null
    private lateinit var binding: ActivityAutomaticMeasureBinding


    companion object {
        private const val REQUEST_ENABLE_BT = 1

        // Function to get the BluetoothAdapter instance
        private fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            return bluetoothManager.adapter
        }
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    //    @SuppressLint("MissingPermission")
    private fun promptEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    // Check if the required permissions are granted before performing Bluetooth operations
    private fun checkBluetoothPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            // Request the missing permissions from the user
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Perform Bluetooth operations since all required permissions are granted
            // Call your Bluetooth-related code here
            promptEnableBluetooth()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        measurementTextView = findViewById(R.id.yourGlucoseLevel)
        db = FirebaseFirestore.getInstance()
        showCategory = findViewById(R.id.yourResultTextView)
        // Initialize the viewModel
        viewModel = ViewModelProvider(this).get(MeasurementViewModel::class.java)

        // Get the userUid from the intent
        val userUid = intent.getStringExtra("userUid")

        // Check if userUid is not blank
        if (userUid.isNullOrEmpty()) {
            Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            // Handle the case when userUid is null or empty (e.g., finish the activity)
            finish()
            return
        }

        initializeBluetooth()

        // Set click listener for the Connect button
        binding.connectButton.setOnClickListener {
            // Call the method to start scanning for BLE devices and connect to the selected device
            startBLEDeviceConnection()
        }

        startMeasureButton = findViewById(R.id.start_measure)
        startMeasureButton?.isEnabled = false

        startMeasureButton?.setOnClickListener {
            if (isDeviceConnected) {
                if (isServiceDiscoveryComplete) {
                    sendStartMeasureCommand()
                } else {
                    Toast.makeText(this, "Penemuan servis belum lengkap", Toast.LENGTH_SHORT).show()
                    Log.d("AutomaticMeasure", "Service discovery is not complete")
                }
            } else {
                Toast.makeText(this, "Perangkat tidak terhubung", Toast.LENGTH_SHORT).show()
                Log.d("AutomaticMeasure", "Device not connected")
            }
        }


        // Handle fasting button click
        binding.fastingButton.setOnClickListener {
            val glucoseLevel = binding.yourGlucoseLevel.text.toString().toInt()
            if (glucoseLevel != null) {
                if (userUid != null) {
                    viewModel.addMeasurementToUser(
                        this,
                        userUid,
                        "Puasa",
                        glucoseLevel,
                        showCategory
                    )
                }
            } else {
                Toast.makeText(this, "Lakukan pengukuran lagi", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Handle non-fasting button click
        binding.nonFastingButton.setOnClickListener {
            val glucoseLevel = binding.yourGlucoseLevel.text.toString().toInt()
            if (glucoseLevel != null) {
                if (userUid != null) {
                    viewModel.addMeasurementToUser(
                        this,
                        userUid,
                        "Setelah makan",
                        glucoseLevel,
                        showCategory
                    )
                }
            } else {
                Toast.makeText(this, "Lakukan pengukuran lagi", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun initializeBluetooth() {
        checkBluetoothPermissions()

        // Get the BluetoothAdapter instance from the Intent
        val bluetoothAdapter = getBluetoothAdapter(this)
        if (bluetoothAdapter == null) {
            // Handle the case when Bluetooth is not supported
            return
        }
        @SuppressLint("MissingPermission")
        // Request to enable Bluetooth if it's not enabled
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            // Bluetooth sudah diaktifkan, lanjutkan dengan langkah berikutnya
            requestLocationPermission()
        }
    }

    // Handle the permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with GATT operations
                } else {
                    // Permission denied, handle this case by showing a prompt or disabling functionality
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    //    @SuppressLint("MissingPermission")
    // Request location permission at runtime
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Location permission already granted, proceed with BLE device connection
            startBLEDeviceConnection()
        }
    }

    //  keep displaying the same alert over and over again until the user accepts our recommendation to turn on Bluetooth
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // Handle the case when the user refused to enable Bluetooth
                // You may prompt the user again or show a message to enable Bluetooth
                Toast.makeText(this, "Bluetooth dibutuhkan untuk fitur ini.", Toast.LENGTH_SHORT)
                    .show()
                finish() // Terminate the activity if Bluetooth is not enabled
            } else {
                // Bluetooth is enabled, proceed with the next step
                requestLocationPermission()
            }
        }
    }

    // Start BLE device connection
    private var isServiceDiscoveryComplete = false

    private fun startBLEDeviceConnection() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Get the BluetoothAdapter instance
            val bluetoothAdapter = getBluetoothAdapter(this)
            if (bluetoothAdapter != null) {
                // Get the BLE device by its address (replace 'bleDeviceAddress' with the actual address)
                val bleDeviceAddress =
                    "40:91:51:FB:B7:D2" // Replace this with your actual BLE device address
                val bleDevice = bluetoothAdapter.getRemoteDevice(bleDeviceAddress)
                // Connect to the BLE device
                bluetoothGatt = bleDevice.connectGatt(this, true, gattCallback)
                Log.d("AutomaticMeasure", "connected")
            }
        } else {
            // Request the location permission if it's not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            Log.d("AutomaticMeasure", "failed to connect")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val deviceAddress = gatt.device.address
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    isDeviceConnected = true
                    if (ContextCompat.checkSelfPermission(this@AutomaticMeasure, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Permission already granted, proceed with GATT operations
                    } else {
                        // Request permission from the user
                        ActivityCompat.requestPermissions(this@AutomaticMeasure, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
                    }
                    gatt.discoverServices() // Trigger service discovery
                    Log.d("BluetoothGattCallback", "service discovery triggered")
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    isDeviceConnected = false
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                isDeviceConnected = false
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.d("AutomaticMeasure", "onServicesDiscovered callback invoked with status: $status")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val services: List<BluetoothGattService> = gatt.services
                val desiredService = services.find { it.uuid.toString() == serviceUUID }

                Log.d("AutomaticMeasure", "service discovered")

                if (desiredService != null) {
                    // Found the desired service, now iterate through its characteristics to find the desired one
                    val characteristics: List<BluetoothGattCharacteristic> = desiredService.characteristics
                    Log.d("AutomaticMeasure", "Desired service found")

                    // Iterate through the list of characteristics and compare UUIDs
                    val desiredCharacteristic = characteristics.find { it.uuid.toString() == CHARACTERISTIC_UUID }

                    if (desiredCharacteristic != null) {
                        // The desired characteristic was found
                        // You can perform your actions here
                        isServiceDiscoveryComplete = true
                        isDeviceConnected = true
                        Log.d("AutomaticMeasure", "CHARACTERISTIC_UUID match device documentation")
                        // Now you can use the 'desiredCharacteristic' for further operations
                        runOnUiThread {
                            // Enable the "Start Measure" button
                            startMeasureButton?.isEnabled = true
                            // Enable notifications for the desired characteristic
                            if (ContextCompat.checkSelfPermission(
                                    this@AutomaticMeasure,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                                != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    this@AutomaticMeasure,
                                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                    REQUEST_LOCATION_PERMISSION
                                )
                            }
                            gatt.setCharacteristicNotification(desiredCharacteristic, true)
                            val CLIENT_CHARACTERISTIC_CONFIG_UUID =
                                UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
                            val descriptor =
                                desiredCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                            if (descriptor != null) {
                                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(descriptor)
                                Log.d("AutomaticMeasure", "write descriptor success")
                            } else {
                                Log.d("AutomaticMeasure", "Descriptor not found for characteristic")
                            }
                        }
                    }else {
                        isServiceDiscoveryComplete = false
                        isDeviceConnected = false
                        Log.d("AutomaticMeasure", "CHARACTERISTIC_UUID does not match device documentation")
                    }
                }
            } else {
                isServiceDiscoveryComplete = false
                isDeviceConnected = false
                Log.e("AutomaticMeasure", "Service discovery failed with status: $status")
            }
        }


        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.d(
                            "BluetoothGattCallback",
                            "Read characteristic $uuid:\n${value.toHexString()}"
                        )
                        val value = characteristic.value
                        isCharacteristicRead = true

                        // After successfully reading the characteristic, send the start measure command
                        sendStartMeasureCommand()
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.d("BluetoothGattCallback", "Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.d(
                            "BluetoothGattCallback",
                            "Characteristic read failed for $uuid, error: $status"
                        )
                        isCharacteristicRead = false
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.d(
                            "BluetoothGattCallback",
                            "Wrote to characteristic $uuid | value: ${value.toHexString()}"
                        )
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.d("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.d("BluetoothGattCallback", "Write not permitted for $uuid!")
                    }
                    else -> {
                        Log.d(
                            "BluetoothGattCallback",
                            "Characteristic write failed for $uuid, error: $status"
                        )
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val serviceUUID = characteristic.service.uuid
            val characteristicUUID = characteristic.uuid

            Log.d("AutomaticMeasure", "Service UUID: $serviceUUID")
            Log.d("AutomaticMeasure", "Characteristic UUID: $characteristicUUID")

            // Compare the characteristic UUID with the desired characteristic UUID
            if (CHARACTERISTIC_UUID == characteristicUUID.toString()) {
                val measurementData = characteristic.value
                runOnUiThread {
                    // Convert the received byte array to an integer
                    val glucoseLevel = byteArrayToInt(measurementData)

                    // Display the glucose level in the text view
                    measurementTextView.text = glucoseLevel.toString()
                    Log.d(
                        "BluetoothGattCallback",
                        "measurement retrieve"
                    )

                    // You can save the glucoseLevel value to Firebase or perform other actions here
                }
            }
        }
    }

    val characteristic = getCharacteristicByUUID(SERVICE_UUID, CHARACTERISTIC_UUID)

    // Send a write request to the characteristic to trigger data transmission from the Arduino
    @SuppressLint("MissingPermission")
    private fun sendStartMeasureCommand() {
        val characteristic = getCharacteristicByUUID(SERVICE_UUID,CHARACTERISTIC_UUID)
        Log.e("AutomaticMeasure", "Characteristic: $characteristic")
        if (characteristic != null) {
            characteristic.setValue(byteArrayOf(0x01))
            bluetoothGatt?.writeCharacteristic(characteristic)
            Log.d("AutomaticMeasure", "send command succesful")
            Toast.makeText(this, "Pengukuran dimulai, harap tunggu", Toast.LENGTH_SHORT).show()
        } else {
            // The characteristic with the specified UUID was not found
            Log.d("AutomaticMeasure", "Characteristic not found")
            Toast.makeText(this, "Tekan tombol mulai ukur lagi", Toast.LENGTH_SHORT).show()
        }
    }


    // Get a GATT service by UUID
    private fun getServiceByUUID(uuid: String): BluetoothGattService? {
        return bluetoothGatt?.getService(UUID.fromString(uuid))
    }

    // Get a GATT characteristics by UUID
    private fun getCharacteristicByUUID(serviceUUID: String, characteristicUUID: String): BluetoothGattCharacteristic? {
        val service = getServiceByUUID(serviceUUID) ?: return null
        return service.getCharacteristic(UUID.fromString(characteristicUUID))
    }


    // Check that UUIDs match the device documentation
    val serviceUUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    val expectedCharacteristicUUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"


    private fun byteArrayToInt(byteArray: ByteArray): Int {
        return byteArray.foldIndexed(0) { index, acc, byte ->
            acc or ((byte.toInt() and 0xFF) shl (index * 8))
        }
    }

    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }


    //     Define the BroadcastReceiver to receive the discovered Bluetooth devices
    @SuppressLint("MissingPermission")
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // Check if the discovered device has the desired service UUID
                if (device != null && hasRequiredServiceUUID(device)) {
                    bluetoothAdapter?.cancelDiscovery()
                    startBLEDeviceConnection()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun hasRequiredServiceUUID(device: BluetoothDevice): Boolean {
        val desiredServiceUUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        val deviceUUIDs = device.uuids ?: return false // Return false if device UUIDs are not available

        return deviceUUIDs.any { it.uuid == desiredServiceUUID }
    }

    @SuppressLint("MissingPermission")
    // Remember to unregister the BroadcastReceiver and close the BluetoothGatt on activity onDestroy
    override fun onDestroy() {
        super.onDestroy()

        // Close the BluetoothGatt object to release resources
        bluetoothGatt?.apply {
            close()
            bluetoothGatt = null
        }
    }
}