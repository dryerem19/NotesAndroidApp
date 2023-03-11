package com.example.notes.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "priorities")
data class Priority(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val value: Int
)