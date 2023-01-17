package com.milkfoam.aposell.hook

import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.entity.Player

object PlayerPoints {

    val ppAPI = PlayerPoints.getInstance().api

    fun takePoint(player: Player, amount: Int): Boolean {
        return if (getPoint(player) < amount) {
            false
        } else {
            ppAPI.take(player.uniqueId, amount)
            true
        }
    }

    fun addPoint(player: Player, amount: Int) {
        ppAPI.give(player.uniqueId, amount)
    }

    fun getPoint(player: Player): Int {
        return ppAPI.look(player.uniqueId)
    }

}