package cn.sincky.plugin

import net.mamoe.mirai.console.command.ContactCommandSender
import net.mamoe.mirai.console.command.registerCommand
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.console.plugins.withDefaultWriteSave
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.*


object DrawLotsPluginMain : PluginBase() {

    private var drawLots: DrawLots? = null
    private const val lostPath = "lots.yml"
    private const val configPath = "config.yml"
    private val config = loadConfig(configPath)

    private val groupList by lazy {
        config.setIfAbsent("groups", mutableListOf<Long>())
        config.getLongList("groups").toMutableSet()
    }

    override fun onLoad() {
        super.onLoad()
        logger.info("onLoad")
        drawLots = DrawLots(lostPath)
        logger.info("DrawLots init success")


    }

    override fun onEnable() {
        super.onEnable()
        registerCommands()

        subscribeGroupMessages {
            (contains("抽签")){
                //logger.info(senderName + "抽签")
                if (groupList.contains(this.group.id)){
                    this.reply(sender.at() + "\n" +
                            drawLots!!.sign(sender.id))
                }
            }
            (contains("解签")){
                //logger.info(senderName + "解签")
                if (groupList.contains(this.group.id)) {
                    this.reply(
                        sender.at() + "\n" +
                                drawLots!!.unSign(sender.id)
                    )
                }
            }

        }

    }


    override fun onDisable() {
        super.onDisable()
        config["groups"] = groupList.toList()
        config.save()
        drawLots = null
        logger.info("onDisable")
    }

    // 注册命令
    private fun registerCommands() {
        registerCommand {
            name = "DrawLots"
            alias = listOf("DL")
            description = "DrawLots插件命令管理"
            usage = "[/DL enable] 打开本群的抽签功能(仅限群里控制)\n" +
                    "[/DL disable] 关闭本群的抽签功能(仅限群里控制)\n" +
                    "[/DL enable 群号] 打开指定群的抽签功能\n" +
                    "[/DL disable 群号] 关闭指定群的抽签功能"
            onCommand {
                if (it.isEmpty()) {
                    return@onCommand false
                }
                when (it[0].toLowerCase()) {
                    "enable" -> {
                        val groupID: Long  = if (it.size == 1) {
                            if(this is ContactCommandSender && this.contact is Group){ //判断是否在群里发送的命令
                                this.contact.id
                            }else{
                                return@onCommand false
                            }
                        } else {
                            it[1].toLong()
                        }
                        groupList.add(groupID)
                        this.sendMessage("群${groupID}:已打开抽签功能")
                        return@onCommand true
                    }
                    "disable" -> {
                        val groupID = if (it.size == 1) {
                            if(this is ContactCommandSender && this.contact is Group){ //判断是否在群里发送的命令
                                this.contact.id
                            }else{
                                return@onCommand false
                            }
                        } else {
                            it[1].toLong()
                        }
                        groupList.remove(groupID)
                        this.sendMessage("群${groupID}:已关闭抽签功能")
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