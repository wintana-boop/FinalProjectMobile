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
import com.example.finalproject.data.Course
import com.example.finalproject.data.model.CourseEntity
import com.example.finalproject.data.repository.AuthRepository
import com.example.finalproject.data.repository.CourseRepository
import com.example.finalproject.data.repository.RetrofitClient
import com.example.finalproject.ui.StatsActivity
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

    // Pagination state
    private var page = 0
    private val pageSize = 5
    private var isLoading = false
    private var reachedEnd = false

    // ברירת מחדל (קיימת רק כדי לאכלס DB פעם ראשונה / להשלים חסרים)
    private val defaultCourses: List<Course> = listOf(
        Course(
            title = "אינפי 1",
            description = "גבולות, נגזרות ואינטגרלים בסיסיים.",
            category = "מתמטיקה",
            imageRes = R.drawable.img_math,
            videoUrl = "https://www.youtube.com/watch?v=WUvTyaaNkzM"
        ),
        Course(
            title = "אלגברה לינארית",
            description = "וקטורים, מטריצות ומרחבים וקטוריים.",
            category = "מתמטיקה",
            imageRes = R.drawable.img_math,
            videoUrl = "https://www.youtube.com/watch?v=ZK3O402wf1c"
        ),
        Course(
            title = "סטטיסטיקה והסתברות",
            description = "התפלגויות, הסתברות מותנית והסקה סטטיסטית.",
            category = "מתמטיקה",
            imageRes = R.drawable.img_math,
            videoUrl = "https://www.youtube.com/watch?v=xxpc-HPKN28"
        ),
        Course(
            title = "בדידה 1",
            description = "לוגיקה, גרפים, אינדוקציה ורקורסיה.",
            category = "מתמטיקה",
            imageRes = R.drawable.img_math,
            videoUrl = "https://www.youtube.com/watch?v=QW5b0v4mF0o"
        ),
        Course(
            title = "Java",
            description = "מחלקות, ירושה, OOP ו-Collections.",
            category = "שפות תכנות",
            imageRes = R.drawable.img_code,
            videoUrl = "https://www.youtube.com/watch?v=eIrMbAQSU34"
        ),
        Course(
            title = "Python",
            description = "משתנים, פונקציות, רשימות וקבצים.",
            category = "שפות תכנות",
            imageRes = R.drawable.img_code,
            videoUrl = "https://www.youtube.com/watch?v=rfscVS0vtbw"
        ),
        Course(
            title = "C#",
            description = "תחביר, OOP והיכרות עם .NET.",
            category = "שפות תכנות",
            imageRes = R.drawable.img_code,
            videoUrl = "https://www.youtube.com/watch?v=GhQdlIFylQ8"
        ),
        Course(
            title = "מבני נתונים",
            description = "רשימות, מחסנית, תור, עצים וטבלאות גיבוב.",
            category = "מדעי המחשב",
            imageRes = R.drawable.img_cs,
            videoUrl = "https://www.youtube.com/watch?v=bum_19loj9A"
        ),
        Course(
            title = "אלגוריתמים",
            description = "גרידיים, דינמי, סיבוכיות וניתוח אלגוריתמים.",
            category = "מדעי המחשב",
            imageRes = R.drawable.img_cs,
            videoUrl = "https://www.youtube.com/watch?v=rL8X2mlNHPM"
        ),
        Course(
            title = "מערכות הפעלה",
            description = "תהליכים, תזמון, זיכרון, סנכרון ו-Deadlocks.",
            category = "מדעי המחשב",
            imageRes = R.drawable.img_cs,
            videoUrl = "https://www.youtube.com/watch?v=26QPDBe-NB8"
        )
    )

    // המרה מ-Course ל-Entity
    private fun Course.toEntity(): CourseEntity = CourseEntity(
        title = title,
        description = description,
        category = category,
        imageRes = imageRes,
        imageUrl = imageUrl,
        videoUrl = videoUrl
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
                withContext(Dispatchers.Main) {
                    loadMore(reset = true) // רענון בעמודים
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        fetchQuote()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        courseRepo = CourseRepository(this)

        // Logout
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            AuthRepository(this).logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Admin
        val isAdmin = intent.getBooleanExtra("isAdmin", false)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.visibility = if (isAdmin) View.VISIBLE else View.GONE
        if (isAdmin) {
            fabAdd.setOnClickListener {
                addCourseLauncher.launch(Intent(this, AdminActivity::class.java))
            }
        }

        // Stats
        findViewById<Button>(R.id.btnStats).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, com.example.finalproject.ui.ProfileActivity::class.java))
        }


        // RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvCourses)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = CourseAdapter(this, allCourses, isAdmin) { course ->
            deleteCourseWithUndo(course)
        }
        rv.adapter = adapter

        // Load More button
        findViewById<Button>(R.id.btnLoadMore).setOnClickListener {
            loadMore(reset = false)
        }

        // Spinner
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val categories = listOf("All", "מתמטיקה", "שפות תכנות", "מדעי המחשב")
        spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                applyFilters(findViewById<EditText>(R.id.etSearch).text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        findViewById<Button>(R.id.btnGps).setOnClickListener {
            startActivity(Intent(this, com.example.finalproject.ui.LocationActivity::class.java))
        }


        // Search
        findViewById<EditText>(R.id.etSearch).addTextChangedListener {
            applyFilters(it?.toString() ?: "")
        }

        //  Populate DB (השלמת קורסים חסרים בלי כפילויות) ואז עמוד ראשון
        lifecycleScope.launch(Dispatchers.IO) {
            val existing = courseRepo.getAllCourses()
            val existingTitles = existing.map { it.title }.toHashSet()

            defaultCourses
                .filter { it.title !in existingTitles }
                .forEach { c -> courseRepo.insert(c.toEntity()) }

            withContext(Dispatchers.Main) {
                loadMore(reset = true)
            }
        }
    }

    private fun loadMore(reset: Boolean = false) {
        if (isLoading || reachedEnd) return
        isLoading = true

        val btn = findViewById<Button>(R.id.btnLoadMore)
        btn.isEnabled = false
        btn.text = "Loading..."

        if (reset) {
            page = 0
            reachedEnd = false
            allCourses.clear()
            btn.visibility = View.VISIBLE
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val offset = page * pageSize
            val newItems = courseRepo.getPagedCourses(pageSize, offset)

            withContext(Dispatchers.Main) {
                if (newItems.isEmpty()) {
                    reachedEnd = true
                    btn.visibility = View.GONE
                } else {
                    allCourses.addAll(newItems)
                    page++
                    applyFilters(findViewById<EditText>(R.id.etSearch).text.toString())
                    btn.isEnabled = true
                    btn.text = "Load More"
                    btn.visibility = View.VISIBLE
                }
                isLoading = false
            }
        }
    }

    private fun applyFilters(query: String) {
        val filtered = allCourses.filter {
            (selectedCategory == "All" || it.category == selectedCategory) &&
                    it.title.contains(query, ignoreCase = true)
        }
        adapter.updateData(filtered)

        // אם סיימנו את כל העמודים - הכפתור לא יופיע
        val btn = findViewById<Button>(R.id.btnLoadMore)
        btn.visibility = if (reachedEnd) View.GONE else View.VISIBLE
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
                    Toast.makeText(this@HomeActivity, "Failed to connect", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
