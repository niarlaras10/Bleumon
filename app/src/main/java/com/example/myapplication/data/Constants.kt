package com.example.myapplication.data

import java.util.*

object Constants {
    const val SERVICE_STRING = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    val SERVICE_UUID: UUID
        get() = UUID.fromString(SERVICE_STRING)
    const val CHARACTERISTIC_ECHO_STRING = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    val CHARACTERISTIC_ECHO_UUID: UUID
        get() = UUID.fromString(CHARACTERISTIC_ECHO_STRING)
    const val CHARACTERISTIC_TIME_STRING = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

    val CHARACTERISTIC_TIME_UUID: UUID
        get() = UUID.fromString(CHARACTERISTIC_TIME_STRING)
       const val CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID = "202"
//    const val SCAN_PERIOD: Long = 5000
}