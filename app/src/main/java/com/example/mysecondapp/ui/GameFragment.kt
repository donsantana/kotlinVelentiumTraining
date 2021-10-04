package com.example.mysecondapp.ui

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.mysecondapp.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : Fragment() {
  // TODO: Rename and change types of parameters
  private var gameStarted = false
  private lateinit var countDownTimer: CountDownTimer
  private var score = 0
  internal val initialCountDown: Long = 20000
  internal val countDownInterval: Long = 1000

  private lateinit var gameScoreText: TextView
  internal lateinit var timeLeftText: TextView
  private lateinit var tapMeBtn: Button

//  override fun onCreate(savedInstanceState: Bundle?) {
//    super.onCreate(savedInstanceState)
//  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    // Inflate the layout for this fragment
    val root = inflater.inflate(R.layout.fragment_game, container, false)
    gameScoreText = root.findViewById(R.id.yourScoreText)
    timeLeftText = root.findViewById(R.id.timeLeftText)
    tapMeBtn = root.findViewById(R.id.tapMeBtn)
    tapMeBtn.setOnClickListener { view ->
      incrementScore()
    }

    resetGame()
    return root
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
    Toast.makeText(this.context, getString(R.string.gameOverMessage, score), Toast.LENGTH_LONG).show()
    resetGame()
  }

  private fun incrementScore() {
    if (!gameStarted){
      startGame()
    }
    score += 1
    val newScore = getString(R.string.yourScoreText, score)
    gameScoreText.text = newScore
  }

  companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GameFragment.
     */
    // TODO: Rename and change types and number of parameters
    @JvmStatic
    fun newInstance(param1: String, param2: String) =
      GameFragment().apply {
        arguments = Bundle().apply {
        }
      }
  }
}