package com.example.finalproject.data.repository

import android.content.Context
import com.example.finalproject.data.AppDatabase
import com.example.finalproject.data.Course
import com.example.finalproject.data.model.CategoryCount


class CourseRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).courseDao()

    suspend fun getAllCourses(): List<Course> {
        return dao.getAll()
    }

    suspend fun insert(course: Course): Long {
        return dao.insert(course)
    }


    suspend fun delete(course: Course) {
        dao.delete(course)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun getCountByCategory(): List<CategoryCount> {
        return dao.countByCategory()
    }


}
