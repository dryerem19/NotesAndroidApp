package com.example.notes.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Priority::class,
            parentColumns = ["id"],
            childColumns = ["priority_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String,
    var text: String,
    @ColumnInfo(name = "category_id", index = true)
    var categoryId: Int,
    @ColumnInfo(name = "priority_id", index = true)
    var priorityId: Int
) : java.io.Serializable