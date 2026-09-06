# Règles ProGuard/R8 — GEO VOICE (section 62 : optimisation de la Release)
# Room, DataStore et Compose fournissent déjà leurs propres règles de conservation
# via consumer-rules ; ce fichier reste volontairement minimal pour l'instant.

-keepattributes *Annotation*
-keep class com.geovoice.app.data.database.** { *; }
