package com.dev.abhinav.mapsapplication.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class LocationEntity(
    @PrimaryKey var id: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "address") var address: String,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double
)
