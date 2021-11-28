package cn.sincky.mirai.drawlots.main

import cn.sincky.mirai.drawlots.DrawLots
import cn.sincky.mirai.drawlots.LOTS_FILE
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.getGroupOrNull
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.at
import net.mamoe.mirai.utils.info

object DrawLotsPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "cn.sincky.mirai.DrawLots-Plugin",
        name = "DrawLots-Plugin",
        version = "2.0-SNAPSHOT"
    )
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        CommandManager.registerCommand(PluginCommand)
        PluginData.reload()
        init()
    }

    private lateinit var drawLots: DrawLots

    private fun init() {
        val tmpDrawLots = try {
            DrawLots.loadDrawLotsData(getResource(LOTS_FILE)!!)
        } catch (t: Throwable) {
            logger.error(t.message)
            null
        }
        if (tmpDrawLots == null) {
            logger.info { "加载抽签数据失败！" }
            return
        }
        drawLots = tmpDrawLots
        globalEventChannel().subscribeGroupMessages {
            (contains("抽签")){
                //logger.info(senderName + "抽签")
                if (PluginData.subscribeGroup.contains(this.group.id)) {
                    group.sendMessage(sender.at() + "\n" + drawLots.sign(sender.id))
                }
            }
            (contains("解签")){
                //logger.info(senderName + "解签")
                if (PluginData.subscribeGroup.contains(this.group.id)) {
                    group.sendMessage(sender.at() + "\n" + drawLots.unSign(sender.id))
                }
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
    }
}

object PluginData : AutoSavePluginData("PluginData") {
    var subscribeGroup: MutableList<Long> by value()
}

object PluginCommand : CompositeCommand(
    DrawLotsPlugin,
    "DrawLots",
    "DL",
    description = """
        DrawLots插件命令管理:
        [/DL enable] 打开本群的抽签功能(仅限群里控制)
        [/DL disable] 关闭本群的抽签功能(仅限群里控制)
        [/DL enable 群号] 打开指定群的抽签功能
        [/DL disable 群号] 关闭指定群的抽签功能
    """.trimIndent()
) {
    @SubCommand
    suspend fun CommandSender.enable() {
        val group = this.getGroupOrNull() ?: return
        sendMessage("群${group.id}:已打开[${primaryName}]功能")
        PluginData.subscribeGroup.add(group.id)
    }

    @SubCommand
    suspend fun CommandSender.disable() {
        val group = this.getGroupOrNull() ?: return
        sendMessage("群${group.id}:已关闭[${primaryName}]功能")
        PluginData.subscribeGroup.remove(group.id)
    }


    @SubCommand
    suspend fun CommandSender.enable(group: Group) {
        DrawLotsPlugin.logger.info("/DL enable ${group.id}")
        PluginData.subscribeGroup.add(group.id)
    }

    @SubCommand
    suspend fun CommandSender.disable(group: Group) {
        DrawLotsPlugin.logger.info("/DL disable ${group.id}")
        PluginData.subscribeGroup.remove(group.id)
    }
}