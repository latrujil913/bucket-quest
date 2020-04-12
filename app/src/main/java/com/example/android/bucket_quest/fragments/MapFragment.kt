package com.example.android.bucket_quest.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.android.bucket_quest.MyViewModel
import com.example.android.bucket_quest.R
import com.example.android.bucket_quest.activities.AddNewActivity
import com.example.android.bucket_quest.activities.EventActivity
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.util.*
import kotlin.properties.Delegates


class MapFragment : Fragment(), OnMapReadyCallback{

    private lateinit var googleMap: GoogleMap
    private lateinit var viewModel: MyViewModel
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var location: Location
    private val DEFAULT_ZOOM = 15f
    private lateinit var database: DatabaseReference
    private var mLocationPermissionGranted: Boolean by Delegates.notNull()
    var API_KEY : String = System.getenv("MY_API_KEY") ?: "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity())[MyViewModel::class.java]
        database = FirebaseDatabase.getInstance().reference
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
        Log.i("env_var", API_KEY)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_map, container, false)

        if (!Places.isInitialized()) {
            Log.i("env_var", API_KEY)
            Places.initialize(context!!, API_KEY)
        }

        initializeMap(savedInstanceState)
        initalizeAutoComplete(v)
        getLocationPermission()
        getDeviceLocation()

        v.findViewById<FloatingActionButton>(R.id.addEvent).setOnClickListener { addActivity() }
        v.findViewById<FloatingActionButton>(R.id.findLocation).setOnClickListener { findLocation() }

        return v
    }

    private fun findLocation(){
        getDeviceLocation()
    }

    private fun addActivity() {
        val intent = Intent(context, AddNewActivity::class.java)
        startActivity(intent)
    }

    private fun getEventsNearLocation() {

        Thread.sleep(1000)
        //val curScreen = googleMap.getProjection().getVisibleRegion().latLngBounds

        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                // TODO: addresses[0].locality not getting city name for me in SLO, find a way to get it
                if (addresses[0].locality != null){
                    val post = dataSnapshot.child("events").child(addresses[0].adminArea).child(
                        addresses[0].locality)

                    for (events in post.children){
                        val latLng = LatLng(events.child("lat").value.toString().toDouble(), events.child("long").value.toString().toDouble())
                        googleMap.addMarker(MarkerOptions().position(latLng).title(events.key + " " + events.child("description").key)).setIcon(
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                        googleMap.setOnInfoWindowClickListener {
                            val intent =  Intent(context, EventActivity::class.java)
                            intent.putExtra("event_name_key", events.key)
                            intent.putExtra("event_location_key", "test")
                            intent.putExtra("event_upvotes_key", events.child("upvotes").key)
                            intent.putExtra("event_downvotes_key", events.child("downvotes").key)
                            intent.putExtra("event_picture_key", events.child("picture").key)

                            startActivity(intent)
                        }
                    }
                    Log.d("EventsCheck", post.toString())
                }
                else{
                    Toast.makeText(context, "Couldn't find location", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("getEventsNearLocation", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }

        database.addListenerForSingleValueEvent(postListener)
    }

    private fun getLocationPermission() {

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
         if (ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true
         }
         else
         {
             mLocationPermissionGranted = false
             ActivityCompat.requestPermissions(context as Activity,
                 arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
         }
    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(context as Activity
                ) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        if(task.result != null){
                            location = task.result!!
                            Log.d("LocationLat", location.latitude.toString())
                            Log.d("LocationLong", location.longitude.toString())

                            if(mLocationPermissionGranted){
                                getEventsNearLocation()
                            }

                            //User location pin
                            googleMap.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("Your Location")).setIcon(
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                            )

                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        location.latitude,
                                        location.longitude
                                    ), DEFAULT_ZOOM
                                ))
                        }
                    } else {
                        Log.d("googlePlaces", "Current location is null. Using defaults.")
                        Log.e("googlePlaces", "Exception: %s", task.exception)

                        googleMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

    override fun onMapReady(map: GoogleMap) {
        MapsInitializer.initialize(context)
        googleMap = map
    }

    private fun initializeMap(savedInstanceState: Bundle?) {

        val mapView = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    private fun initalizeAutoComplete(v: View) {

        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.VIEWPORT))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.latLng, 17f)
                googleMap.moveCamera(cameraUpdate)
                Log.i("TESTING", place.name)
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("PLACES", "An error occurred: $status")
            }
        })
    }
}
