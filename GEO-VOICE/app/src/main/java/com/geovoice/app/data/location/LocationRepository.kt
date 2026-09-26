/** Demande activement une position fraîche, sans dépendre d'un cache déjà rempli. */
    @SuppressLint("MissingPermission")
    suspend fun currentPosition(): GeoPosition? {
        return try {
            val cancellationSource = com.google.android.gms.tasks.CancellationTokenSource()
            val location = com.google.android.gms.tasks.Tasks.await(
                fusedClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationSource.token
                )
            )
            location?.let {
                GeoPosition(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracyMeters = it.accuracy,
                    speedMetersPerSecond = if (it.hasSpeed()) it.speed else null,
                    bearingDegrees = if (it.hasBearing()) it.bearing else null,
                    timestampMillis = it.time
                )
            }
        } catch (e: Exception) {
            null
        }
    }
