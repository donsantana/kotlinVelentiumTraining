package com.velentium.android.platformv.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.velentium.android.platformv.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_home.*

class HomeActivity : AppCompatActivity() {

  lateinit var navigationView: NavigationView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)
    setSupportActionBar(homeToolbar)

    navigationView = findViewById(R.id.sideNavigation)
    val toggle = ActionBarDrawerToggle(this,drawer_layout,homeToolbar,R.string.open, R.string.close)
    toggle.isDrawerIndicatorEnabled = true
    drawer_layout.addDrawerListener(toggle)
    toggle.syncState()

    navigationView.setNavigationItemSelectedListener {
      drawer_layout.closeDrawer(GravityCompat.START)
      when(it.itemId){
        R.id.devices_menu->startActivity(Intent(this, MainActivity::class.java))
        R.id.game_menu->startActivity(Intent(this, GameActivity::class.java))
        R.id.flag_menu->startActivity(Intent(this, DisplayFlagList::class.java))
      }
      true
    }

  }
}