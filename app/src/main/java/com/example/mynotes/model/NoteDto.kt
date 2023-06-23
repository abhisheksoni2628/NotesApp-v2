package com.example.mynotes.model

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "notes_table")
data class NoteDto(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "note")
    val description: String,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val image: ByteArray?,

    @ColumnInfo(name = "date")
    val date: String

)  : Serializable
