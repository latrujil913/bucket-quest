package com.example.android.bucket_quest.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.android.bucket_quest.Event
import com.example.android.bucket_quest.MyViewModel
import com.example.android.bucket_quest.R
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_event.*
import kotlinx.android.synthetic.main.row_comment.view.*


class EventActivity: AppCompatActivity() {

    private lateinit var event: Event
    lateinit var viewModel: MyViewModel
    private lateinit var adapter: MyCommentsAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        viewModel = ViewModelProviders.of(this)[MyViewModel::class.java]
        database = FirebaseDatabase.getInstance().reference

        setupUI()
        getComments()
    }

    private fun getComments() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post = dataSnapshot.child("events").child(event.state!!).child(event.city!!).child(
                    event.name
                )
                var list: ArrayList<String> = ArrayList()
                for (events in post.children){
                    if(events.key == "comments"){
                        list = events.value!! as ArrayList<String>
                    }
                    if(events.key == "name" && events.value!! == event.name){
                        adapter.replaceItems(list)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, logging message
                Log.w("getEventsNearLocation", "loadPost:onCancelled", databaseError.toException())
            }
        }
        database.addListenerForSingleValueEvent(postListener)
    }

    private fun setupUI(){

        event = intent.extras.getSerializable("event_key") as Event

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageView = findViewById<ImageView>(R.id.imageview_event)

        storageRef.child("event-pictures/${event.picture}").downloadUrl.addOnSuccessListener {
            // Got the download URL for 'event-pictures/farmers.jpg"'
            Glide.with(this).load(it).into(imageView)

        }.addOnFailureListener {
            // Handle any errors
            Log.i("evt_img","Failed to retrieve image.")
        }

        textview_event_name.text = event.name
        textview_location.text = event.location
        textview_upvotes.text = event.upvotes.toString()
        textview_downvotes.text = event.downvotes.toString()

        adapter = MyCommentsAdapter()
        commentsRecyclerView.adapter = adapter
        commentsRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        buttonAddComment.setOnClickListener { viewModel.addComment(event.state!!, event.city!!, event.name!!, editTextComment.text.toString()) }
        buttonUpvote.setOnClickListener { upvote() }
        buttonDownvote.setOnClickListener { downvote() }
//        buttonSaveEvent.setOnClickListener{ saveEvent() }
        bookmark_toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                saveEvent()
            } else {
                // TODO: The toggle is disabled, add functionality to remove an event from the Mylist
                 removeEvent()
            }
        }
    }

    private fun downvote() {
        viewModel.downvote(event.state!!, event.city!!, event.name)
        textview_downvotes.text = (textview_downvotes.text.toString().toInt() - 1).toString()
        buttonDownvote.alpha = 0.25f
        buttonDownvote.isClickable = false
        Toast.makeText(this, "Downvote Added!", Toast.LENGTH_LONG).show()
    }

    private fun upvote() {
        viewModel.upvote(event.state!!, event.city!!, event.name)
        textview_upvotes.text = (textview_upvotes.text.toString().toInt() + 1).toString()
        buttonUpvote.alpha = .25f
        buttonUpvote.isClickable = false
        Toast.makeText(this, "Upvote Added!", Toast.LENGTH_LONG).show()
    }

    private fun saveEvent() {
        viewModel.saveEvent(event)
        Toast.makeText(this, "Event Saved!", Toast.LENGTH_LONG).show()

//        finish()
    }

    private fun removeEvent(){
        viewModel.removeEvent(event)
        Toast.makeText(this, "Event Removed!", Toast.LENGTH_LONG).show()

    }

    class MyCommentsAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<MyCommentsAdapter.ViewHolder>() {

        private var items : MutableList<String?> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_comment, parent, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]

            if (item != null) {
                holder.commentTextView.text = item
            }
        }

        fun replaceItems(items : ArrayList<String>) {

            this.items = items as MutableList<String?>

            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        // NOTE: need to bind here first to be able to have access in onBindViewHolder
        inner class ViewHolder(override val containerView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView),
            LayoutContainer {
            val commentTextView: TextView = containerView.commentTextView
        }
    }
}