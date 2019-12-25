package edu.minesweeper

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.minesweeper.game.Coord
import edu.minesweeper.game.Ranges
import android.util.TypedValue


class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var screenSize: Point
    }

    private lateinit var player: String
    private lateinit var mode: GameMode

    override fun onCreate(savedInstanceState: Bundle?) {
        screenSize = Point().apply { windowManager.defaultDisplay.getRealSize(this) }

        val typedValue = TypedValue()
        val actionBarHeight =
            if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true))
                TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
            else
                0
        screenSize.y -= actionBarHeight

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getSharedPreferences(StringConstants.APP, Context.MODE_PRIVATE).apply {
            val topJson = getString(StringConstants.TOP, null)
            if (topJson != null) {
                val type = object : TypeToken<HashMap<GameMode, MutableMap<String, Long>>>() {}.type
                TopList.list = Gson().fromJson(topJson, type)
            }
        }

        findViewById<Spinner>(R.id.gameMode_spinner).apply {
            adapter = ArrayAdapter.createFromResource(
                this@MainActivity,
                R.array.game_mods_array,
                android.R.layout.simple_spinner_item
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(1)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                    mode = when (pos) {
                        0 -> GameMode.EASY
                        1 -> GameMode.MEDIUM
                        2 -> GameMode.HARD
                        else -> return
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) { }
            }
        }

        findViewById<Button>(R.id.btn_desc).setOnClickListener {
            startActivity(Intent(this, DescActivity::class.java))
        }

        findViewById<Button>(R.id.btn_play).setOnClickListener {
            val columns = screenSize.x / GameView.IMAGE_SIZE
            val rows = screenSize.y / GameView.IMAGE_SIZE

            player = findViewById<EditText>(R.id.player).text.toString()
            if (player == "")
                player = getString(R.string.guest)

            Ranges.setSize(Coord(columns, rows))
            Ranges.bombsCount = (columns * rows * mode.bombsPercentage).toInt()

            startActivity(Intent(this, GameActivity::class.java)
                .putExtra(StringConstants.PLAYER, player)
                .putExtra(StringConstants.MODE, mode.name)
            )
        }

        findViewById<Button>(R.id.btn_top).setOnClickListener {
            startActivity(Intent(this, TopActivity::class.java))
        }
    }

    override fun onStop() {
        super.onStop()

        getSharedPreferences(StringConstants.APP, Context.MODE_PRIVATE).edit().apply {
            val topJson = Gson().toJson(TopList.list)
            putString(StringConstants.TOP, topJson)

            apply()
        }
    }
}
