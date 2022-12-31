package cn.sincky.mirai

import cn.sincky.mirai.notifier.DataBaseHelper
import cn.sincky.mirai.notifier.Notifier
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import javax.xml.crypto.Data

suspend fun main() {
    // Notifier test
    DataBaseHelper.initDatabase("./test.db")
    DataBaseHelper.clearDb()
    Notifier.initData()
    Notifier.setInterval(1000 * 10)
    Notifier.start()
}

