package com.example.myapplication


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.charts.ScatterChart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapplication.data.MeasurementGroup


class History : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var userUid: String
    private lateinit var dateTextView: TextView
    private lateinit var scatterChart: ScatterChart
    private val measurements =
        mutableListOf<Measurement>() // Declare measurements as a class-level property
    private lateinit var recyclerView: RecyclerView
    private lateinit var measurementAdapter: MeasurementAdapter
    private lateinit var measurementGroup: MeasurementGroup



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerView) // Initialize the recyclerView

        // Get the userUid from the intent
        userUid = intent.getStringExtra("userUid") ?: ""
        Log.d("History", "userUid: $userUid")

        db = FirebaseFirestore.getInstance()
        dateTextView = findViewById(R.id.dateTextView)

        setupScatterChart()
//        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        measurementAdapter = MeasurementAdapter(mutableListOf()) // Pass an empty mutable list
        recyclerView.adapter = measurementAdapter

        measurementGroup = MeasurementGroup(measurements, db, userUid) // Initialize MeasurementGroup instance



        val showLastWeekButton: Button = findViewById(R.id.oneweek_button)
        showLastWeekButton.setOnClickListener {
            retrieveMeasurementsForLastWeek()
        }

        val showLastMonthButton: Button = findViewById(R.id.onemonth_button)
        showLastMonthButton.setOnClickListener {
            retrieveMeasurementsForLastMonth()
        }
    }
    // Create a new function to set up the ScatterChart
    private fun setupScatterChart() {
        scatterChart = findViewById(R.id.scatter_chart)
        scatterChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        scatterChart.xAxis.textColor = Color.BLACK // Set the x-axis label text color to black
        scatterChart.axisLeft.textColor =
            Color.BLACK // Set the left y-axis label text color to black
        scatterChart.axisRight.textColor =
            Color.BLACK // Set the right y-axis label text color to black

        scatterChart.description.isEnabled = true// Disable the description text

        scatterChart.setDrawGridBackground(true)
        scatterChart.setPinchZoom(true)

        // Customize the x-axis
        val xAxis = scatterChart.xAxis
        xAxis.granularity = 1.0f // Set the minimum interval between two x-axis values (labels)
        xAxis.isGranularityEnabled = true
        xAxis.setCenterAxisLabels(true) // Center x-axis labels on the data point

        // Customize the y-axis
        val yAxis = scatterChart.axisLeft
        yAxis.setDrawGridLines(true) // Hide the horizontal grid lines

        // Hide the right y-axis (we only want to show the left y-axis)
        scatterChart.axisRight.isEnabled = false

        // Customize the legend
        val legend = scatterChart.legend
        legend.isEnabled = true // Disable the legend

        // Set up an empty ScatterData (no need to specify a data set)
        val emptyData = ScatterData()
        scatterChart.data = emptyData

        scatterChart.invalidate()
    }

    private fun updateScatterChart(
        data: ScatterData?,
        formattedStartDate: String,
        formattedEndDate: String
    ) {
        if (data != null) {
            scatterChart.data = data
            scatterChart.invalidate()
            runOnUiThread{

                // Update the dateTextView with the formatted date range
                dateTextView.text = "Date: $formattedStartDate - $formattedEndDate"
            }

        } else {
            scatterChart.clear() // Clear the scatter chart
            dateTextView.text = "No measurements found"
        }
    }


    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }


    private fun retrieveMeasurementsForLastWeek() {
        val lastWeekDates = getLastWeekDates()
        val endTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis // End of the current day
        val startTime = lastWeekDates.first().time

        // Call the function within a coroutine scope
        CoroutineScope(Dispatchers.Main).launch {
            val measurements = fetchAndDisplayScatterChartData(startTime, endTime)
            measurementAdapter.updateData(measurements)
        }
    }

    private fun retrieveMeasurementsForLastMonth() {
        val lastMonthDates = getLastMonthDates()
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis // End of the current day
        val firstTime = lastMonthDates.first().time

        // Call the function within a coroutine scope
        CoroutineScope(Dispatchers.Main).launch {
            val measurements = fetchAndDisplayScatterChartData(firstTime, currentTime)
            measurementAdapter.updateData(measurements)
        }
    }

    private fun getLastWeekDates(): List<Date> {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        val lastWeekDates = mutableListOf<Date>()

        for (i in 6 downTo 0) {
            val clonedCalendar = Calendar.getInstance()
            clonedCalendar.time = currentDate
            clonedCalendar.add(Calendar.DAY_OF_YEAR, -i) // Subtract i days from the current date
            lastWeekDates.add(clonedCalendar.time)
        }

        return lastWeekDates
    }

    private fun getLastMonthDates(): List<Date> {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        val lastMonthDates = mutableListOf<Date>()

        for (i in 29 downTo 0) {
            val clonedCalendar = Calendar.getInstance()
            clonedCalendar.time = currentDate
            clonedCalendar.add(Calendar.DAY_OF_YEAR, -i) // Subtract i days from the current date
            lastMonthDates.add(clonedCalendar.time)
        }

        return lastMonthDates
    }

    // Inside your History class, add a property to store the x-axis labels
    // Inside your History class, add a property to store the x-axis labels
    private val xAxisLabels = mutableListOf<String>()

    // Modify the fetchAndDisplayScatterChartData function as follows:
    private suspend fun fetchAndDisplayScatterChartData(
        startTime: Long,
        endTime: Long
    ): List<Measurement> {
        return withContext(Dispatchers.IO) {
            val startDate = Date(startTime)
            val endDate = Date(endTime)
            val sortedMeasurements = measurementGroup.retrieveMeasurements(startTime, endTime)

            if (sortedMeasurements.isNotEmpty()) {
                // Sort the measurements by timestamp in ascending order
//                sortedMeasurements.sortBy { measurement: Measurement -> measurement.timestamp }

                val entries = mutableListOf<Entry>()
                xAxisLabels.clear() // Clear the existing x-axis labels

                for ((index, measurement) in sortedMeasurements.withIndex()) {
                    val formattedDate = formatTimestamp(measurement.timestamp ?: 0)
                    val entry = Entry(index.toFloat(), measurement.glucoseLevel?.toFloat() ?: 0f)
                    entries.add(entry)
                    xAxisLabels.add(formattedDate) // Add the formatted date to the list of x-axis labels
                }

                val dataSet = ScatterDataSet(entries, "Glucose Level")
                dataSet.scatterShapeSize = resources.getDimension(R.dimen.scatter_point_size)

                val colors = sortedMeasurements.map { measurement ->
                    when (measurement?.category) {
                        "High" -> ContextCompat.getColor(this@History, R.color.high_category_color)
                        "Medium" -> ContextCompat.getColor(this@History, R.color.medium_category_color)
                        "Normal" -> ContextCompat.getColor(this@History, R.color.normal_category_color)
                        "Too low" -> ContextCompat.getColor(this@History, R.color.low_category_color)
                        else -> ContextCompat.getColor(this@History, R.color.default_category_color)
                    }
                }

                dataSet.colors = colors
                val data = ScatterData(dataSet)

                // Call the function to update the ScatterChart with the new data
                updateScatterChart(data, formatTimestamp(startTime), formatTimestamp(endTime))

                // Set the x-axis labels using the formatted date strings
                val xAxis = scatterChart.xAxis
                xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                xAxis.labelCount = xAxisLabels.size // Set the number of x-axis labels
                xAxis.setCenterAxisLabels(true) // Center x-axis labels on the data points

                // Set the formatted timestamp of the last measurement
                val lastMeasurement = sortedMeasurements.lastOrNull()
                if (lastMeasurement != null) {
                    runOnUiThread {
                        dateTextView.text = "Date: ${formatTimestamp(startTime)} - ${formatTimestamp(endTime)}"
                    }
                }
                return@withContext sortedMeasurements
            } else {
                // Handle the case when no measurements are found within the desired time range
                val emptyData = ScatterData() // Create an empty ScatterData
                updateScatterChart(emptyData, formatTimestamp(startTime), formatTimestamp(endTime))
                return@withContext emptyList()
            }
        }
    }

}