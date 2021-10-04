package com.example.mysecondapp

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.mysecondapp.ui.GameFragment
import com.example.mysecondapp.ui.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

//  internal var score = 0
//  internal var gameStarted = false
//  internal lateinit var countDownTimer: CountDownTimer
//  internal val initialCountDown: Long = 20000
//  internal val countDownInterval: Long = 1000
//
//  internal lateinit var gameScoreText: TextView
//  internal lateinit var timeLeftText: TextView
//  internal lateinit var tapMeBtn: Button
//  internal lateinit var goToListBtn: Button
  internal lateinit var bottomNavigationView: BottomNavigationView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
//    gameScoreText = findViewById(R.id.yourScoreText)
//    timeLeftText = findViewById(R.id.timeLeftText)
//    tapMeBtn = findViewById(R.id.tapMeBtn)
    bottomNavigationView = findViewById(R.id.bottomNavigation)
    val homeFragment = HomeFragment()
    val gameFragment = GameFragment()
//    tapMeBtn.setOnClickListener { view ->
//      incrementScore()
//    }

    bottomNavigationView.setOnItemReselectedListener {
      Log.d("item","${it.itemId}")
      when(it.itemId){
        R.id.home_menu->setCurrentFragment(homeFragment)
        R.id.game_menu->setCurrentFragment(gameFragment)
        R.id.flag_menu->navigateToFlagActivity()

      }
      true
    }
    //resetGame()

//    goToListBtn = findViewById(R.id.goToListBtn)
//    goToListBtn.setOnClickListener{
//      val intent = Intent(this,DisplayFlagList::class.java)
//      startActivity(intent)
//    }

  }

  private fun setCurrentFragment(fragment: Fragment)=
    supportFragmentManager.beginTransaction().apply {
      replace(R.id.fragmentContainerView,fragment)
      commit()
    }

  private fun navigateToFlagActivity(){
      val intent = Intent(this,DisplayFlagList::class.java)
      startActivity(intent)
  }
//
//  private fun resetGame() {
//    score = 0
//    gameScoreText.text = getString(R.string.yourScoreText, score)
//    val initialTimeLeft = initialCountDown / 1000
//    timeLeftText.text = getString(R.string.timeLeftText, initialTimeLeft)
//
//    countDownTimer = object : CountDownTimer(initialCountDown, countDownInterval) {
//      override fun onTick(p0: Long) {
//        val timeLeft = p0 / 1000
//        timeLeftText.text = getString(R.string.timeLeftText, timeLeft)
//      }
//
//      override fun onFinish() {
//        endGame()
//      }
//    }
//    gameStarted = false
//  }
//
//  private fun startGame() {
//    countDownTimer.start()
//    gameStarted = true
//  }
//
//  private fun endGame() {
//    Toast.makeText(this, getString(R.string.gameOverMessage, score), Toast.LENGTH_LONG).show()
//    resetGame()
//  }
//
//  private fun incrementScore() {
//    if (!gameStarted) {
//      startGame()
//    }
//    score += 1
//    val newScore = getString(R.string.yourScoreText, score)
//    gameScoreText.text = newScore
//  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    return when (item.itemId) {
      R.id.actionAbout -> {
        showInfo()
        true
      }
      R.id.actionClose -> {
        closeApp()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun showInfo() {
    val dialogTitle = getString(R.string.aboutTitle)
    val dialogMessage = getString(R.string.aboutMessage)

    val builder = AlertDialog.Builder(this)
    builder.setTitle(dialogTitle)
    builder.setMessage(dialogMessage)
    builder.create().show()
  }

  private fun closeApp(){

    val builder = AlertDialog.Builder(this)
    builder.setMessage(R.string.closeMessage)
      .setPositiveButton(R.string.yesText,
        DialogInterface.OnClickListener { dialog, id ->
          exitProcess(0)
        })
      .setNegativeButton(R.string.noText,
        DialogInterface.OnClickListener { dialog, id ->
          // User cancelled the dialog
        })
    // Create the AlertDialog object and return it
    builder.create().show()
  }
}