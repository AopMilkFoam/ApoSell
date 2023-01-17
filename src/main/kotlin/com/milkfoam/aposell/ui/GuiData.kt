package com.milkfoam.aposell.ui

import com.milkfoam.aposell.ApoSell.config
import com.milkfoam.aposell.hook.PlayerPoints
import com.milkfoam.aposell.hook.Vault
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getConsoleSender
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common5.Coerce
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.parseToXMaterial
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.module.configuration.Configuration
import taboolib.module.kether.KetherShell
import taboolib.module.kether.runKether
import taboolib.module.nms.getName
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.*


class GuiData(
    val file: String,
    val opener: Player,
) {

    val vaultSellKey = config.getString("VaultSellKey")!!
    val pointSellKey = config.getString("PointSellKey")!!
    val prefix = config.getString("Prefix")!!.colored()
    val onCloseSell = config.getBoolean("OnCloseSell")
    val guiFile = Configuration.loadFromFile(releaseResourceFile("gui/${file}.yml"))


    fun create() {
        val title = guiFile.getString("Title") ?: "出售商店"
        val map = guiFile.getStringList("Layout")
        val sellItems: MutableMap<ItemStack, Int> = mutableMapOf()
        openEvent()
        opener.openMenu<Basic>(title = title.colored()) {
            map(*map.toTypedArray())
            handLocked(false)
            guiFile.getConfigurationSection("Items")?.getKeys(false)?.forEach { slot ->
                set(
                    Coerce.toChar(slot),
                    buildItem(guiFile.getString("Items.${slot}.material")?.parseToXMaterial() ?: XMaterial.STONE) {
                        name = guiFile.getString("Items.${slot}.name")?.colored()?.replacePlaceholder(opener)
                        lore.addAll(
                            guiFile.getStringList("Items.${slot}.lore").colored().replacePlaceholder(opener)
                        )
                    }) {
                    isCancelled = true
                }
            }

            onClick('-') { clickEvent ->
                var vaultAllMoney = 0.0
                var pointAllMoney = 0
                val list: ArrayList<ItemStack> = arrayListOf()
                clickEvent.getItems('+').forEach {
                    val amount = it.amount
                    if (it.isNotAir() && it.hasLore(vaultSellKey)) {
                        vaultAllMoney += getVaultSellPrice(it.itemMeta?.lore, amount)!!
                    } else if (it.isNotAir() && it.hasLore(pointSellKey)) {
                        pointAllMoney += getPointSellPrice(it.itemMeta?.lore, amount)!!
                    } else {
                        list.add(it)
                    }
                    if (it.isNotAir()) {
                        sellItems.put(it, amount)
                    }
                }
                getSlots('+').forEach {
                    if (!list.contains(clickEvent.inventory.getItem(it))) {
                        clickEvent.inventory.setItem(it, ItemStack(Material.AIR))
                    }
                }
                if (vaultAllMoney > 0 || pointAllMoney > 0) {
                    Vault.addMoney(opener, vaultAllMoney)
                    PlayerPoints.addPoint(opener, pointAllMoney)
                    opener.sendLang(
                        "Sell-success",
                        prefix,
                        vaultAllMoney,
                        pointAllMoney,
                        Vault.getMoney(opener),
                        PlayerPoints.getPoint(opener)
                    )
                }
            }

            if (onCloseSell) {
                onClose { invEvent ->
                    val list: ArrayList<Int> = arrayListOf()
                    var vaultAllMoney = 0.0
                    var pointAllMoney = 0
                    getSlots('+').forEach {
                        val item = invEvent.inventory.getItem(it) ?: ItemStack(Material.AIR)
                        val amount = item.amount
                        if (item.isNotAir() && item.hasLore(vaultSellKey)) {
                            vaultAllMoney += getVaultSellPrice(item.itemMeta?.lore, amount)!!
                            sellItems.put(item, amount)
                        } else if (item.isNotAir() && item.hasLore(pointSellKey)) {
                            pointAllMoney += getPointSellPrice(item.itemMeta?.lore, amount)!!
                            sellItems.put(item, amount)
                        } else {
                            list.add(it)
                        }
                    }
                    invEvent.returnItems(list)
                    jsonInfo(sellItems)
                    if (vaultAllMoney > 0 || pointAllMoney > 0) {
                        Vault.addMoney(opener, vaultAllMoney)
                        PlayerPoints.addPoint(opener, pointAllMoney)
                        opener.sendLang(
                            "Sell-success",
                            prefix,
                            vaultAllMoney,
                            pointAllMoney,
                            Vault.getMoney(opener),
                            PlayerPoints.getPoint(opener)
                        )
                    }
                }
            } else {
                onClose { inv ->
                    val list: ArrayList<Int> = arrayListOf()
                    getSlots('+').forEach {
                        list.add(it)
                    }
                    inv.returnItems(list)
                }
            }
        }
    }

    fun getVaultSellPrice(Lore: List<String>?, amount: Int): Double? {
        var price = 0.0
        if (Lore != null) for (i in Lore.indices) {
            if (Lore[i].contains(vaultSellKey)) {
                val filter = Lore[i].replace("[^\\d.]".toRegex(), "")
                if (filter.isNotEmpty() && filter.toDouble() > 0.0)
                    price += filter.toDouble() * amount
            }
        }
        return price
    }

    fun getPointSellPrice(Lore: List<String>?, amount: Int): Int? {
        var price = 0
        if (Lore != null) for (i in Lore.indices) {
            if (Lore[i].contains(pointSellKey)) {
                val filter = Lore[i].replace("[^\\d.]".toRegex(), "")
                if (filter.isNotEmpty() && filter.split(".")[0].toInt() > 0)
                    price += filter.split(".")[0].toInt() * amount
                //暂时想到的办法
            }
        }
        return price
    }

    fun openEvent() {
        val openType = guiFile.getString("OpenType") ?: ""
        val openEvent = guiFile.getString("OpenEvent")?.replacePlaceholder(opener) ?: ""
        when (openType) {
            "op" -> {
                val isOp = opener.isOp
                opener.isOp = true
                try {
                    Bukkit.dispatchCommand(opener, openEvent)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
                opener.isOp = isOp
            }

            "console" -> {
                Bukkit.dispatchCommand(getConsoleSender(), openEvent)
            }

            "kether" -> {
                runKether {
                    KetherShell.eval(openEvent, namespace = listOf("ApoSell"), sender = adaptPlayer(opener))
                        .thenApply { v ->
                            opener.sendMessage(v.toString())
                        }
                }
            }

            "" -> {
            }
        }
    }

    fun jsonInfo(itemList: MutableMap<ItemStack, Int>) {
        if (itemList.isNotEmpty()) {
            TellrawJson().sendTo(adaptPlayer(opener)) {
                if (guiFile.getString("SellJson") != null) {
                    append(prefix + " " + guiFile.getString("SellJson")!!)
                    hoverText(itemList.map { it.key.getName() + "*" + it.value }.toString())
                }
            }
        }

    }

}
