package com.example.finalproject.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.finalproject.data.Course
import com.example.finalproject.data.model.CategoryCount

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses ORDER BY title")
    suspend fun getAll(): List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course): Long

    @Delete
    suspend fun delete(course: Course)

    @Query("DELETE FROM courses")
    suspend fun clearAll()

    @Query("""
    SELECT category, COUNT(*) as count
    FROM courses
    GROUP BY category """)
    suspend fun countByCategory(): List<CategoryCount>

}
