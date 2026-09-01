package com.sleepaudio.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.sleepaudio.app.data.AudioFile

/**
 * Lit les métadonnées disponibles d'un URI sélectionné via le Storage Access
 * Framework, sans jamais exposer l'URI technique brute à l'utilisateur.
 */
object AudioUtils {

    fun resolveAudioFile(context: Context, uri: Uri): AudioFile {
        var displayName = "Fichier audio"
        var mimeType: String? = context.contentResolver.getType(uri)

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                displayName = cursor.getString(nameIndex) ?: displayName
            }
        }

        return AudioFile(
            uri = uri.toString(),
            displayName = displayName,
            mimeType = mimeType
        )
    }

    /** Tente de conserver une permission persistante sur l'URI, si Android le permet. */
    fun tryPersistPermission(context: Context, uri: Uri) {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
    }
}
