package com.example.finalproject.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.R
import com.example.finalproject.data.repository.CourseRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val tvStats = findViewById<TextView>(R.id.tvStats)
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val repo = CourseRepository(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val stats = repo.getCountByCategory()

            val text = stats.joinToString("\n") { "${it.category} — ${it.count}" }

            val entries = stats.map { PieEntry(it.count.toFloat(), it.category) }

            withContext(Dispatchers.Main) {

                tvStats.text = text


                val dataSet = PieDataSet(entries, "Courses by Category")
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS.toList())
                dataSet.valueTextSize = 13f
                dataSet.valueTextColor = Color.BLACK

                val data = PieData(dataSet)

                pieChart.data = data
                pieChart.description.isEnabled = false
                pieChart.setUsePercentValues(false)
                pieChart.isDrawHoleEnabled = true
                pieChart.centerText = "Courses"
                pieChart.setEntryLabelTextSize(12f)
                pieChart.setEntryLabelColor(Color.BLACK)


                pieChart.legend.isEnabled = true
                pieChart.legend.textSize = 12f

                pieChart.invalidate()
            }
        }
    }
}
