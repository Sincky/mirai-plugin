package cn.sincky.mirai.notifier.main


import cn.sincky.mirai.notifier.Notifier
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.getGroupOrNull
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.info

object NotifierPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "cn.sincky.mirai.FFFF-Notifier",
        name = "0xFFFF-Notifier",
        version = "2.0-SNAPSHOT",
    )
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        CommandManager.registerCommand(PluginCommand)
        PluginData.reload()
        init()
    }

    private fun init() {
        // 注册订阅回调
        Notifier.subscribe { entry ->
            val successEntry = mutableListOf<Notifier.Entry>()
            for ((index, item) in entry.withIndex()) {
                val msg = """
                    > 论坛新帖
                    #${index}: ${item.title}
                    By: @${item.author}
                    Link: ${item.link}
                """.trimIndent()

                try {
                    Bot.instances.forEach {
                        PluginData.subscribeGroup.forEach { groupID ->
                            if (it.groups.contains(groupID)) {
                                launch {
                                    it.groups[groupID]?.sendMessage(msg)
                                }
                                Thread.sleep(500)
                            }
                        }
                    }
                    successEntry.add(item)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return@subscribe successEntry
        }
    }

    override fun onDisable() {
        super.onDisable()
        Notifier.stop()
    }
}

object PluginData : AutoSavePluginData("PluginData") {
    var subscribeGroup: MutableList<Long> by value()
}

object PluginCommand : CompositeCommand(
    NotifierPlugin,
    "0xFFFF-Notifier",
    "FF",
    description = """
        0xFFFF论坛的新帖通知插件
        [/FF enable] 打开本群的论坛新帖通知功能(仅限群里控制)
        [/FF disable] 关闭本群的论坛新帖通知功能(仅限群里控制)
        [/FF enable 群号] 打开指定群的论坛新帖通知功能
        [/FF disable 群号] 关闭指定群的论坛新帖通知功能
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
        NotifierPlugin.logger.info("/FF enable ${group.id}")
        PluginData.subscribeGroup.add(group.id)
    }

    @SubCommand
    suspend fun CommandSender.disable(group: Group) {
        NotifierPlugin.logger.info("/FF disable ${group.id}")
        PluginData.subscribeGroup.remove(group.id)
    }
}