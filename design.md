# Design mobile — SleepAudio

## Direction produit

SleepAudio est un lecteur audio local conçu pour accompagner l’endormissement. L’interface doit évoquer le calme, la confiance et la maîtrise sans ressembler à une application musicale chargée. Le design suit les conventions iOS/Android modernes, en portrait 9:16, avec des contrôles accessibles au pouce et des actions principales placées dans la moitié basse de l’écran.

## Écrans

| Écran | Contenu principal | Fonctionnalités |
|---|---|---|
| Accueil | Audio actuel, état de lecture, durée du minuteur, carte de sélection | Choisir un fichier, lire/pause, ouvrir le minuteur, accéder aux réglages de fondu |
| Minuteur | Sélecteur de mode, durées rapides, heure de fin, résumé de l’échéance | Choisir une durée ou une heure précise, confirmer, annuler le minuteur |
| Programmation | Heure de début et heure de fin, statut de programmation | Préparer une fenêtre de lecture future et gérer le passage de minuit |
| Lecture | Titre, visuel calme, progression, commandes, temps restant | Lecture/pause, arrêt, déplacement dans la piste, modification du minuteur |
| Réglages | Fondu de fin, dernières préférences, informations de confidentialité | Activer le fondu, choisir sa durée, réinitialiser les préférences locales |

## Hiérarchie visuelle

L’accueil commence par un en-tête discret avec le nom SleepAudio et un sous-titre orienté action. Une grande carte centrale représente l’audio sélectionné avec une icône de fichier sonore, son nom lisible et sa durée. En l’absence de fichier, la carte explique clairement l’action à effectuer et propose un bouton principal « Choisir un audio ».

Sous la carte, un panneau « Arrêt automatique » affiche soit « Aucun minuteur », soit le compte à rebours calculé à partir d’une échéance réelle. Les raccourcis de durée sont présentés dans une rangée de boutons tactiles. Un bouton de lecture circulaire et contrasté reste proche du bas de l’écran afin d’être utilisable d’une seule main.

## Parcours clés

1. L’utilisateur ouvre Accueil, appuie sur « Choisir un audio », sélectionne un document via le sélecteur système, puis revient avec le nom et la durée du fichier.
2. L’utilisateur ouvre « Arrêt automatique », choisit 15, 30, 45 ou 60 minutes, ou saisit une durée personnalisée, puis confirme. L’échéance est enregistrée localement.
3. L’utilisateur choisit « Heure de fin », sélectionne une heure, puis confirme. Si l’heure est déjà passée, elle est interprétée comme le lendemain afin d’éviter toute durée négative.
4. L’utilisateur démarre la lecture. Il peut quitter l’écran, verrouiller le téléphone et retrouver le même état à son retour. Lorsque l’échéance est atteinte, le volume diminue éventuellement puis la lecture s’arrête.
5. Depuis Lecture, l’utilisateur peut mettre en pause, reprendre, arrêter, déplacer la progression ou modifier le minuteur sans perdre l’audio sélectionné.

## Couleurs et typographie

La palette de marque est nocturne mais chaleureuse : fond principal `#0B1020` bleu nuit, surface `#151D33`, surface secondaire `#202A45`, texte principal `#F7F8FC`, texte secondaire `#A8B2CB`, accent lavande `#A78BFA`, accent secondaire bleu brume `#7DD3FC`, succès `#86EFAC`, erreur `#FDA4AF`.

L’accent lavande est réservé aux actions importantes et à la progression. Les textes utilisent une hiérarchie proche des styles système : titre d’écran 28–32 px, titre de carte 18–20 px, texte courant 15–16 px, légendes 12–13 px. Les zones tactiles principales mesurent au moins 44 px de haut.

## Comportements et accessibilité

Les boutons utilisent un retour de pression léger et un retour haptique seulement pour les actions importantes. Les états de chargement, d’erreur et d’absence d’accès au fichier sont toujours explicites. Les contrastes restent lisibles en mode sombre et la navigation ne dépend jamais d’un geste caché. Les contrôles média Android constituent une extension de l’écran Lecture, jamais une source d’état différente.
