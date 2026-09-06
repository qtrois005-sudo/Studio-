package com.geovoice.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Insert
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("SELECT * FROM trips ORDER BY startedAtMillis DESC")
    fun observeTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTrip(tripId: Long): TripEntity?

    @Insert
    suspend fun insertLocationPoint(point: LocationPointEntity)

    @Query("SELECT * FROM location_points WHERE tripId = :tripId ORDER BY timestampMillis ASC")
    suspend fun getLocationPoints(tripId: Long): List<LocationPointEntity>

    @Insert
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Query("SELECT * FROM announcements WHERE tripId = :tripId ORDER BY timestampMillis ASC")
    suspend fun getAnnouncements(tripId: Long): List<AnnouncementEntity>

    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()
}
