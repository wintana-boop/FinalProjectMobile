package com.example.finalproject.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.R
import com.example.finalproject.data.repository.CourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val tvStats = findViewById<TextView>(R.id.tvStats)
        val repo = CourseRepository(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val stats = repo.getCountByCategory()

            val text = stats.joinToString("\n") {
                "${it.category} — ${it.count}"
            }

            withContext(Dispatchers.Main) {
                tvStats.text = text
            }
        }
    }
}
