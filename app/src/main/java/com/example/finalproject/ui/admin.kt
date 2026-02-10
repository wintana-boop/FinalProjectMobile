package com.example.finalproject.ui.admin


import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finalproject.R

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDesc = findViewById<EditText>(R.id.etDesc)
        val etImageUrl = findViewById<EditText>(R.id.etImageUrl)
        val etVideoUrl = findViewById<EditText>(R.id.etVideoUrl)
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val categories = listOf("מתמטיקה", "שפות תכנות", "מדעי המחשב")
        spCategory.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val imageUrl = etImageUrl.text.toString().trim()
            val videoUrl = etVideoUrl.text.toString().trim()
            val category = spCategory.selectedItem.toString()

            var isValid = true
            if (title.isEmpty()) { etTitle.error = "יש להזין כותרת"; isValid = false }
            if (desc.isEmpty()) { etDesc.error = "יש להזין תיאור"; isValid = false }
            if (imageUrl.isEmpty()) { etImageUrl.error = "יש להזין URL לתמונה"; isValid = false }
            if (videoUrl.isEmpty()) { etVideoUrl.error = "יש להזין URL לוידאו"; isValid = false }
            if (!isValid) return@setOnClickListener

            val resultIntent = intent
            resultIntent.putExtra("title", title)
            resultIntent.putExtra("description", desc)
            resultIntent.putExtra("category", category)
            resultIntent.putExtra("imageUrl", imageUrl)
            resultIntent.putExtra("videoUrl", videoUrl)

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
