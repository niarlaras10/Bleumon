package com.example.myapplication.data

import com.example.myapplication.Measurement

data class User(
    val email: String? = null,
    val name: String? = null,
    val dateOfBirth: String? = null,
    val password: String? = null,
    val measurements: MutableMap<String, Measurement>? = null,
    val symptoms: MutableMap<String, Symptom>? = null
)
{
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}