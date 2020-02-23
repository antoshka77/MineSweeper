package edu.minesweeper

import androidx.lifecycle.ViewModel
import edu.minesweeper.game.Game

class GameViewModel : ViewModel() {

    var game: Game? = null
    var mode: GameMode? = null
    var player: String? = null
    var duration: Long? = null
}