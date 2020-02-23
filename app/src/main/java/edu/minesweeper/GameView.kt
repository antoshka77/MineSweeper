package edu.minesweeper

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import edu.minesweeper.MainActivity.Companion.actionBarHeight
import edu.minesweeper.MainActivity.Companion.screenSize
import edu.minesweeper.game.*
import java.util.*


class GameView : View {

    lateinit var game: Game
    lateinit var mode: GameMode
    lateinit var player: String
    private var paint = Paint()

    private var lastActionTime = 0L
    private val columnsMargin =
        (ifRotated(screenSize.x, screenSize.y).toInt() % IMAGE_SIZE + actionBarHeight!!) / 2
    private val rowsMargin =
        ifRotated(screenSize.y, screenSize.x).toInt() % IMAGE_SIZE / 2
    private val parentActivity = parentActivity()

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        setImages()
    }

    companion object {
        const val IMAGE_SIZE = 100
        private const val TOUCH_DURATION = 500
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (coord in Ranges.getAllCoords()) {
            game.getBox(coord).image?.apply {
                canvas!!.drawBitmap(
                    this as Bitmap,
                    columnsMargin + (ifRotated(coord.x, coord.y).toFloat()) * IMAGE_SIZE,
                    rowsMargin + (ifRotated(coord.y, coord.x).toFloat()) * IMAGE_SIZE,
                    paint
                )
            }
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        val x = (ifRotated(event.x, event.y).toFloat() - columnsMargin) / IMAGE_SIZE
        val y = (ifRotated(event.y, event.x).toFloat() - rowsMargin) / IMAGE_SIZE

        val coord = Coord(x.toInt(), y.toInt())
        if (!Ranges.inRange(coord)) return false


        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastActionTime = System.currentTimeMillis()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val gameContinue = if (System.currentTimeMillis() - lastActionTime > TOUCH_DURATION)
                    game.move(coord)

                else
                    game.flag(coord)

                if (!gameContinue) {
                    val duration = parentActivity.also { it.stopTimer() }.duration
                    when (game.state) {
                        GameState.BOMBED -> showRestartDialog(context.getString(R.string.you_lose), duration)

                        GameState.WINNER -> {

                            TopList.saveNewResult(
                                mode,
                                player,
                                duration
                            )
                            showRestartDialog(context.getString(R.string.you_win), duration)
                        }
                        else -> return false
                    }
                }
            }
        }

        return performClick()
    }

    override fun performClick(): Boolean {
        invalidate()
        return super.performClick()
    }

    fun restartGame(game: Game, mode: GameMode, player: String) {
        this.game = game
        this.mode = mode
        this.player = player
    }

    private fun showRestartDialog(text: String, duration: Long) {
        AlertDialog.Builder(context).apply {
            setMessage("$text за ${TopList.humanizedFormat(duration)}")
            setTitle(R.string.game_over)

            setPositiveButton(R.string.retry) { dialog, id ->
                restartGame(Game().apply { start() }, mode, player)
                parentActivity.startTimer()
                invalidate()
            }
            setNegativeButton(R.string.to_menu) { dialog, id ->
                parentActivity.finish()
            }

            create()
        }.show()
    }

    private fun setImages() {
        for (box in Box.values())
            box.image = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources,
                    resources.getIdentifier(
                        box.name.toLowerCase(Locale.ENGLISH),
                        "drawable",
                        context.packageName
                    )
                ),
                IMAGE_SIZE, IMAGE_SIZE, true
            )
    }

    private fun parentActivity(): GameActivity {
        var parent = context
        while (parent is ContextWrapper) {
            if (parent is Activity) break
            parent = (context as ContextWrapper).baseContext
        }

        return parent as GameActivity
    }

    private fun ifRotated(a: Number, b: Number): Number =
        if (resources.configuration.orientation == MainActivity.orientation) a else b
}