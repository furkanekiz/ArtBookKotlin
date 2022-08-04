package com.furkanekiz.kotlinartbook.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.furkanekiz.kotlinartbook.ACArt
import com.furkanekiz.kotlinartbook.Art
import com.furkanekiz.kotlinartbook.databinding.RowArtBinding

class AdapterArt(private val artList: ArrayList<Art>): RecyclerView.Adapter<AdapterArt.ArtHolder>() {

    class ArtHolder(val binding: RowArtBinding): RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RowArtBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.tvArtName.text = artList[(position)].name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ACArt::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id", artList[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }
}