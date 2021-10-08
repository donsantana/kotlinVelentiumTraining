package com.velentium.android.platformv.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.velentium.android.platformv.R

class GameActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game)
    //setCurrentFragment(findViewById(R.id.gameFragment))
  }

  private fun setCurrentFragment(fragment: Fragment)=
    supportFragmentManager.beginTransaction().apply {
      replace(R.id.gameFragmentContainerView,fragment)
      commit()
    }
}