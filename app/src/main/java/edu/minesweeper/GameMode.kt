package edu.minesweeper

enum class GameMode(var bombsPercentage: Double) {
    EASY(0.1), MEDIUM(0.15), HARD(0.2)
}