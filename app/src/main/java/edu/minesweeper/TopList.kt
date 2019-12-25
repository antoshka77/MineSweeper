package edu.minesweeper

import java.util.concurrent.TimeUnit


object TopList {
    var list = HashMap<GameMode, MutableMap<String, Long>>()

    init {
        clear()
    }

    fun saveNewResult(mode: GameMode, player: String, duration: Long) {
        if (list[mode]!![player] == null || list[mode]!![player]!! > duration) {
            list[mode]!![player] = duration
        }
    }

    fun clear() {
        for (mode in GameMode.values())
            list[mode] = HashMap()
    }

    fun humanizedFormat(time: Long): String {
        val hours = TimeUnit.SECONDS.toHours(time)
        val minutes = TimeUnit.SECONDS.toMinutes(time) % TimeUnit.HOURS.toSeconds(1)
        val seconds = time % TimeUnit.MINUTES.toSeconds(1)

        return String.format("%d:%02d:%02d", hours, minutes, seconds)
    }
}