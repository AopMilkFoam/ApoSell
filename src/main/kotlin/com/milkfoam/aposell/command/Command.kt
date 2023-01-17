package com.milkfoam.aposell.command

import com.milkfoam.aposell.hook.Vault
import com.milkfoam.aposell.ui.GuiData
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.platform.compat.VaultService.economy


@CommandHeader("ApoSell", ["sell", "apos"])
object Command {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val open = subCommand {
        dynamic("guiName") {
            execute<Player> { player, context, argument ->
                GuiData(context["guiName"], player).create()
            }
        }
    }

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val openplayer = subCommand {
        dynamic("guiName") {
            dynamic("playerName") {
                execute<CommandSender> { player, context, argument ->
                    if (Bukkit.getPlayer(context["playerName"]) != null) {
                        GuiData(
                            context["guiName"],
                            Bukkit.getPlayer(context["playerName"])!!
                        ).create()
                    }
                }
            }
        }
    }


}