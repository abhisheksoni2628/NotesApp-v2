package com.example.mynotes.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.adapter.NoteAdapter
import com.example.mynotes.database.NoteDatabase
import com.example.mynotes.databinding.ActivityMainBinding
import com.example.mynotes.interfaces.OnItemClickListener
import com.example.mynotes.model.NoteDto
import com.example.mynotes.viewmodel.NoteViewModel
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
            Log.e(TAG, "onCreate: $data")
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
        binding.progressBar.visibility = GONE
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
     binding.searchView.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(arg0: Editable) {
                        // TODO Auto-generated method stub
                        val text: String = binding.searchView.text.toString().toLowerCase(Locale.getDefault())
                        adapter.filterList(text)
                    }

                    override fun beforeTextChanged(
                        arg0: CharSequence, arg1: Int,
                        arg2: Int, arg3: Int
                    ) {
                        // TODO Auto-generated method stub
                    }

                    override fun onTextChanged(
                        arg0: CharSequence, arg1: Int, arg2: Int,
                        arg3: Int
                    ) {
                        // TODO Auto-generated method stub
                    }
                })

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createPDF(noteTitle: String, noteContent: String, imageBitmap: Bitmap?) {

        binding.progressBar.visibility = VISIBLE

        GlobalScope.launch(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val outputPath =
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/${noteTitle}_${timestamp}.pdf"
            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(outputPath))

            document.open()

            // Add the title
            val title = Paragraph(noteTitle)
            title.alignment = Element.ALIGN_CENTER
            document.add(title)

            // Convert the bitmap to a file
            imageBitmap?.let {
                val imageFile = File(outputPath.replace(".pdf", ".jpg"))
                try {
                    val fileOutputStream = FileOutputStream(imageFile)
                    imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = GONE
                    }
                    return@launch

                }

                // Add the image
                val image = Image.getInstance(imageFile.absolutePath)
                image.alignment = Element.ALIGN_CENTER
                document.add(image)
                imageFile.delete()
            }


            // Add the content
            val content = Paragraph(noteContent)
            document.add(content)

            document.close()

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = GONE
                Toast.makeText(this@MainActivity, "Exported", Toast.LENGTH_SHORT).show()
                // Handle the created PDF file
            }

        }
    }

    private fun getResizedBitmap(bm: Bitmap?, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm?.width
        val height = bm?.height
        val scaleWidth = newWidth.toFloat() / width!!
        val scaleHeight = newHeight.toFloat() / height!!
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

    override fun OnItemClick(note: NoteDto) {
        val intent = Intent(this, AddNote::class.java)
        intent.putExtra("current_note", note)
        updateNote.launch(intent)
    }

    override fun OnLongItemclicked(note: NoteDto, cardView: CardView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Export this Note")
        builder.setMessage("Are you sure to export this note ?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes"){dialog, id ->
            selectedNote = note
            var bmp = selectedNote.image?.size?.let {
                BitmapFactory.decodeByteArray(selectedNote.image, 0,
                    it
                )
            }

            selectedNote.image?.let {
                bmp = getResizedBitmap(bmp, 320, 400)
            }
            createPDF(selectedNote.title, selectedNote.description, bmp)


        }

        builder.setNegativeButton("No"){dialog, id->

            dialog.cancel()

        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }


}