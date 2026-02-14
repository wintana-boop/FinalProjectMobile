package com.example.finalproject.data.repository

import android.content.Context
import com.example.finalproject.data.AppDatabase
import com.example.finalproject.data.model.CourseEntity
import com.example.finalproject.data.model.CategoryCount

class CourseRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).courseDao()

    suspend fun getAllCourses(): List<CourseEntity> {
        return dao.getAll()
    }

    // ✅ Pagination
    suspend fun getPagedCourses(limit: Int, offset: Int): List<CourseEntity> {
        return dao.getPaged(limit, offset)
    }

    suspend fun insert(course: CourseEntity): Long {
        return dao.insert(course)
    }

    suspend fun delete(course: CourseEntity) {
        dao.delete(course)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun getCountByCategory(): List<CategoryCount> {
        return dao.countByCategory()
    }
}
