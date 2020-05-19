@file:Suppress("DEPRECATION")

package com.example.android.bucket_quest.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.example.android.bucket_quest.Event
import com.example.android.bucket_quest.MyViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_new.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AddNewActivity : AppCompatActivity() {

    private lateinit var viewModel: MyViewModel
    private var galleryPermissionGranted: Boolean by Delegates.notNull()
    private val GALLERY_REQUEST_CODE = 1
    private lateinit var database: DatabaseReference
    private var uri : Uri? = null
    private val firePath = UUID.randomUUID().toString()
    private lateinit var progressDialog : ProgressDialog


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.android.bucket_quest.R.layout.activity_add_new)

        viewModel = ViewModelProviders.of(this)[MyViewModel::class.java]
        button_event_add.isEnabled = false
        progressDialog = ProgressDialog(this)



        button_upload.setOnClickListener {
            getReadPermissions(this)
            getImage()
            button_event_add.isEnabled = true
        }

        button_event_add.setOnClickListener {
            getEventInfo()
            uploadImage()
            // TODO: exit activity, calling finish() crashes app
            finish()
        }

    }

    private fun uploadImage() {
        val storageReference = FirebaseStorage.getInstance().reference

        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val ref = storageReference.child("event-pictures/${firePath}")
        ref.putFile(uri as Uri)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                    .totalByteCount
                progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
            }
    }

    private fun getEventInfo() {
        val list = ArrayList<String>()
        database = FirebaseDatabase.getInstance().reference

        // get editText values
        val newEventName = edittext_event_name.text.toString()
        val newEventCity = edittext_event_city.text.toString()
        val newEventState = edittext_event_state.text.toString()

        // define the new event to be stored in Firebase
        val event = Event(
            edittext_event_name.text.toString(),
            edittext_event_location.text.toString(),
            firePath,
            0,
            0,
            edittext_event_description.text.toString(),
            list,
            edittext_event_city.text.toString(),
            edittext_event_state.text.toString(),
            0.0,
            0.0
        )

        database
            .child("events")
            .child(newEventState)
            .child(newEventCity)
            .child(newEventName)
            .setValue(event)
            .addOnSuccessListener {
                // Write was successful!
                Log.i("upld", "Event info successfully updated.")
            }
            .addOnFailureListener {
                // Write failed
                Toast.makeText(this, "Event failed to update", Toast.LENGTH_LONG).show()
            }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun getImage(){
        // allow the user select an image
        if (galleryPermissionGranted){
            Log.i("grntd","Permission was granted")
            pickFromGallery()
        }
        else{
            Log.i("grntd","Permission needed to upload images")
        }
    }

    private fun getReadPermissions(context: Context) {

         // Request permission to read external storage
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            galleryPermissionGranted = true
        }
        else
        {
            galleryPermissionGranted = false
            Toast.makeText(this, "Permission needed to read files.", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(context as Activity,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun pickFromGallery() {
        //Create an Intent with action as ACTION_PICK
        val intent = Intent(Intent.ACTION_PICK)
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        val mimeTypes = arrayOf("image/jpeg", "image/png")

        // Sets the type as image/*. This ensures only components of type image are selected
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        // Launching the Intent, GALLERY_REQUEST_CODE is used to identify the returning event
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    //data.getData returns the content URI for the selected Image
                    uri = data!!.data
                    imageview_preview.setImageURI(uri)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissProgressDialog()
    }

    private fun dismissProgressDialog() {
        if (progressDialog.isShowing){
            progressDialog.dismiss()
        }
    }
}