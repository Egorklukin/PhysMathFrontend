package com.example.physmath.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subjectId: String
)