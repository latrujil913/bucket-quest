package com.example.android.bucket_quest

import android.util.Log


class User(
    val name: String?,
    val userId: String,
    val todoList: MutableList<Event?>,
    val googlePicture: String
) {
    fun containsEvent(todoList: HashMap<String, Any>): Boolean {
        Log.i("asdf","Hello")

        return false
    }
}