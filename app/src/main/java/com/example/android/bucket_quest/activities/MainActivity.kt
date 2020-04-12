package com.example.android.bucket_quest.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.android.bucket_quest.MyViewModel
import com.example.android.bucket_quest.R
import com.example.android.bucket_quest.fragments.ListFragment
import com.example.android.bucket_quest.fragments.MapFragment
import com.example.android.bucket_quest.fragments.MyListFragment
import com.example.android.bucket_quest.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this)[MyViewModel::class.java]

        bottom_navigation.setOnNavigationItemSelectedListener(navListener)
        bottom_navigation.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                MyListFragment()
            ).commit()
        }
    }

    private val navListener = OnNavigationItemSelectedListener { item ->

        var selectedFragment: Fragment? = null

        when (item.itemId) {
            R.id.nav_my_list -> selectedFragment =
                MyListFragment()
            R.id.nav_list -> selectedFragment =
                ListFragment()
            R.id.nav_map -> selectedFragment =
                MapFragment()
            R.id.nav_settings -> selectedFragment =
                SettingsFragment()
        }

        bottom_navigation.menu.getItem(2).isEnabled = selectedFragment !is MapFragment

        viewModel.handleBottomTab(selectedFragment, supportFragmentManager)
    }
}
