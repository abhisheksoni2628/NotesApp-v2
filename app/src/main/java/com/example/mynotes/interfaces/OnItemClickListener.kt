package com.example.mynotes.interfaces

import androidx.cardview.widget.CardView
import com.example.mynotes.model.NoteDto

interface OnItemClickListener {

    fun OnItemClick(note: NoteDto)

    fun OnLongItemclicked(note: NoteDto, cardView: CardView)

}