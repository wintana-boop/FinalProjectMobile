package com.example.finalproject.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String,
    val category: String,

    val imageRes: Int? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null
)