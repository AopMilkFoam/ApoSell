package com.milkfoam.aposell.utils

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.chat.TellrawJson
import taboolib.module.configuration.Configuration
import taboolib.module.nms.getName

fun jsonInfo(itemList: MutableMap<ItemStack, Int>, opener: Player, guiFile: Configuration,prefix:String) {
    if (itemList.isNotEmpty()) {
        TellrawJson().sendTo(adaptPlayer(opener)) {
            if (guiFile.getString("SellJson") != null) {
                append(prefix + " " + guiFile.getString("SellJson")!!)
                hoverText(itemList.map { it.key.getName() + "*" + it.value }.toString())
            }
        }
    }
}