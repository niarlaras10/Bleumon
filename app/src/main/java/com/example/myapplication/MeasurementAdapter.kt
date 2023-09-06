package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Measurement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeasurementAdapter(private val measurements: MutableList<Measurement>) : RecyclerView.Adapter<MeasurementAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        val glucoseTextView: TextView = itemView.findViewById(R.id.glucoseTextView)
    }

    fun updateData(newData: List<Measurement>) {
        measurements.clear()
        measurements.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_measurement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val measurement = measurements[position]
        holder.glucoseTextView.text ="Kadar glukosa: ${measurement.glucoseLevel}"
        holder.categoryTextView.text = "Kategori: ${measurement.category}"
        holder.timestampTextView.text = "Waktu: ${measurement.formattedTimestamp ?: "N/A"}"
        holder.typeTextView.text = "Tipe: ${measurement.type}"

    }

    fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy 'pada' HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    override fun getItemCount(): Int = measurements.size
}

