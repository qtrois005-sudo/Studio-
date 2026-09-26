package com.geovoice.app.domain.voice

/** Commandes vocales reconnues par GEO VOICE (ensemble volontairement restreint pour le MVP). */
sealed class VoiceCommand {
    object WhereAmI : VoiceCommand()
    object Unknown : VoiceCommand()
}

/**
 * Interprète un texte reconnu par la reconnaissance vocale et le convertit en commande.
 * Reconnaissance simple par mots-clés — suffisante pour une première commande unique.
 */
object VoiceCommandInterpreter {

    private val whereAmIKeywords = listOf(
        "où suis-je", "ou suis je", "où je suis", "ou je suis",
        "quelle est ma position", "où je me trouve", "ou je me trouve",
        "où suis je", "position actuelle"
    )

    fun interpret(spokenText: String): VoiceCommand {
        val normalized = spokenText.lowercase().trim()
        return if (whereAmIKeywords.any { normalized.contains(it) }) {
            VoiceCommand.WhereAmI
        } else {
            VoiceCommand.Unknown
        }
    }
}
