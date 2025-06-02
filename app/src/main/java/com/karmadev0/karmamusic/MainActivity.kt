package com.karmadev0.karmamusic

import android.os.Bundle
import android.os.Build
import android.view.View
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

    // RecyclerView y adaptador para la lista de canciones
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter

    // MediaPlayer compartido
    var mediaPlayer: MediaPlayer? = null

    // Lista completa de canciones y posición actual
    private var songList: List<Song> = emptyList()
    var currentSongIndex: Int = 0       // Hacerlo 'var' y público permite acceder desde fragmentos
    private var currentSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) Cargar el MiniPlayerFragment desde el inicio
        supportFragmentManager.beginTransaction()
            .replace(R.id.miniContainer, MiniPlayerFragment())
            .commit()

        // 2) Pedir permisos y cargar canciones
        checkAndRequestPermissions()
    }

    // Método para devolver la canción actual
    fun getCurrentSong(): Song? = currentSong

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_MEDIA_AUDIO &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

        val cursor: Cursor? =
            contentResolver.query(collection, projection, selection, null, sortOrder)

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

                list.add(Song(id, title, artist, album, duration, contentUri))
            }
        }

        if (list.isNotEmpty()) {
            songList = list
            setupRecyclerView(songList)

            // Reproducir la primera canción automáticamente
            currentSongIndex = 0
            currentSong = songList[currentSongIndex]
            playSong(currentSong!!)
        }
    }

    private fun setupRecyclerView(songList: List<Song>) {
        recyclerView = findViewById(R.id.recyclerViewSongs)
        songAdapter = SongAdapter(songList) { song ->
            // Al hacer clic en un ítem de la lista:
            currentSongIndex = songList.indexOf(song)
            currentSong = songList[currentSongIndex]
            playSong(songList[currentSongIndex])
        }
        recyclerView.adapter = songAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun playSong(song: Song) {
        // 1) Si ya había un MediaPlayer, reinícialo y libéralo
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null

        // 2) Crea y ejecuta un nuevo MediaPlayer
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@MainActivity, song.contentUri)
            prepare()
            start()
            setOnCompletionListener {
                playNextSong()
            }
        }

        // 3) Actualiza o abre el FullPlayerFragment
        updateOrOpenPlayer(song)
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause() else it.start()
        }
    }

    fun playNextSong() {
        if (currentSongIndex < songList.size - 1) {
            currentSongIndex++
            currentSong = songList[currentSongIndex]
            playSong(songList[currentSongIndex])
        }
    }

    fun playPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--
            currentSong = songList[currentSongIndex]
            playSong(songList[currentSongIndex])
        }
    }

    private fun updateOrOpenPlayer(song: Song) {
        val fragment = supportFragmentManager.findFragmentById(R.id.playerContainer)
        if (fragment is FullPlayerFragment && fragment.isVisible) {
            // Si el FullPlayer ya está delante, solo actualizamos sus datos
            fragment.updateSongInfo(song)
        } else {
            // Ocultamos el MiniPlayerFragment
            supportFragmentManager.beginTransaction()
                .hide(supportFragmentManager.findFragmentById(R.id.miniContainer)!!)
                .commitNow()

            // Abrimos (o reemplazamos) el FullPlayerFragment
            val bundle = Bundle().apply {
                putString("SONG_URI", song.contentUri.toString())
                putString("SONG_TITLE", song.title)
                putString("SONG_ARTIST", song.artist)
            }
            val newFragment = FullPlayerFragment().apply {
                arguments = bundle
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.playerContainer, newFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        if (fm.backStackEntryCount > 0) {
            // Si hay un FullPlayerFragment en back stack, lo removemos
            fm.popBackStack()
            // Luego mostramos de nuevo el MiniPlayerFragment
            fm.beginTransaction()
                .show(fm.findFragmentById(R.id.miniContainer)!!)
                .commitNow()
        } else {
            super.onBackPressed()
        }
    }

    fun showMiniPlayerAndSongList() {
        // Mostrar el fragmento MiniPlayer
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.playerContainer, MiniPlayerFragment())
            .commit()

        // Mostrar la lista de canciones
        findViewById<RecyclerView>(R.id.recyclerViewSongs).visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}