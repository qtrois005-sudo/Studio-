# Sleep Audio — App Android de lecture avec arrêt programmé

Application native Android (Kotlin + Jetpack Compose + Media3/ExoPlayer)
qui lit un fichier audio local et l'arrête automatiquement selon une durée
ou une heure précise, avec fondu de volume optionnel, en continuant à
fonctionner en arrière-plan et écran verrouillé.

## Arborescence

```
SleepAudio/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/sleepaudio/app/
        │   │   ├── SleepAudioApp.kt
        │   │   ├── MainActivity.kt
        │   │   ├── MainViewModel.kt
        │   │   ├── data/        (AudioFile, SleepSchedule, PreferencesRepository)
        │   │   ├── player/      (PlaybackService, AudioController, PlayerState)
        │   │   ├── timer/       (SleepTimerManager, SleepScheduler)
        │   │   ├── ui/          (écrans Compose, thème, composants)
        │   │   └── util/        (TimeUtils, AudioUtils)
        │   └── res/values/      (strings.xml, themes.xml)
        └── test/java/com/sleepaudio/app/  (tests unitaires TimeUtils, SleepTimerManager)
```

## Compiler le projet

1. Ouvrir le dossier `SleepAudio/` dans **Android Studio** (version récente,
   Koala ou supérieure recommandée).
2. Android Studio détecte l'absence du jar du wrapper Gradle et propose de
   le régénérer automatiquement (ou lancer *File → Sync Project with Gradle
   Files*). `gradle-wrapper.properties` pointe déjà vers Gradle 8.7.
3. Laisser Android Studio télécharger les dépendances (AndroidX, Media3,
   Compose BOM 2024.06.00).
4. Build → Make Project.

En ligne de commande, une fois le wrapper généré :
```
./gradlew assembleDebug
```

## Installer sur un appareil réel

```
./gradlew installDebug
```
ou directement depuis Android Studio via *Run ▶* avec l'appareil (USB,
débogage USB activé) ou un émulateur sélectionné.

## Générer un APK / AAB signé (release)

1. *Build → Generate Signed Bundle / APK*.
2. Choisir *Android App Bundle* (recommandé pour le Play Store) ou *APK*.
3. Créer ou sélectionner un keystore de signature (non fourni ici pour des
   raisons de sécurité — à générer localement).
4. Le build `release` a déjà `isMinifyEnabled = true` avec les règles
   Proguard nécessaires pour Media3.

## Checklist de validation manuelle

- [ ] Installation et premier lancement
- [ ] Sélection d'un fichier audio (SAF)
- [ ] Lecture démarre réellement (son audible)
- [ ] Pause / reprise
- [ ] Arrêt manuel
- [ ] Minuterie par durée
- [ ] Minuterie par heure précise (avant et après minuit)
- [ ] Fondu de volume avant arrêt
- [ ] Écran verrouillé pendant la lecture
- [ ] Notification média synchronisée avec l'état réel
- [ ] Écouteurs filaires / Bluetooth (perte d'audio focus)
- [ ] Fichier supprimé ou permission perdue après sélection
- [ ] Redémarrage de l'app / du téléphone
- [ ] Mode sombre
- [ ] Petit écran / grand écran
- [ ] Accessibilité (TalkBack, tailles tactiles)

## Ce qui est prêt pour la V1 vs prévu pour plus tard

**Implémenté :** sélection audio via SAF, lecture Media3/ExoPlayer,
lecture en arrière-plan via `MediaSessionService`, notification média,
minuterie par durée et par heure (gestion du passage de minuit testée),
fondu de volume configurable, persistance DataStore (dernier audio,
dernière durée), thème sombre premium, tests unitaires sur les calculs
temporels et la minuterie.

**Architecturé mais pas encore dans l'UI (pour ne pas surcharger la V1) :**
programmation récurrente avec heure de début/fin (`SleepScheduler`,
`ScheduleWindow`) — l'écran `ScheduleScreen` est un point d'entrée prêt à
être complété sans réécrire le reste de l'app. De même pour favoris,
historique, playlists, minuteries multiples, widgets, statistiques.

## Icône de l'application

Aucune icône n'est fournie (le cahier des charges interdit tout contenu
propriétaire codé en dur). Ajouter `ic_launcher.png` / `ic_launcher.xml`
via *Image Asset Studio* dans Android Studio (clic droit sur `res` →
*New → Image Asset*).
