package com.geovoice.app.core.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Reçoit BOOT_COMPLETED. Conformément à la section 41 et à l'interdiction n°"lancer un
 * suivi secret" (section 67), ce récepteur NE relance JAMAIS le service de localisation
 * automatiquement. Il se contente de laisser l'état "wasTrackingActive" en préférences,
 * que l'écran d'accueil pourra lire pour proposer explicitement à l'utilisateur de reprendre
 * son suivi (avec les permissions déjà accordées).
 */
class BootRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(
                "GeoVoice.Boot",
                "Redémarrage détecté — aucun suivi relancé automatiquement (par conception)."
            )
            // Aucune action de suivi ici. L'UI (HomeViewModel) lit wasTrackingActive
            // via PreferencesRepository au lancement et propose une reprise explicite.
        }
    }
}
