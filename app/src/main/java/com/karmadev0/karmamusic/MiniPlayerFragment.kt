package com.karmadev0.karmamusic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

class MiniPlayerFragment : Fragment() {

    private lateinit var tvMiniTitle: TextView
    private lateinit var btnMiniPlayPause: ImageButton
    private lateinit var btnMiniNext: ImageButton
    private lateinit var btnMiniPrevious: ImageButton
    private lateinit var miniRoot: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_mini_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvMiniTitle = view.findViewById(R.id.tvMiniTitle)
        btnMiniPlayPause = view.findViewById(R.id.btnMiniPlayPause)
        btnMiniNext = view.findViewById(R.id.btnMiniNext)
        btnMiniPrevious = view.findViewById(R.id.btnMiniPrevious)
        miniRoot = view.findViewById(R.id.miniPlayerRoot)

        val main = activity as? MainActivity
        val currentSong = main?.getCurrentSong()

        tvMiniTitle.text = currentSong?.title ?: "Sin título"

        btnMiniPlayPause.setOnClickListener {
            main?.togglePlayPause()
            updatePlayPauseIcon()
        }

        btnMiniNext.setOnClickListener {
            main?.playNextSong()
        }

        btnMiniPrevious.setOnClickListener {
            main?.playPreviousSong()
        }

        // Tocar fuera de los controles abre el reproductor completo
        miniRoot.setOnClickListener { openFullPlayer() }

        updatePlayPauseIcon()
    }

    fun updateMiniPlayerUI(song: Song) {
        tvMiniTitle.text = song.title
        updatePlayPauseIcon()
    }

    private fun updatePlayPauseIcon() {
        val isPlaying = (activity as? MainActivity)?.mediaPlayer?.isPlaying ?: false
        val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        btnMiniPlayPause.setImageResource(icon)
    }

    private fun openFullPlayer() {
        val main = activity as? MainActivity ?: return
        val fullPlayer = FullPlayerFragment()

        val song = main.getCurrentSong()
        val args = Bundle()
        args.putString("SONG_TITLE", song?.title)
        args.putString("SONG_ARTIST", song?.artist)
        fullPlayer.arguments = args

        main.supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.playerContainer, fullPlayer) // Asegúrate de que este contenedor exista
            .addToBackStack(null)
            .commit()
    }
}