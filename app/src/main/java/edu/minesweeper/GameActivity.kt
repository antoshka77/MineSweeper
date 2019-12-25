package edu.minesweeper

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.concurrent.fixedRateTimer


class GameActivity : AppCompatActivity() {

    companion object {
        const val IMMERSIVE_UI_VISIBILITY = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private lateinit var timer: Timer
    private var paused = false
    var duration = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.apply {
            systemUiVisibility = IMMERSIVE_UI_VISIBILITY

            setOnSystemUiVisibilityChangeListener {
                if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                    systemUiVisibility = IMMERSIVE_UI_VISIBILITY
            }
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_game)

        val mode = GameMode.valueOf(intent.getStringExtra(StringConstants.MODE)!!)
        val player = intent.getStringExtra(StringConstants.PLAYER)!!
        findViewById<GameView>(R.id.game_view).restartGame(mode, player)
    }

    override fun onResume() {
        super.onResume()

        if (paused) {
            resumeTimer()
            paused = false
        }
    }

    override fun onPause() {
        super.onPause()

        paused = true
        stopTimer()
    }

    fun startTimer() {
        duration = 0L
        resumeTimer()
    }

    private fun resumeTimer() {
        timer = fixedRateTimer("Timer", false, 0, 1000) {
            this@GameActivity.runOnUiThread {
                findViewById<TextView>(R.id.top_text).text = TopList.humanizedFormat(duration++)
            }
        }
    }

    fun stopTimer() {
        timer.cancel()
    }
}