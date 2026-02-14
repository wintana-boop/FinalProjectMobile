package com.example.finalproject.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.finalproject.data.model.CourseEntity
import com.example.finalproject.data.model.CategoryCount

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses ORDER BY title")
    suspend fun getAll(): List<CourseEntity>

    // 🔥 Pagination
    @Query("""
        SELECT * FROM courses
        ORDER BY title
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getPaged(limit: Int, offset: Int): List<CourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity): Long

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("DELETE FROM courses")
    suspend fun clearAll()

    @Query("""
        SELECT category, COUNT(*) as count
        FROM courses
        GROUP BY category
    """)
    suspend fun countByCategory(): List<CategoryCount>
}
