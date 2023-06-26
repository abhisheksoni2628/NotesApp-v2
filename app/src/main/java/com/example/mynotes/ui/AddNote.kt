package com.example.mynotes.ui

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.example.mynotes.databinding.ActivityAddNoteBinding
import com.example.mynotes.model.NoteDto
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

class AddNote : AppCompatActivity() {

    companion object{
        private const val RESULT_IMAGE = 1000
    }
    private lateinit var originalImage: Bitmap
    private var image : ByteArray? = null

    lateinit var oldNote: NoteDto
    lateinit var note: NoteDto
    var isUpdate: Boolean = false
    lateinit var binding: ActivityAddNoteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)


        try {
            oldNote = intent.getSerializableExtra("current_note") as NoteDto
            binding.etTitle.setText(oldNote.title)
            binding.etNote.setText(oldNote.description)
            image = oldNote.image
            val bmp = oldNote.image?.size?.let {
                BitmapFactory.decodeByteArray(oldNote.image, 0,
                    it
                )
            }
            binding.ivImage.setImageBitmap(bmp)
            isUpdate = true
        }catch (e: Exception){
            e.printStackTrace()
        }

        binding.imgSaveBtn.setOnClickListener {

            val title = binding.etTitle.text.toString()

            val note_des = binding.etNote.text.toString()

            if (title.isNotEmpty() || note_des.isNotEmpty()){

                val formatter = SimpleDateFormat("EEE, d MM yyyy HH:mm a")

                if (isUpdate) {
                    note = NoteDto(
                        oldNote.id, title, note_des, image,  formatter.format(Date())
                    )
                }
                else{
                    note = NoteDto(
                        null,title, note_des,image, formatter.format(Date())
                    )
                }

                val intent = Intent()
                intent.putExtra("note", note)
                setResult(AppCompatActivity.RESULT_OK, intent)
                finish()

            }
            else{

                Toast.makeText(this@AddNote, "Please Enter Some Data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }

        }

        binding.imgBackBtn.setOnClickListener {

            onBackPressed()

        }

        binding.fbAddImg.setOnClickListener {
            openGallery()
        }

    }
    private fun openGallery() {
        ImagePicker.with(this@AddNote)
            .crop()
            .maxResultSize(720, 720)
            .compress(1024)
            .start(1000)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_IMAGE && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            try {
                originalImage = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                val stream = ByteArrayOutputStream()
                originalImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
                image = stream.toByteArray()
                binding.ivImage.setImageBitmap(originalImage)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show()
        }
    }
}