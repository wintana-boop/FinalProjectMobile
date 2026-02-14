package com.example.finalproject.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.R
import com.example.finalproject.data.repository.CourseRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
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
                tvStats.textDirection = TextView.TEXT_DIRECTION_RTL
                tvStats.textAlignment = TextView.TEXT_ALIGNMENT_VIEW_END

                val dataSet = PieDataSet(entries, "")
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS.toList())
                dataSet.valueTextSize = 13f
                dataSet.valueTextColor = Color.BLACK

                val data = PieData(dataSet)


                pieChart.data = data
                pieChart.description.isEnabled = false          // מוריד "Courses by Category"
                pieChart.setDrawEntryLabels(false)              // לא מצייר שמות ליד הפרוסות (מונע בלגן)
                pieChart.setUsePercentValues(false)

                pieChart.isDrawHoleEnabled = true
                pieChart.holeRadius = 55f
                pieChart.transparentCircleRadius = 60f

                pieChart.setDrawCenterText(true)
                pieChart.centerText = "קורסים לפי קטגוריה"
                pieChart.setCenterTextSize(14f)
                pieChart.setCenterTextColor(Color.BLACK)


                pieChart.setExtraOffsets(16f, 16f, 16f, 16f)


                val legend = pieChart.legend
                legend.isEnabled = true
                legend.textSize = 12f
                legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                legend.orientation = Legend.LegendOrientation.HORIZONTAL
                legend.setDrawInside(false)
                legend.xEntrySpace = 12f
                legend.yEntrySpace = 8f

                pieChart.invalidate()
            }
        }
    }
}
