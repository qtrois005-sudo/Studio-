# SleepAudio Premium

Application Android moderne de relaxation et de sommeil, construite avec **Kotlin + Jetpack Compose + Material 3 + Media3**.

## Fonctionnalités
- Lecture de fichiers audio locaux
- Lecteur avec lecture/pause/arrêt
- Minuterie de sommeil et arrêt automatique
- Fondu progressif
- Programme de sommeil
- Bibliothèque personnelle / favoris
- Réglages de l'expérience
- Interface mobile premium avec navigation inférieure
- Persistance des préférences avec DataStore

## Structure propre
Les ressources Android sont séparées correctement dans `res/drawable`, `res/values` et `res/mipmap-*`. Les anciens dossiers créés accidentellement avec des accolades ont été supprimés.

## GitHub
Le dossier du ZIP est directement prêt à être initialisé :

```bash
git init
git add .
git commit -m "Initial premium release"
```

Ouvrez ensuite le projet dans Android Studio et synchronisez Gradle avant de compiler.
