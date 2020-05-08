package cn.sincky.plugin


import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.ContactCommandSender
import net.mamoe.mirai.console.command.registerCommand
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.id


object AntiRecallPluginMain : PluginBase() {

    private var antiRecall : AntiRecall? = null

    override fun onLoad() {
        super.onLoad()
        antiRecall = AntiRecall()
    }

    override fun onEnable() {
        super.onEnable()
        logger.info("AntiRecall Plugin loaded!")
        registerCommands()

        subscribeGroupMessages {
            always {
                launch {
                    //保存聊天记录
                    antiRecall!!.saveMessage(group.id,message)
                }
            }
        }


        subscribeAlways<MessageRecallEvent> { event ->
            launch {
                //监听撤回事件
                if (event is MessageRecallEvent.GroupRecall){
                    antiRecall!!.antiRecallByGroupEvent(event)
                }
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        AntiRecallPluginMain.logger.info("onDisable")
        antiRecall = null
    }

    // 注册命令
    private fun registerCommands() {
        registerCommand {
            name = "AntiRecall"
            alias = listOf()
            description = "AntiRecall插件命令管理"
            usage = "[/AntiRecall enable] 打开本群的防撤回功能(仅限群里控制)\n" +
                    "[/AntiRecall disable] 关闭本群的防撤回功能(仅限群里控制)\n" +
                    "[/AntiRecall enable 群号] 打开指定群的防撤回功能\n" +
                    "[/AntiRecall disable 群号] 关闭指定群的防撤回功能"
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
                        antiRecall!!.setAntiRecallStatus(groupID,true)
                        this.sendMessage("群${groupID}:已打开防撤回功能")
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
                        antiRecall!!.setAntiRecallStatus(groupID,false)
                        this.sendMessage("群${groupID}:已关闭防撤回功能")
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