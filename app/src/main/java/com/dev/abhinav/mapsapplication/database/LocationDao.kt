package com.dev.abhinav.mapsapplication.database

import androidx.room.*

@Dao
interface LocationDao {
    @Query("SELECT * FROM favorites")
    fun getAll(): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: LocationEntity)

    @Delete
    fun delete(location: LocationEntity)
}