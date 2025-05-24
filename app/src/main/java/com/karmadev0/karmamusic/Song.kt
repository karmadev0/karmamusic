package com.karmadev0.karmamusic

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val contentUri: Uri // sepa q es esto pero bue
)
