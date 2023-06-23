package com.example.mynotes.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.R
import com.example.mynotes.adapter.NoteAdapter
import com.example.mynotes.database.NoteDatabase
import com.example.mynotes.databinding.ActivityMainBinding
import com.example.mynotes.interfaces.OnItemClickListener
import com.example.mynotes.model.NoteDto
import com.example.mynotes.viewmodel.NoteViewModel


class MainActivity : AppCompatActivity(), OnItemClickListener {

    val TAG = "MainActivity"

    lateinit var viewModel: NoteViewModel
    lateinit var adapter: NoteAdapter
    lateinit var database: NoteDatabase
    lateinit var selectedNote : NoteDto
    var NotesList = ArrayList<NoteDto>()
    //Update Note ->
    private val updateNote = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        if (it.resultCode == RESULT_OK){

            val note = it.data?.getSerializableExtra("note") as? NoteDto
            if (note != null){
                viewModel.updateNote(note)
            }
        }

    }

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialization of UI

        initialize()


        //BroadCast Receiver Task ->
        var data = binding.etSend.text
        binding.btnSend.setOnClickListener {
            Log.e(TAG, "onCreate: $data", )
            val intent = Intent("com.example.mynotes.ACTION_SEND")
            intent.putExtra("com.example.mynotes.EXTRA_DATA", data.toString())
            sendBroadcast(intent)
        }


        //View Model init

        viewModel = ViewModelProvider(this,
            ViewModelProvider
                .AndroidViewModelFactory
                .getInstance(application))[NoteViewModel::class.java]

        viewModel.allNotes.observe(this) {
            it?.let {
                adapter.updateList(it)
            }
        }

        database = NoteDatabase.getDatabase(this)


        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Delete this Note")
                builder.setMessage("Are you sure to delete this note ?")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                val selectedNotes = NotesList[viewHolder.adapterPosition]

                builder.setPositiveButton("Yes"){dialog, id ->

                    viewModel.deleteNote(selectedNotes)
                    adapter.notifyDataSetChanged()
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)

                }

                builder.setNegativeButton("No"){dialog, id->

                    viewModel.insertNote(selectedNotes)
                    adapter.notifyDataSetChanged()
                    adapter.notifyItemInserted(viewHolder.adapterPosition)
                    dialog.cancel()

                }

                val alertDialog: AlertDialog = builder.create()

                alertDialog.setCancelable(false)
                alertDialog.show()


            }
        }).attachToRecyclerView(binding.rvNotes)

    }

    private fun initialize() {
        binding.searchView.queryHint = "Search Notes"
        adapter = NoteAdapter(this, this, NotesList)
        binding.rvNotes.adapter = adapter

        val getcontent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == RESULT_OK) {
                val note = it.data?.getSerializableExtra("note") as? NoteDto
                note?.let {
                    viewModel.insertNote(note)
                }
            }
        }

        binding.fbAdd.setOnClickListener {

            val intent = Intent(this, AddNote::class.java)
            getcontent.launch(intent)

        }

        //SearchView ->
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText != null){
                    adapter.filterList(newText)
                }

                return true
            }

        })

    }

    override fun OnItemClick(note: NoteDto) {
        val intent = Intent(this, AddNote::class.java)
        intent.putExtra("current_note", note)
        updateNote.launch(intent)
    }

    override fun OnLongItemclicked(note: NoteDto, cardView: CardView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete this Note")
        builder.setMessage("Are you sure to delete this note ?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes"){dialog, id ->

            selectedNote = note
            viewModel.deleteNote(selectedNote)

        }

        builder.setNegativeButton("No"){dialog, id->

            dialog.cancel()

        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }


}