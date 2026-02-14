package com.example.finalproject.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.model.CourseEntity
import com.example.finalproject.data.repository.AuthRepository
import com.example.finalproject.data.repository.CourseRepository
import com.example.finalproject.data.repository.RetrofitClient
import com.example.finalproject.ui.adapters.CourseAdapter
import com.example.finalproject.ui.admin.AdminActivity
import com.example.finalproject.ui.login.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    private lateinit var adapter: CourseAdapter
    private lateinit var courseRepo: CourseRepository

    private var selectedCategory: String = "All"
    private val allCourses = arrayListOf<CourseEntity>()

    private val defaultCourses = listOf(
        CourseEntity(title="אינפי 1", description="גבולות, נגזרות ואינטגרלים בסיסיים.", category="מתמטיקה",
            imageRes=R.drawable.img_math, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=WUvTyaaNkzM"),
        CourseEntity(title="אלגברה לינארית", description="וקטורים, מטריצות ומרחבים וקטוריים.", category="מתמטיקה",
            imageRes=R.drawable.img_math, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=ZK3O402wf1c"),
        CourseEntity(title="סטטיסטיקה והסתברות", description="התפלגויות והסקה סטטיסטית.", category="מתמטיקה",
            imageRes=R.drawable.img_math, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=xxpc-HPKN28"),
        CourseEntity(title="Java", description="מחלקות ו-OOP.", category="שפות תכנות",
            imageRes=R.drawable.img_code, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=eIrMbAQSU34")
    )

    private val addCourseLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult

            val newCourse = CourseEntity(
                title = data.getStringExtra("title") ?: return@registerForActivityResult,
                description = data.getStringExtra("description") ?: "",
                category = data.getStringExtra("category") ?: "מתמטיקה",
                imageRes = null,
                imageUrl = data.getStringExtra("imageUrl"),
                videoUrl = data.getStringExtra("videoUrl")
            )

            lifecycleScope.launch(Dispatchers.IO) {
                courseRepo.insert(newCourse)
                val fresh = courseRepo.getAllCourses()
                withContext(Dispatchers.Main) {
                    allCourses.clear()
                    allCourses.addAll(fresh)
                    applyFilters(findViewById<EditText>(R.id.etSearch).text.toString())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 🔥 אינטגרציה חיצונית
        fetchQuote()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        courseRepo = CourseRepository(this)

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            AuthRepository(this).logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val isAdmin = intent.getBooleanExtra("isAdmin", false)

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.visibility = if (isAdmin) View.VISIBLE else View.GONE
        if (isAdmin) {
            fabAdd.setOnClickListener {
                addCourseLauncher.launch(Intent(this, AdminActivity::class.java))
            }
        }

        val rv = findViewById<RecyclerView>(R.id.rvCourses)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = CourseAdapter(this, allCourses, isAdmin) { course ->
            deleteCourseWithUndo(course)
        }
        rv.adapter = adapter

        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val categories = listOf("All", "מתמטיקה", "שפות תכנות")
        spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                applyFilters(findViewById<EditText>(R.id.etSearch).text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        findViewById<EditText>(R.id.etSearch)
            .addTextChangedListener { applyFilters(it?.toString() ?: "") }

        lifecycleScope.launch(Dispatchers.IO) {
            var list = courseRepo.getAllCourses()
            if (list.isEmpty()) {
                defaultCourses.forEach { courseRepo.insert(it) }
                list = courseRepo.getAllCourses()
            }
            withContext(Dispatchers.Main) {
                allCourses.clear()
                allCourses.addAll(list)
                applyFilters("")
            }
        }
    }

    private fun applyFilters(query: String) {
        val filtered = allCourses.filter {
            (selectedCategory == "All" || it.category == selectedCategory) &&
                    it.title.contains(query, ignoreCase = true)
        }
        adapter.updateData(filtered)
    }

    private fun deleteCourseWithUndo(course: CourseEntity) {
        val index = allCourses.indexOfFirst { it.id == course.id }
        if (index == -1) return

        allCourses.removeAt(index)
        applyFilters(findViewById<EditText>(R.id.etSearch).text.toString())

        lifecycleScope.launch(Dispatchers.IO) {
            courseRepo.delete(course)
        }

        Snackbar.make(findViewById(R.id.main), "הקורס נמחק", Snackbar.LENGTH_LONG)
            .setAction("בטל") {
                allCourses.add(index, course)
                applyFilters(findViewById<EditText>(R.id.etSearch).text.toString())
            }
            .show()
    }

    // 🔥 Retrofit Call
    private fun fetchQuote() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.getRandomQuote()

                if (response.isSuccessful) {
                    val quote = response.body()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@HomeActivity,
                            "\"${quote?.content}\" - ${quote?.author}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Failed to connect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
