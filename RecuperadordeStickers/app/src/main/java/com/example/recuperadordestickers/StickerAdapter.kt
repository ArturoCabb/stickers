package com.example.recuperadordestickers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recuperadordestickers.models.Sticker
import com.example.recuperadordestickers.models.StickerPack

class StickerAdapter(private val stickerPacks: List<StickerPack>) : RecyclerView.Adapter<StickerAdapter.StickerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sticker_item, parent, false)
        return StickerViewHolder(view)
    }

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        val sticker = stickerPacks[position].stickers[0]
        Glide.with(holder.itemView.context)
            .load(sticker.uri)
            .into(holder.stickerImageView)
    }

    override fun getItemCount(): Int = stickerPacks.size

    class StickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stickerImageView: ImageView = itemView.findViewById(R.id.stickerImageView)
    }
}