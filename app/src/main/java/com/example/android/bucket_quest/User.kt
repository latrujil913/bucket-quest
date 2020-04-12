package com.example.android.bucket_quest


class User(
    val name: String?,
    val userId: String,
    // TODO: instead make this a reference to the "events" db!
    val todoList: MutableList<Event?>,
    val googlePicture: String
)