package com.example.mysecondapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

  internal var score = 0
  internal var gameStarted = false
  internal lateinit var countDownTimer: CountDownTimer
  internal val initialCountDown: Long = 20000
  internal val countDownInterval: Long = 1000

  internal lateinit var gameScoreText: TextView
  internal lateinit var timeLeftText: TextView
  internal lateinit var tapMeBtn: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    gameScoreText = findViewById(R.id.yourScoreText)
    timeLeftText = findViewById(R.id.timeLeftText)
    tapMeBtn = findViewById(R.id.tapMeBtn)
    tapMeBtn.setOnClickListener { view ->
      incrementScore()
    }
    resetGame()
  }

  private fun resetGame() {
    score = 0
    gameScoreText.text = getString(R.string.yourScoreText, score)
    val initialTimeLeft = initialCountDown / 1000
    timeLeftText.text = getString(R.string.timeLeftText, initialTimeLeft)

    countDownTimer = object : CountDownTimer(initialCountDown, countDownInterval) {
      override fun onTick(p0: Long) {
        val timeLeft = p0 / 1000
        timeLeftText.text = getString(R.string.timeLeftText, timeLeft)
      }

      override fun onFinish() {
        endGame()
      }
    }
    gameStarted = false
  }

  private fun startGame() {
    countDownTimer.start()
    gameStarted = true
  }

  private fun endGame() {
    Toast.makeText(this, getString(R.string.gameOverMessage, score), Toast.LENGTH_LONG).show()
    resetGame()
  }

  private fun incrementScore() {
    if (!gameStarted) {
      startGame()
    }
    score += 1
    val newScore = getString(R.string.yourScoreText, score)
    gameScoreText.text = newScore
  }

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
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun showInfo() {
    val dialogTitle = getString(R.string.aboutTitle, BuildConfig.VERSION_NAME)
    val dialogMessage = getString(R.string.aboutMessage)

    val builder = AlertDialog.Builder(this)
    builder.setTitle(dialogTitle)
    builder.setMessage(dialogMessage)
    builder.create().show()
  }
}