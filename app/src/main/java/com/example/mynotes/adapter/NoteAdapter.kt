package com.example.mynotes.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.R
import com.example.mynotes.databinding.ItemviewListBinding
import com.example.mynotes.interfaces.OnItemClickListener
import com.example.mynotes.model.NoteDto


class NoteAdapter(private val context: Context, private val listener: OnItemClickListener, var NotesList : ArrayList<NoteDto>): RecyclerView.Adapter<NoteAdapter.ViewHolder>(){


    private var FullList = ArrayList<NoteDto>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteAdapter.ViewHolder {
        val binding = DataBindingUtil.inflate<ItemviewListBinding>(
            LayoutInflater.from(parent.context), R.layout.itemview_list, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteAdapter.ViewHolder, position: Int) {
        holder.sendData(NotesList[position])

        val bmp = NotesList[position].image?.size?.let {
            BitmapFactory.decodeByteArray(NotesList[position].image, 0,
                it
            )
        }
        holder.binding.ivImg.setImageBitmap(bmp)

        holder.binding.cardLayout.setOnClickListener {
            listener.OnItemClick(NotesList[holder.adapterPosition])
        }
        holder.binding.cardLayout.setOnLongClickListener {
            listener.OnLongItemclicked(NotesList[holder.adapterPosition], holder.binding.cardLayout)
            true
        }
    }

    override fun getItemCount(): Int {
        return NotesList.size
    }

    fun updateList(newList: List<NoteDto>){

        FullList.clear()
        FullList.addAll(newList)

        NotesList.clear()
        NotesList.addAll(FullList)

        notifyDataSetChanged()

    }

    fun filterList(search: String){

        NotesList.clear()

        for (item in FullList){
            if (item.title?.lowercase()?.contains(search.lowercase()) == true ||
                    item.description?.lowercase()?.contains(search.lowercase()) == true){

                NotesList.add(item)

            }
        }
        notifyDataSetChanged()

    }

    class ViewHolder(val binding: ItemviewListBinding): RecyclerView.ViewHolder(binding.root) {
        fun sendData(data: NoteDto){
            binding.myData = data
        }
    }



}