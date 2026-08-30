package com.sleepaudio.app.data

/**
 * Représente un fichier audio sélectionné par l'utilisateur via le
 * Storage Access Framework. Ne contient jamais l'URI brute affichée
 * telle quelle à l'utilisateur : [displayName] est ce qui doit être montré.
 */
data class AudioFile(
    val uri: String,
    val displayName: String,
    val durationMs: Long? = null,
    val mimeType: String? = null
)
