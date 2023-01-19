package com.milkfoam.aposell.utils

import taboolib.common5.LoreMap
import taboolib.module.chat.uncolored

fun getVaultSellPrice(lore: List<String>?, amount: Int, key: String): Double {
    var price = 0.0
    lore?.forEachIndexed { index, string ->
        if (string.contains(key)) {
            val a = lore[index].uncolored()
            val b = a.replace(Regex("[\\u4e00-\\u9fa5_a-zA-Z:]"), "")
            price = b.toDouble() * amount
        }
    }
    return price
}