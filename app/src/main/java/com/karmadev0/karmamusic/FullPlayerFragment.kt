package com.karmadev0.karmamusic

import android.media.MediaPlayer
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

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrent: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton
    private val handler = Handler(Looper.getMainLooper())
    private var songUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_full_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera la URI de la canción desde los argumentos
        arguments?.getString("SONG_URI")?.let {
            songUri = Uri.parse(it)
        }

        // Views
        seekBar = view.findViewById(R.id.seekBar)
        tvCurrent = view.findViewById(R.id.tvCurrent)
        tvRemaining = view.findViewById(R.id.tvRemaining)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)
        btnNext = view.findViewById(R.id.btnNext)
        btnPrevious = view.findViewById(R.id.btnPrevious)

        // Prepara MediaPlayer
        songUri?.let { uri ->
            mediaPlayer = MediaPlayer().apply {
                setDataSource(requireContext(), uri)
                prepare()
                start()
            }
        }

        // Configura SeekBar
        mediaPlayer?.let {
            seekBar.max = it.duration
            updateSeekBar()
        }

        // Listeners de botones
        btnPlayPause.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    btnPlayPause.setImageResource(R.drawable.ic_play_arrow)
                } else {
                    it.start()
                    btnPlayPause.setImageResource(R.drawable.ic_pause)
                }
            }
        }
        // NEXT / PREVIOUS quedan para tu lógica de lista
        btnNext.setOnClickListener { /* saltar a siguiente */ }
        btnPrevious.setOnClickListener { /* saltar a anterior */ }

        // Manejar cambio manual de SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, pos: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer?.seekTo(pos)
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })
    }

    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    seekBar.progress = it.currentPosition
                    tvCurrent.text = formatTime(it.currentPosition)
                    tvRemaining.text = "-${formatTime(it.duration - it.currentPosition)}"
                    handler.postDelayed(this, 1000)
                }
            }
        }, 0)
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
        mediaPlayer?.release()
    }
}