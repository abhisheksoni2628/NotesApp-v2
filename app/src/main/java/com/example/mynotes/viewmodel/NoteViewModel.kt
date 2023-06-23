package com.example.mynotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.mynotes.database.NoteDatabase
import com.example.mynotes.model.NoteDto
import com.example.mynotes.repository.NoteRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application): AndroidViewModel(application) {

    private val repository: NoteRepo

    val allNotes : LiveData<List<NoteDto>>

    init {
        val dao = NoteDatabase.getDatabase(application).getNoteDao()
        repository = NoteRepo(dao)
        allNotes = repository.allNotes
    }

    fun deleteNote(note: NoteDto){
        viewModelScope.launch(Dispatchers.IO){
            repository.delete(note)
        }
    }

    fun insertNote(note: NoteDto){
        viewModelScope.launch(Dispatchers.IO){
            repository.insert(note)
        }
    }

    fun updateNote(note: NoteDto){
        viewModelScope.launch(Dispatchers.IO){
            repository.update(note)
        }
    }


}