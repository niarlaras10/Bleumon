package com.example.myapplication.data

import java.io.Serializable
import java.util.*

data class Symptom(
    val symptom1: Int = 0,
    val symptom2: Int = 0,
    val symptom3: Int = 0,
    val symptom4: Int = 0,
    val symptom5: Int = 0,
    val timestamp1: Date? = null, //save the timestamp for every symptom so it acts independently
    val timestamp2: Date? = null,
    val timestamp3: Date? = null,
    val timestamp4: Date? = null,
    val timestamp5: Date? = null
): Serializable

