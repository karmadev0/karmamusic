package com.karmadev0.karmamusic

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView

class FullPlayerFragment : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrent: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_full_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Views
        seekBar = view.findViewById(R.id.seekBar)
        tvCurrent = view.findViewById(R.id.tvCurrent)
        tvRemaining = view.findViewById(R.id.tvRemaining)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)
        btnNext = view.findViewById(R.id.btnNext)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        tvTitle = view.findViewById(R.id.songTitle)
        tvArtist = view.findViewById(R.id.songArtist)

        // Carga inicial
        tvTitle.text = arguments?.getString("SONG_TITLE") ?: "Sin título"
        tvArtist.text = arguments?.getString("SONG_ARTIST") ?: "Desconocido"

        // Configura botones
        btnPlayPause.setOnClickListener {
            (activity as? MainActivity)?.togglePlayPause()
            updatePlayPauseIcon()
        }

        btnNext.setOnClickListener {
            (activity as? MainActivity)?.playNextSong()
        }

        btnPrevious.setOnClickListener {
            (activity as? MainActivity)?.playPreviousSong()
        }

        // Configura SeekBar
        updateSeekBar()
    }

    fun updateSongInfo(song: Song) {
        tvTitle.text = song.title
        tvArtist.text = song.artist
        updateSeekBar()
        updatePlayPauseIcon()
    }

    private fun updatePlayPauseIcon() {
        val isPlaying = (activity as? MainActivity)?.mediaPlayer?.isPlaying ?: false
        val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        btnPlayPause.setImageResource(icon)
    }

    private fun updateSeekBar() {
        val mediaPlayer = (activity as? MainActivity)?.mediaPlayer ?: return
        seekBar.max = mediaPlayer.duration

        handler.postDelayed(object : Runnable {
            override fun run() {
                val mp = (activity as? MainActivity)?.mediaPlayer
                if (mp != null) {
                    seekBar.progress = mp.currentPosition
                    tvCurrent.text = formatTime(mp.currentPosition)
                    tvRemaining.text = "-${formatTime(mp.duration - mp.currentPosition)}"
                    handler.postDelayed(this, 1000)
                }
            }
        }, 0)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    (activity as? MainActivity)?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun formatTime(ms: Int): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%d:%02d", min, sec)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}