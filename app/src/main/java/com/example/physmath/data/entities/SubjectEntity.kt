package com.example.physmath.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val icon: String,
    val color: String
)