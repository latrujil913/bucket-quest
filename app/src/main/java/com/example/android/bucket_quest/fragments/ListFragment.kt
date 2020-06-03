package com.example.android.bucket_quest.fragments

import android.widget.Toast
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.android.bucket_quest.Event
import com.example.android.bucket_quest.MyViewModel
import com.example.android.bucket_quest.R
import com.example.android.bucket_quest.activities.EventActivity
import com.example.android.bucket_quest.activities.AddNewActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.event_row.view.*
import kotlinx.android.synthetic.main.fragment_list.*
import java.util.*
import kotlin.properties.Delegates


class ListFragment : Fragment() {

    private lateinit var adapter: EventsAdapter
    private lateinit var viewModel: MyViewModel
    private lateinit var database: DatabaseReference
    private var location: Location? = null
    private var mLocationPermissionGranted: Boolean by Delegates.notNull()
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        database = FirebaseDatabase.getInstance().reference
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)


        if (user != null) {
            // User is signed in, now get the users data
            Log.i("UserStatus", "User was found.")
        } else {
            // No user is signed in
            Log.i("UserStatus", "User not logged in.")
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v =  inflater.inflate(R.layout.fragment_list, container, false) as RelativeLayout

//        val floatingActionButton = context?.let { FloatingActionButton(it) }
//        if (floatingActionButton != null) {
//            floatingActionButton.id = R.id.addEvent
//        }
//
//        val t = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
//        t.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
//        t.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
//        t.bottomMargin = 50
//        t.rightMargin = 50
//        if (floatingActionButton != null) {
//            floatingActionButton.layoutParams = t
//            floatingActionButton.setImageResource(R.drawable.ic_add_black_24dp)
//
//        }

        getLocationPermission()
        getDeviceLocation()

//        v.addView(floatingActionButton)
        v.findViewById<FloatingActionButton>(R.id.addEvent).setOnClickListener { addActivity() }
        return v
    }

    private fun addActivity() {
        val intent = Intent(context, AddNewActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity())[MyViewModel::class.java]
        adapter = EventsAdapter(context!!)
        my_recycler_view.setItemViewCacheSize(20)


        val geocoder = Geocoder(context, Locale.getDefault())
        var addresses : MutableList<Address>? = null


        FirebaseDatabase.getInstance().reference.child("events")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val allEvents: MutableList<Event?>
                    if (location != null){
                        addresses = geocoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
                        allEvents  = getData(dataSnapshot, addresses)
                    }
                    else{
                        allEvents = getData(dataSnapshot, addresses)
                    }

                    adapter.replaceItems(allEvents)
                    if (my_recycler_view != null) {
                        my_recycler_view.adapter = adapter
                        my_recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Failed to load data.", Toast.LENGTH_LONG).show()
                }
            })
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
                            Log.d("LocationLat", location!!.latitude.toString())
                            Log.d("LocationLong", location!!.longitude.toString())

                        }
                    } else {
                        Log.d("googlePlaces", "Current location is null. Using defaults.")
                        Log.e("googlePlaces", "Exception: %s", task.exception)

                    }
                }
            }
        }
        catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }



    private fun getData(dataSnapshot: DataSnapshot, addresses: MutableList<Address>?) : MutableList<Event?>{
        val eventList : MutableList<Event?> = mutableListOf()
        lateinit var event : Event

        if (addresses?.get(0)?.locality != null && mLocationPermissionGranted){
            for (states in dataSnapshot.children) {
                for (cities in states.children) {
                    if (cities.key == addresses[0].locality){
                        for (events in cities.children){
                            event  = events.getValue(Event::class.java)!!
                            eventList.add(event)
                        }
                    }
                }
            }
        }
        else{
            for (states in dataSnapshot.children) {
                for (cities in states.children) {
                    for (events in cities.children){
                        event  = events.getValue(Event::class.java)!!
                        eventList.add(event)
                    }
                }
            }
        }

        return eventList
    }


    class EventsAdapter (private val context : Context) : androidx.recyclerview.widget.RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
        private var items = mutableListOf<Event?>()
        private val storage = FirebaseStorage.getInstance()
        private val storageRef = storage.reference


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.event_row, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]

            if (item != null) {
                holder.nameTextView.text = item.name
                holder.locationTextView.text = item.location
                holder.upvotesTextView.text = item.upvotes.toString()
                holder.downvotesTextView.text = item.downvotes.toString()

                storageRef.child("event-pictures/${item.picture}").downloadUrl.addOnSuccessListener {
                    // Got the download URL for 'event-pictures/farmers.jpg"'
                    Glide.with(context).load(it).into(holder.eventImageView)

                }.addOnFailureListener {
                    // Handle any errors
                    Log.i("evt_img","Failed to retrieve image.")
                }
            }

            holder.itemView.setOnClickListener {
                Log.i("onClick", item?.name)
                val intent =  Intent(context, EventActivity::class.java)

                // 'intent.putExtra' packages up information within the intent to sent to the new activity
                if (item != null) {
                    intent.putExtra("event_key", item)
                }

                context.startActivity(intent)
            }
        }

        fun replaceItems(items: MutableList<Event?>) {
            this.items = items
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size


        inner class ViewHolder(override val containerView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView), LayoutContainer {
            val nameTextView: TextView = containerView.nameTextView
            val locationTextView: TextView = containerView.locationTextView
            val upvotesTextView: TextView = containerView.upvotesTextView
            val downvotesTextView: TextView = containerView.downvotesTextView
            val eventImageView : ImageView = containerView.card_view_image
        }
    }
}

