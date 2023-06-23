package com.example.mynotes.repository

import androidx.lifecycle.LiveData
import com.example.mynotes.database.NoteDao
import com.example.mynotes.model.NoteDto

class NoteRepo(var noteDao: NoteDao) {

    val allNotes: LiveData<List<NoteDto>> = noteDao.getAllNotes()

    suspend fun insert(note: NoteDto){
        noteDao.insert(note)
    }

    suspend fun delete(note: NoteDto){
        noteDao.delete(note)
    }

    suspend fun update(note: NoteDto){
        noteDao.update(note.id, note.title, note.description, note?.image)
    }


}