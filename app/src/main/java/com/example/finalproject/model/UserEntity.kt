package com.example.finalproject.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val email: String,
    val passwordHash: String,
    val role: String // ADMIN / USER
)

