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
import edu.minesweeper.game.*
import java.util.*


class GameView : View {

    private lateinit var game: Game
    private lateinit var mode: GameMode
    private lateinit var player: String
    private var paint = Paint()

    private var lastActionTime = 0L
    private val columnsMargin = MainActivity.screenSize.x % IMAGE_SIZE / 2
    private val rowsMargin = MainActivity.screenSize.y % IMAGE_SIZE / 2
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
                    columnsMargin + coord.x.toFloat() * IMAGE_SIZE,
                    rowsMargin + coord.y.toFloat() * IMAGE_SIZE,
                    paint
                )
            }
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        val x = (event.x - columnsMargin) / IMAGE_SIZE
        val y = (event.y - rowsMargin) / IMAGE_SIZE

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

    fun restartGame(mode: GameMode, player: String) {
        game = Game().apply { start() }
        this.mode = mode
        this.player = player
        parentActivity.startTimer()
    }

    private fun showRestartDialog(text: String, duration: Long) {
        AlertDialog.Builder(context).apply {
            setMessage("$text за ${TopList.humanizedFormat(duration)}")
            setTitle(R.string.game_over)

            setPositiveButton(R.string.retry) { dialog, id ->
                restartGame(mode, player)
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
}