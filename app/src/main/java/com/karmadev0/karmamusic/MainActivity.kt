package com.karmadev0.karmamusic

import android.os.Bundle
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.content.pm.PathPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.provider.MediaStore
import android.net.Uri
import android.database.Cursor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaPlayer

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_READ_MEDIA_AUDIO = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
            // solicitar permiso
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                REQUEST_CODE_READ_MEDIA_AUDIO
            )
        } else {
            // ya está concedido
            loadMusicFiles()

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_MEDIA_AUDIO) {
           if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               // permiso concedido dea
               loadMusicFiles()
           } else {
               // permiso denegado no Papu
               // debo descubrir como
           }
        }
    }

    private fun loadMusicFiles() {
        val songList = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI // MUSICA EXTERNA SHEEEEEESH
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )
        // filtrar archivos xdd
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC" // orden de titulos

        val cursor: Cursor? = contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )

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
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                songList.add(Song(id, title, artist, album, duration, contentUri))
            }
        }

        if (songList.isNotEmpty()) {
            // tenemos canciones Yupi
            println("Canciones encontradas: ${songList.size}")
            // función RecycleView
            setupRecyclerView(songList)
        } else {
            println("no se encontró ninguna canción pipipi")
        }

    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var mediaPlayer: MediaPlayer? = null // variable para el reproductor

    private fun setupRecyclerView(songList: List<Song>) {
        recyclerView = findViewById(R.id.recyclerViewSongs)
        songAdapter = SongAdapter(songList) { song ->
            playSong(song)
        }
        recyclerView.adapter = songAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun playSong(song: Song) {
        mediaPlayer?.release() // libera si ya existe
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@MainActivity, song.contentUri)
            prepare() // prepara la canción
            start() // música siiiiiii
            // añade un texto de canción terminada
            setOnCompletionListener {
                println("Canción terminada: ${song.title}")
                // despues implementar siguientes canciones
            }
        }
        println("Reproduciendo: ${song.title}")
    }

    // liberar media cuando se destruya
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}





















