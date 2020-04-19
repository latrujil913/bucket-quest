package com.example.android.bucket_quest.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.android.bucket_quest.Event
import com.example.android.bucket_quest.MyViewModel
import com.example.android.bucket_quest.R
import com.example.android.bucket_quest.activities.EventActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.event_row.view.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_my_list.*


class MyListFragment : Fragment() {

    private lateinit var adapter: MyEventsAdapter
    private lateinit var viewModel: MyViewModel
    private lateinit var database: DatabaseReference
    private val user = FirebaseAuth.getInstance().currentUser
    private val USER = "users"
    lateinit var userEventList: ArrayList<Event>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        database = FirebaseDatabase.getInstance().reference

        if (user != null) {
            // User is signed in, now get the users data
            Log.i("UserStatus", "User was found.")
            // This method is used to update user data
            //database.child("users").child(user.uid).child("googlePicture").setValue("It worked")
        } else {
            // No user is signed in
            Log.i("UserStatus", "User not logged in.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        addEvent.visibility = View.GONE


        viewModel = ViewModelProviders.of(requireActivity())[MyViewModel::class.java]

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val users = dataSnapshot.child(USER)
                    val currentUser =  FirebaseAuth.getInstance().currentUser

                    for (user in users.children){

                        if(currentUser?.uid == user.key){
                            if(dataSnapshot.child(USER).child(user.key as String).child("todoList").value == null){
                                adapter = MyEventsAdapter(context!!, ArrayList())
                                my_recycler_view.setItemViewCacheSize(20)
                                my_recycler_view.adapter = adapter
                                my_recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
                                adapter.replaceItems(ArrayList())
                                break
                            }
                            else{
                                userEventList = dataSnapshot.child(USER).child(user.key as String).child("todoList").value as ArrayList<Event>
                                adapter = MyEventsAdapter(context!!, userEventList)
                                my_recycler_view.setItemViewCacheSize(20)
                                my_recycler_view.adapter = adapter
                                my_recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
                                adapter.replaceItems(userEventList)
                                break
                            }

                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Failed to load data.", Toast.LENGTH_LONG).show()
                }
            }

        database.addListenerForSingleValueEvent(postListener)
    }

    // TODO: decouple this adapter into it's own file
    class MyEventsAdapter (private val context :Context, private val userEventList: ArrayList<Event>) : androidx.recyclerview.widget.RecyclerView.Adapter<MyEventsAdapter.ViewHolder>() {
        private var items : ArrayList<Event> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.event_row, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val hashMapItem = items[position] as HashMap<String, Any>

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference

            holder.nameTextView.text = hashMapItem["name"].toString()
            holder.locationTextView.text = hashMapItem["location"].toString()
            holder.upvotesTextView.text = hashMapItem["upvotes"].toString()
            holder.downvotesTextView.text = hashMapItem["downvotes"].toString()

            storageRef.child("event-pictures/${hashMapItem["picture"]}").downloadUrl.addOnSuccessListener {
                // Got the download URL for 'event-pictures/farmers.jpg"'
                Glide.with(context).load(it).into(holder.eventImageView)

            }.addOnFailureListener {
                // Handle any errors
                Log.i("evt_img","Failed to retrieve image.")
            }

            holder.itemView.setOnClickListener {
                Log.i("onClick", hashMapItem["name"].toString())
                val intent =  Intent(context, EventActivity::class.java)

                // 'intent.putExtra' packages up information within the intent to sent to the new activity
                intent.putExtra("event_key", createMyEvent(hashMapItem))
                /*  TODO: instead of sending just the information I might need to send the FB ref
                    intent.putExtra("event_name_key", item.name)
                 */
                context.startActivity(intent)
            }
        }

        fun replaceItems(items: ArrayList<Event>) {
            this.items = items
            notifyDataSetChanged()
        }

        private fun createMyEvent(hashMap: HashMap<String, Any>): Event{
            return Event(
                hashMap["name"] as String, hashMap["location"] as String,
                         hashMap["picture"] as String, hashMap["upvotes"] as Long,
                         hashMap["downvotes"] as Long, hashMap["description"] as String,
                         hashMap["comments"] as ArrayList<String>?, hashMap["city"] as String,
                         hashMap["state"] as String, hashMap["lat"] as Long,
                         hashMap["long"] as Long)
        }

        override fun getItemCount(): Int = items.size

        // NOTE: need to bind here first to be able to have access in onBindViewHolder
        inner class ViewHolder(override val containerView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView),
            LayoutContainer {
            val nameTextView: TextView = containerView.nameTextView
            val locationTextView: TextView = containerView.locationTextView
            val upvotesTextView: TextView = containerView.upvotesTextView
            val downvotesTextView: TextView = containerView.downvotesTextView
            val eventImageView : ImageView = containerView.card_view_image
        }
    }
}
