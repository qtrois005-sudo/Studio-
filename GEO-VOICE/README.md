# GEO VOICE

**« Votre position vous parle. »**

Assistant géographique vocal intelligent en temps réel pour Android — Kotlin, Jetpack Compose, MVVM, Clean Architecture.

---

## ⚠️ État réel du projet — à lire avant tout

Ce dépôt est un **MVP P0** construit à partir du cahier des charges maître (74 sections). Il a été écrit avec soin mais **jamais compilé ni exécuté** (l'environnement où il a été généré n'a ni Android SDK ni accès réseau). Attends-toi à devoir corriger quelques erreurs mineures de compilation en premier build — c'est normal pour un projet de cette taille livré sans compilation intermédiaire.

**Fait, fonctionnel (code réel) :**
- Architecture complète (core / data / domain / service / presentation)
- Identité visuelle réelle : logo (pin + ondes vocales), adaptive icon, launcher, monochrome, splash — aucun placeholder Android générique
- Moteurs P0 : Location, Location Quality (filtre sauts GPS/bruit), Distance (cumulée + seuils 1-50 km), Geography (Geocoder Android), Confidence (HIGH/MEDIUM/LOW), Message Builder (règle "ne jamais inventer")
- Voice Engine (TTS), Voice Queue (anti-chevauchement), Audio Focus Manager
- Notification Engine (4 canaux : suivi, distance, géographie, système)
- Base de données Room (Trip / LocationPoint / Announcement), Trip Engine, Préférences DataStore
- Foreground Service orchestrant l'ensemble
- Écrans : Splash, Onboarding, Permissions, Accueil, Paramètres, Diagnostics — navigation complète

**Pas encore fait :**
- Écran Carte (Google Maps Compose) — dépendance déjà ajoutée, composant non écrit
- Écrans Trajets / détail de trajet, écran "Vous êtes ici" détaillé
- Modes Conduite / Exploration dédiés
- Tests unitaires et instrumentés (section 59)
- Assets Play Store (captures, fiche store)

**Ce que je ne peux pas faire à ta place :**
- Compiler et tester sur un appareil réel
- Créer/gérer ta clé Google Maps Platform ou ton compte Google Cloud
- Créer/gérer ton compte développeur Play Store et y publier l'app
- Garantir un comportement qu'Android lui-même peut restreindre (arrière-plan, fabricants agressifs sur la batterie)

---

## Builder depuis GitHub Codespaces (sans ordinateur)

1. Crée un dépôt GitHub et pousse le contenu de ce dossier.
2. Ouvre le dépôt → bouton **Code** → **Codespaces** → **Create codespace on main**.
3. Dans le terminal du Codespace, installe le SDK Android en ligne de commande :
   ```bash
   sudo apt-get update && sudo apt-get install -y openjdk-17-jdk unzip
   mkdir -p ~/android-sdk/cmdline-tools
   cd ~/android-sdk/cmdline-tools
   curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
   unzip cmdline-tools.zip && mv cmdline-tools latest
   export ANDROID_SDK_ROOT=~/android-sdk
   export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools
   yes | sdkmanager --licenses
   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```
4. Génère le wrapper Gradle (le binaire `gradle-wrapper.jar` n'est pas inclus dans ce livrable) :
   ```bash
   # Si gradle n'est pas déjà présent :
   curl -o gradle.zip https://services.gradle.org/distributions/gradle-8.7-bin.zip
   unzip gradle.zip -d /opt && export PATH=$PATH:/opt/gradle-8.7/bin
   gradle wrapper --gradle-version 8.7
   ```
5. Configure ta clé Google Maps (facultatif pour un premier build sans carte) :
   ```bash
   echo "GEO_VOICE_MAPS_API_KEY=TA_CLE_ICI" >> local.properties
   echo "sdk.dir=$ANDROID_SDK_ROOT" >> local.properties
   ```
6. Build de l'APK debug :
   ```bash
   ./gradlew assembleDebug
   ```
   L'APK se trouve dans `app/build/outputs/apk/debug/`.
7. Récupère l'APK depuis Codespaces (clic droit → Download dans l'explorateur de fichiers VS Code) et installe-le sur ton téléphone Android pour le tester réellement (section 60 : la validation ne peut pas se faire uniquement en émulateur/CI).

---

## Configuration de la clé Google Maps

Ne jamais écrire la clé directement dans un fichier versionné. Utilise `local.properties` (déjà exclu par `.gitignore`) :
```
GEO_VOICE_MAPS_API_KEY=ta_clé_google_maps
```
Restreins cette clé dans Google Cloud Console (empreinte SHA-1 + nom de package `com.geovoice.app`) avant toute publication.

## Permissions demandées

Voir `AndroidManifest.xml` : localisation précise/approximative, localisation en arrière-plan, service de premier plan de localisation, notifications (Android 13+), démarrage au boot (pour proposer une reprise, jamais pour relancer un suivi caché).

## Architecture

```
core/        audio, location, notification, permission, security, time, util
data/        database (Room), geocoding, maps, location, preferences (DataStore)
domain/      distance, geography, confidence, tracking, voice, trip
service/     LocationForegroundService (orchestrateur central)
presentation/ splash, onboarding, permissions, home, settings, diagnostics, theme
```

## Prochaines étapes suggérées

1. Builder et corriger les éventuelles erreurs de compilation (normal, voir avertissement en tête de fichier)
2. Tester sur un vrai appareil (GPS, notifications, TTS, écran verrouillé)
3. Ajouter l'écran carte (Maps Compose) — dépendance déjà présente
4. Écrire les tests unitaires listés section 59 du cahier des charges
5. Préparer les assets Play Store
