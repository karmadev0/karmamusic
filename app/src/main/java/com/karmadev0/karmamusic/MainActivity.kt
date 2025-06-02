package com.karmadev0.karmamusic

import android.os.Bundle
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.database.Cursor
import android.media.MediaPlayer
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_READ_MEDIA_AUDIO = 1
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    var mediaPlayer: MediaPlayer? = null

    private var songList: List<Song> = emptyList()
    private var currentSongIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_READ_MEDIA_AUDIO)
        } else {
            loadMusicFiles()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_MEDIA_AUDIO && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMusicFiles()
        }
    }

    private fun loadMusicFiles() {
        val list = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor: Cursor? = contentResolver.query(collection, projection, selection, null, sortOrder)

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val album = it.getString(albumColumn)
                val duration = it.getLong(durationColumn)
                val contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())

                list.add(Song(id, title, artist, album, duration, contentUri))
            }
        }

        if (list.isNotEmpty()) {
            songList = list
            setupRecyclerView(songList)
            currentSongIndex = 0
            playSong(songList[currentSongIndex])
            openFullPlayerFragment(songList[currentSongIndex])
        }
    }

    private fun setupRecyclerView(songList: List<Song>) {
        recyclerView = findViewById(R.id.recyclerViewSongs)
        songAdapter = SongAdapter(songList) { song ->
            currentSongIndex = songList.indexOf(song)
            openFullPlayerFragment(song)
            playSong(song)
        }
        recyclerView.adapter = songAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun playSong(song: Song) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@MainActivity, song.contentUri)
            prepare()
            start()
            setOnCompletionListener {
                playNextSong()
            }
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause() else it.start()
        }
    }

    fun playNextSong() {
        if (currentSongIndex < songList.size - 1) {
            currentSongIndex++
            val nextSong = songList[currentSongIndex]
            playSong(nextSong)
            openFullPlayerFragment(nextSong)
        }
    }

    fun playPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--
            val previousSong = songList[currentSongIndex]
            playSong(previousSong)
            openFullPlayerFragment(previousSong)
        }
    }

    private fun openFullPlayerFragment(song: Song) {
        val bundle = Bundle().apply {
            putString("SONG_URI", song.contentUri.toString())
            putString("SONG_TITLE", song.title)
            putString("SONG_ARTIST", song.artist)
        }

        val fragment = FullPlayerFragment().apply {
            arguments = bundle
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.playerContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}