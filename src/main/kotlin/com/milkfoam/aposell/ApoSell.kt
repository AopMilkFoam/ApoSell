package com.milkfoam.aposell

import jdk.nashorn.internal.runtime.regexp.joni.Config.log
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration

object ApoSell : Plugin() {

    override fun onEnable() {
        info("                       _____      _ _ ")
        info("     /\\               / ____|    | | |")
        info("    /  \\   _ __   ___| (___   ___| | |")
        info("   / /\\ \\ | '_ \\ / _ \\\\___ \\ / _ \\ | |")
        info("  / ____ \\| |_) | (_) |___) |  __/ | |")
        info(" /_/    \\_\\ .__/ \\___/_____/ \\___|_|_|")
        info("          | |                         ")
        info("          |_|                         ")

    }

    @Config(migrate = true, value = "settings.yml", autoReload = true)
    lateinit var config: Configuration
        private set

    @Config(migrate = true, value = "gui/example.yml", autoReload = true)
    lateinit var example: Configuration
        private set

}