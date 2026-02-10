package com.example.finalproject.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R
import com.example.finalproject.data.Course
import com.example.finalproject.data.repository.AuthRepository
import com.example.finalproject.data.repository.CourseRepository
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
    private val allCourses = arrayListOf<Course>()

    // דיפולט (ישמרו ל-DB רק אם הטבלה ריקה)
    private val defaultCourses = listOf(
        Course(title="אינפי 1", description="גבולות, נגזרות ואינטגרלים בסיסיים.", category="מתמטיקה",
            imageRes=R.drawable.img_math, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=WUvTyaaNkzM"),
        Course(title="אלגברה לינארית", description="וקטורים, מטריצות ומרחבים וקטוריים.", category="מתמטיקה",
            imageRes=R.drawable.img_math, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=ZK3O402wf1c"),
        Course(title="סטטיסטיקה והסתברות", description="התפלגויות, הסתברות מותנית והסקה סטטיסטית.", category="מתמטיקה",
            imageRes=R.drawable.img_math, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=xxpc-HPKN28"),
        Course(title="בדידה 1", description="לוגיקה, גרפים, אינדוקציה ורקורסיה.", category="מתמטיקה",
            imageRes=R.drawable.img_math, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=QW5b0v4mF0o"),
        Course(title="Java", description="מחלקות, ירושה, OOP ו-Collections.", category="שפות תכנות",
            imageRes=R.drawable.img_code, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=eIrMbAQSU34"),
        Course(title="Python", description="משתנים, פונקציות, רשימות וקבצים.", category="שפות תכנות",
            imageRes=R.drawable.img_code, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=rfscVS0vtbw"),
        Course(title="C#", description="תחביר, OOP והיכרות עם .NET.", category="שפות תכנות",
            imageRes=R.drawable.img_code, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=GhQdlIFylQ8"),
        Course(title="מבני נתונים", description="רשימות, מחסנית, תור, עצים וטבלאות גיבוב.", category="מדעי המחשב",
            imageRes=R.drawable.img_cs, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=bum_19loj9A"),
        Course(title="אלגוריתמים", description="גרידיים, דינמי, סיבוכיות וניתוח אלגוריתמים.", category="מדעי המחשב",
            imageRes=R.drawable.img_cs, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=rL8X2mlNHPM"),
        Course(title="מערכות הפעלה", description="תהליכים, תזמון, זיכרון, סנכרון ו-Deadlocks.", category="מדעי המחשב",
            imageRes=R.drawable.img_cs, imageUrl=null, videoUrl="https://www.youtube.com/watch?v=26QPDBe-NB8")
    )

    private val addCourseLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult

            val title = data.getStringExtra("title") ?: return@registerForActivityResult
            val desc = data.getStringExtra("description") ?: ""
            val category = data.getStringExtra("category") ?: "מתמטיקה"
            val imageUrl = data.getStringExtra("imageUrl") ?: ""
            val videoUrl = data.getStringExtra("videoUrl") ?: ""

            val newCourseNoId = Course(
                title = title,
                description = desc,
                category = category,
                imageRes = null,
                imageUrl = imageUrl,
                videoUrl = videoUrl
            )

            // להוסיף ל-DB ואז לרענן רשימה
            lifecycleScope.launch(Dispatchers.IO) {
                courseRepo.insert(newCourseNoId)
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        courseRepo = CourseRepository(this)

        // Logout
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val authRepo = AuthRepository(this)
        btnLogout.setOnClickListener {
            authRepo.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val btnStats = findViewById<Button>(R.id.btnStats)

        btnStats.setOnClickListener {
            startActivity(Intent(this, com.example.finalproject.ui.StatsActivity::class.java))
        }


        // Role + FAB
        val tvRole = findViewById<TextView>(R.id.tvRole)
        val isAdmin = intent.getBooleanExtra("isAdmin", false)
        tvRole.text = if (isAdmin) "Role: Admin" else "Role: Student"

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        if (isAdmin) {
            fabAdd.visibility = View.VISIBLE
            fabAdd.setOnClickListener {
                addCourseLauncher.launch(Intent(this, AdminActivity::class.java))
            }
        } else {
            fabAdd.visibility = View.GONE
        }

        // Recycler
        val rv = findViewById<RecyclerView>(R.id.rvCourses)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = CourseAdapter(this, allCourses, isAdmin) { course ->
            deleteCourseWithUndo(course)
        }
        rv.adapter = adapter

        // Spinner
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val categories = listOf("All", "מתמטיקה", "שפות תכנות", "מדעי המחשב")
        spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spCategory.setSelection(0)
        spCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                val query = findViewById<EditText>(R.id.etSearch).text.toString()
                applyFilters(query)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        // Search
        val etSearch = findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener { applyFilters(it?.toString() ?: "") }

        // Load courses from DB (seed if empty)
        lifecycleScope.launch(Dispatchers.IO) {
            var list = courseRepo.getAllCourses()

            if (list.isEmpty()) {
                // seed defaults once
                defaultCourses.forEach { courseRepo.insert(it) }
                list = courseRepo.getAllCourses()
            }

            withContext(Dispatchers.Main) {
                allCourses.clear()
                allCourses.addAll(list)
                applyFilters(etSearch.text.toString())
            }
        }
    }

    private fun applyFilters(query: String) {
        val q = query.trim()
        val filtered = allCourses.filter { course ->
            val matchesText = course.title.contains(q, ignoreCase = true)
            val matchesCategory = (selectedCategory == "All") || (course.category == selectedCategory)
            matchesText && matchesCategory
        }
        adapter.updateData(filtered)
    }

    private fun deleteCourseWithUndo(course: Course) {
        val index = allCourses.indexOfFirst { it.id == course.id }
        if (index == -1) return

        // UI remove immediately
        allCourses.removeAt(index)
        applyFilters(findViewById<EditText>(R.id.etSearch).text.toString())

        // DB delete
        lifecycleScope.launch(Dispatchers.IO) {
            courseRepo.delete(course)
        }

        Snackbar.make(findViewById(R.id.main), "הקורס נמחק", Snackbar.LENGTH_LONG)
            .setAction("בטל") {
                // UI add back
                allCourses.add(index, course)
                applyFilters(findViewById<EditText>(R.id.etSearch).text.toString())

                // DB insert back (new id may be assigned if it was deleted)
                lifecycleScope.launch(Dispatchers.IO) {
                    courseRepo.insert(course.copy(id = 0))
                    val fresh = courseRepo.getAllCourses()
                    withContext(Dispatchers.Main) {
                        allCourses.clear()
                        allCourses.addAll(fresh)
                        applyFilters(findViewById<EditText>(R.id.etSearch).text.toString())
                    }
                }
            }
            .show()
    }
}
