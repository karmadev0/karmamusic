package com.karmadev0.karmamusic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class SongAdapter (
    private var songs: List<Song>,
    private val onItemClick: (Song) -> Unit // manejo de clicks
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    // pa q se mantenga las music
    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val artistTextView: TextView = itemView.findViewById(R.id.textViewArtist)

        fun bind(song: Song, clickListener: (Song) -> Unit) {
            titleTextView.text = song.title
            artistTextView.text = song.artist
            itemView.setOnClickListener { clickListener(song) }
        }
    }

    // nuevas vistas sheeesh
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_song, parent, false)
        return SongViewHolder(view)
    }

    //remplaza el contenido
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position], onItemClick)
    }

    // devuelve el tamaño
    override fun getItemCount() = songs.size

    // función para actualizar lista de canciones si cambia
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged() //notifica que cambiaron los datos
    }

}