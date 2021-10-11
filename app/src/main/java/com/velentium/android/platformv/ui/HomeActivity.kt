package com.velentium.android.platformv.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import com.velentium.android.platformv.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_home.*

class HomeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)
    setSupportActionBar(homeToolbar)

    val toggle = ActionBarDrawerToggle(this,drawer_layout,homeToolbar,R.string.open, R.string.close)
    toggle.isDrawerIndicatorEnabled = true
    drawer_layout.addDrawerListener(toggle)
    toggle.syncState()

  }
}