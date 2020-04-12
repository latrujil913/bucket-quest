package com.example.android.bucket_quest.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.android.bucket_quest.Event
import com.example.android.bucket_quest.R
import com.example.android.bucket_quest.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : androidx.fragment.app.Fragment() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var database: DatabaseReference


    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureGoogleSignIn()
        auth = FirebaseAuth.getInstance()
    }


    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        database = FirebaseDatabase.getInstance().reference

        if (currentUser != null) {
            Log.w(TAG, "Google sign in success!")
            val templst: MutableList<Event?> = getDefaultSaved()

            val fireUser = User(currentUser.displayName, currentUser.uid, templst, "")

            // Create a new user
            database.child("users").child(currentUser.uid).setValue(fireUser)

            userName.text = "Hi, ${currentUser.displayName}!"
        }
        else {
            Log.i("auth", "No user.")
        }
    }

    private fun getDefaultSaved() : MutableList<Event?>{
        val list = ArrayList<String>()

        return mutableListOf(
            Event("Cal Poly vs UCSB", "Stadium", "f4dbde2a-96e7-4230-8691-8330e0a6451b", 0, 0, "desc...", list, "San Luis Obispo", "California"),
            Event("Career Fair", "Madona", "7b650a0c-c5db-44c5-94cf-747cac5359ae", 0, 0, "desc...", list, "San Luis Obispo", "California"),
            Event("Bishops peak hike for cancer", "Bishops", "7e681b0b-ebb2-40ee-b7d1-79f0b0742bdb", 0, 0, "Hike for a cause",list, "San Luis Obispo", "California")
            )
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(activity!!, mGoogleSignInOptions)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater
            .inflate(R.layout.fragment_settings, container, false)

        view
            .findViewById<SignInButton>(R.id.signInButton)
            .setOnClickListener {
                signIn()
            }
        view
            .findViewById<Button>(R.id.signout)
            .setOnClickListener {
                signOut()
                Toast.makeText(activity, "Logout success.", Toast.LENGTH_LONG).show()
            }

        return view
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        mGoogleSignInClient.signOut()
        userName.text = ""
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(activity,"Login failed: oAR.", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i("auth","Login Success.")
            } else {
                Log.i("auth", "Authentication Failed.")
            }
        }
    }


    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

}


