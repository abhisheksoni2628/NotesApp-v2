package com.example.mynotes.database

import android.graphics.Bitmap
import android.icu.text.CaseMap.Title
import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mynotes.model.NoteDto

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteDto)

    @Delete
    suspend fun delete(note: NoteDto)

    @Query("Select * from notes_table order by id ASC")
    fun getAllNotes() : LiveData<List<NoteDto>>

    @Query("UPDATE notes_table Set title = :title, note = :note, image= :image WHERE id = :id")
    suspend fun update(id: Int?, title: String?, note: String?, image: ByteArray?)

}