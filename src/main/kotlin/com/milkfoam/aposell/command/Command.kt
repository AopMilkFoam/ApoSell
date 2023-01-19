package com.milkfoam.aposell.command

import com.milkfoam.aposell.GuiHandle
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper


@CommandHeader("ApoSell", ["sell", "apos"], permissionDefault = PermissionDefault.TRUE)
object Command {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val open = subCommand {
        dynamic("guiName") {
            execute<Player> { player, context, argument ->
                GuiHandle(context["guiName"], player).create()
            }
        }
    }

    @CommandBody
    val openplayer = subCommand {
        dynamic("guiName") {
            dynamic("playerName") {
                execute<CommandSender> { _, context, _ ->
                    if (Bukkit.getPlayer(context["playerName"]) != null) {
                        GuiHandle(
                            context["guiName"],
                            Bukkit.getPlayer(context["playerName"])!!
                        ).create()
                    }
                }
            }
        }
    }


}