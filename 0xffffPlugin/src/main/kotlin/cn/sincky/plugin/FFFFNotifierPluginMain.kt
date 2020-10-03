package cn.sincky.plugin

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.ContactCommandSender
import net.mamoe.mirai.console.command.registerCommand
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group

object FFFFNotifierPluginMain : PluginBase() {

    private const val configPath = "config.yml"
    private val config = loadConfig(configPath)

    private val groupList by lazy {
        config.setIfAbsent("groups", mutableListOf<Long>())
        config.getLongList("groups").toMutableSet()
    }

    override fun onLoad() {
        super.onLoad()
        Logger = logger
        registerCommands()
    }


    override fun onEnable() {
        super.onEnable()
            Notifier.subscribe { entry ->
                val successEntry = mutableListOf<Notifier.Entry>()
                for ((index, item) in entry.withIndex()) {
                    val msg = "> 论坛新帖\n" +
                            "#${index}: " + item.title + "\n" +
                            "by: @" + item.author + "\n" +
                            item.link
                    try {
                        Bot.botInstances.forEach { bot ->
                            groupList.forEach { groupID ->
                                if (bot.groups.contains(groupID)) {
                                    launch {
                                        sendGroupMessage(bot.groups[groupID], msg)
                                        delay(100)
                                    }
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

    private suspend fun sendGroupMessage(group: Contact, msg: String) {
        group.sendMessage(msg)
    }

    override fun onDisable() {
        super.onDisable()
        config["groups"] = groupList.toList()
        config.save()
        Notifier.stop()
        logger.info("onDisable")

    }

    // 注册命令
    private fun registerCommands() {
        registerCommand {
            name = "0xFFFF-Notifier"
            alias = listOf("FF")
            description = "0xFFFF论坛的新帖通知插件"
            usage = "[/FF enable] 打开本群的论坛新帖通知功能(仅限群里控制)\n" +
                    "[/FF disable] 关闭本群的论坛新帖通知功能(仅限群里控制)\n" +
                    "[/FF enable 群号] 打开指定群的论坛新帖通知功能\n" +
                    "[/FF disable 群号] 关闭指定群的论坛新帖通知功能"
            onCommand {
                if (it.isEmpty()) {
                    return@onCommand false
                }
                when (it[0].toLowerCase()) {
                    "enable" -> {
                        val groupID: Long = if (it.size == 1) {
                            if (this is ContactCommandSender && this.contact is Group) { //判断是否在群里发送的命令
                                this.contact.id
                            } else {
                                return@onCommand false
                            }
                        } else {
                            it[1].toLong()
                        }
                        groupList.add(groupID)
                        this.sendMessage("群${groupID}:已打开论坛新帖通知功能")
                        return@onCommand true
                    }
                    "disable" -> {
                        val groupID = if (it.size == 1) {
                            if (this is ContactCommandSender && this.contact is Group) { //判断是否在群里发送的命令
                                this.contact.id
                            } else {
                                return@onCommand false
                            }
                        } else {
                            it[1].toLong()
                        }
                        groupList.remove(groupID)
                        this.sendMessage("群${groupID}:已关闭论坛新帖通知功能")
                        return@onCommand true
                    }
                    else -> {
                        return@onCommand false
                    }
                }
            }
        }
    }
}