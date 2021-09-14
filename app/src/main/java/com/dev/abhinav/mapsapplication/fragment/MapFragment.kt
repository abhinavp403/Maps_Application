package com.dev.abhinav.mapsapplication.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dev.abhinav.mapsapplication.R
import com.dev.abhinav.mapsapplication.database.LocationDatabase
import com.dev.abhinav.mapsapplication.database.LocationEntity
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

// First Fragment
class MapFragment : Fragment() {

    private lateinit var mMap: GoogleMap
    private lateinit var gpsIcon: ImageView
    private lateinit var placesClient: PlacesClient

    private val TAG: String = "MapFragment"
    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private val DEFAULT_ZOOM = 15f
    private var mLocationPermissionsGranted = false
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var db: LocationDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        gpsIcon = view.findViewById(R.id.gps_icon)
        db = LocationDatabase.invoke(activity?.applicationContext!!)

        // to get location permission from device
        getLocationPermission()

        return view
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        Log.d(TAG, "onMapReady: map is ready")
        mMap = googleMap

        if (mLocationPermissionsGranted) {
            getDeviceLocation()
            if (ActivityCompat.checkSelfPermission(activity?.applicationContext!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity?.applicationContext!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return@OnMapReadyCallback
            }
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false
            init()
        }
    }

    private fun init() {
        Places.initialize(activity?.applicationContext!!, getString(R.string.google_maps_key))
        placesClient = Places.createClient(activity?.applicationContext!!)

        initMarkers()

        // Initialize the AutocompleteSupportFragment
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.address}, ${place.latLng}")
                val latLng: LatLng? = place.latLng
                mMap.addMarker(MarkerOptions().position(latLng!!).title(place.name))
                place.name?.let { moveCamera(latLng, DEFAULT_ZOOM, it) }

                // Creates dialog box when user clicks on marker
                mMap.setOnMarkerClickListener {
                    val builder = AlertDialog.Builder(activity)
                    builder.setMessage("Add to Favorites?")
                    builder.setPositiveButton(android.R.string.yes) { _, _ ->
                        // If OK, then marker is added to database
                        doAsync {
                            db.locationDao().insert(LocationEntity(place.id!!, place.name!!, place.address!!, place.latLng!!.latitude, place.latLng!!.longitude))
                        }
                        Log.d(TAG, "Added to Favorite")
                    }
                    builder.show()
                    false
                }
            }
            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })

        gpsIcon.setOnClickListener {
            Log.d(TAG, "onClick: clicked gps icon")
            getDeviceLocation()
        }
    }

    // Initialize map fragment with fragment manager
    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)
    }

    // Adds markers to those locations saved in the database
    private fun initMarkers() {
        Log.d(TAG, "initMarkers")
        doAsync {
            db = LocationDatabase.invoke(activity?.applicationContext!!)
            val data = db.locationDao().getAll()
            uiThread {
                data.forEach {
                    mMap.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)).title(it.name))
                }
            }
        }
    }

    // Resets location on map to current device location
    private fun getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation")
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity?.applicationContext!!)
        try {
            if (mLocationPermissionsGranted) {
                val location: Task<Location> = mFusedLocationProviderClient.lastLocation
                location.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "onComplete: found location!")
                        val currentLocation: Location = task.result as Location
                        moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude), DEFAULT_ZOOM, "My Location")
                    } else {
                        Log.d(TAG, "onComplete: current location is null")
                        Toast.makeText(activity?.applicationContext!!, "unable to get current location", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.message)
        }
    }

    // Gets location permission from user for this app
    private fun getLocationPermission() {
        Log.d(TAG, "getLocationPermission")
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (ContextCompat.checkSelfPermission(activity?.applicationContext!!, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(activity?.applicationContext!!, COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true
                initMap()
            } else {
                ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    // Focuses camera on marker
    private fun moveCamera(latLng: LatLng, zoom: Float, title: String) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        if (title != "My Location") {
            val options = MarkerOptions()
                .position(latLng)
                .title(title)
            mMap.addMarker(options)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mLocationPermissionsGranted = false
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    var i = 0
                    while (i < grantResults.size) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false
                            Log.d(TAG, "onRequestPermissionsResult: permission failed")
                            return
                        }
                        i++
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    mLocationPermissionsGranted = true
                    initMap()
                }
            }
        }
    }

    // Updates markers whenever Maps Tab is clicked
    override fun onResume() {
        super.onResume()
        initMarkers()
    }
}