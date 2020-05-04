package com.example.android.bucket_quest

import java.io.Serializable


class Event (
    val name: String = "",
    val location: String? = null,
    val picture: String? = null, // TODO: change to Bitmap
    var upvotes: Long = 0,
    var downvotes: Long = 0,
    var description: String? = null,
    val comments: ArrayList<String>? = ArrayList(),
    var city :  String? = null,
    var state : String? = null,
    var lat : Double = 0.0,
    var long : Double = 0.0
): Serializable {
    fun addUpvote(){
        this.upvotes += 1
    }

    fun addDownvote(){
        this.downvotes -= 1
    }
}