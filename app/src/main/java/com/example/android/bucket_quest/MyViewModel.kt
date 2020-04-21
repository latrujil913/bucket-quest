package com.example.android.bucket_quest

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class MyViewModel: ViewModel(){

    private lateinit var database: DatabaseReference
    private val USER = "users"

    fun handleBottomTab(selectedFragment: Fragment?, supportFragmentManager: FragmentManager): Boolean {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment!!).commit()
        return true
    }


    fun downvote(eventState: String, eventCity: String, eventName: String){

        database = FirebaseDatabase.getInstance().reference

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var downvotes = dataSnapshot.child("events").child(eventState).child(eventCity).child(eventName).child("downvotes").value as Long
                downvotes -= 1
                database.child("events").child(eventState).child(eventCity).child(eventName).child("downvotes").setValue(downvotes)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("getEventsNearLocation", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }

        database.addListenerForSingleValueEvent(postListener)

    }

    fun upvote(eventState: String, eventCity: String, eventName: String){

        database = FirebaseDatabase.getInstance().reference

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var upvotes = dataSnapshot.child("events").child(eventState).child(eventCity).child(eventName).child("upvotes").value as Long
                upvotes += 1
                database.child("events").child(eventState).child(eventCity).child(eventName).child("upvotes").setValue(upvotes)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("getEventsNearLocation", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }

        database.addListenerForSingleValueEvent(postListener)
    }


    fun addComment(eventState: String, eventCity: String, eventName: String, comment: String) {

        database = FirebaseDatabase.getInstance().reference

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child("events").child(eventState).child(eventCity).child(eventName).child("comments").value  == null){
                    val comments : ArrayList<String> = ArrayList()
                    comments.add(comment)
                    database.child("events").child(eventState).child(eventCity).child(eventName).child("comments").setValue(comments)
                }
                else{
                    val comments = dataSnapshot.child("events").child(eventState).child(eventCity).child(eventName).child("comments").value as ArrayList<String>
                    comments.add(comment)
                    database.child("events").child(eventState).child(eventCity).child(eventName).child("comments").setValue(comments)
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

    fun saveEvent(event: Event) {

        database = FirebaseDatabase.getInstance().reference

        val currentUser =  FirebaseAuth.getInstance().currentUser

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = dataSnapshot.child(USER)

                for (user in users.children){

                    if(currentUser?.uid == user.key){
                        if(dataSnapshot.child(USER).child(user.key as String).child("todoList").value == null){
                            val newEvents : ArrayList<Event> = ArrayList()
                            newEvents.add(event)
                            database.child(USER).child(user.key as String).child("todoList").setValue(newEvents)
                        }
                        else{
                            val newEvents = dataSnapshot.child(USER).child(user.key as String).child("todoList").value as ArrayList<Event>
                            newEvents.add(event)
                            database.child(USER).child(user.key as String).child("todoList").setValue(newEvents)
                        }

                    }
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

    fun removeEvent (event: Event) {

        database = FirebaseDatabase.getInstance().reference

        val currentUser =  FirebaseAuth.getInstance().currentUser

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = dataSnapshot.child(USER)

                for (user in users.children){
                    // if the user exists
                    if(currentUser?.uid == user.key){
                        if(dataSnapshot.child(USER).child(user.key as String).child("todoList").value != null){
                            val newEvents = dataSnapshot.child(USER).child(user.key as String).child("todoList").value as ArrayList<HashMap<String, Event>>
                            var index = 0
                            var found = 0
                            for (events in newEvents) {
                                if (event.name == events["name"].toString() && event.city == events.toString()) {
                                    Log.i("asdf", events["name"].toString())
                                    found = index
                                }
                                index += 1
                            }
                            newEvents.removeAt(found)
                            database.child(USER).child(user.key as String).child("todoList").setValue(newEvents)
                        }

                    }
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

}