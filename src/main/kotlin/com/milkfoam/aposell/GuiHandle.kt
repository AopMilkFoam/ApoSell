package com.milkfoam.aposell

import com.milkfoam.aposell.ApoSell.config
import com.milkfoam.aposell.hook.Vault
import com.milkfoam.aposell.utils.getVaultSellPrice
import com.milkfoam.aposell.utils.jsonInfo
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getConsoleSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common5.Coerce
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.parseToXMaterial
import taboolib.module.chat.colored
import taboolib.module.configuration.Configuration
import taboolib.module.kether.KetherShell
import taboolib.module.kether.runKether
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.*


class GuiHandle(
    file: String,
    val opener: Player,
) {

    val vaultSellKey = config.getString("VaultSellKey")!!
    val prefix = config.getString("Prefix")!!.colored()
    val guiFile = Configuration.loadFromFile(releaseResourceFile("gui/${file}.yml"))
    val jsonItem: MutableMap<ItemStack, Int> = mutableMapOf()


    fun create() {
        val title = guiFile.getString("Title") ?: "出售商店"
        val map = guiFile.getStringList("Layout")
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


            //代码重写，学到了挺多东西
            onClick('-') { clickEvent ->
                var allPrice = 0.0
                clickEvent.getItems('+').filter {
                    it.isNotAir()
                }.forEach {
                    val sellPrice = getVaultSellPrice(it.itemMeta?.lore, it.amount, vaultSellKey)
                    clickEvent.inventory.remove(it)
                    allPrice += sellPrice
                    jsonItem[it] = it.amount
                }
                if (allPrice > 0) {
                    Vault.addMoney(opener, allPrice)
                    jsonInfo(jsonItem, opener, guiFile, prefix)
                    opener.sendLang("Sell-Vault", prefix, allPrice, Vault.getMoney(opener))
                }
            }

            onClose {
                it.returnItems(this.getSlots('+'))
            }

        }
    }

    fun openEvent() {
        val openType = guiFile.getString("OpenType") ?: ""
        val openEvent = guiFile.getString("OpenEvent")?.replacePlaceholder(opener) ?: ""
        when (openType) {
            "op" -> {
                //经过多次修改，终于修改成了
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
                return
            }

        }
    }


}