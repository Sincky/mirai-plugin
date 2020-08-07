package cn.sincky.plugin

import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.jvm.loadSetting
import net.mamoe.mirai.console.setting.Setting
import net.mamoe.mirai.console.setting.getValue
import net.mamoe.mirai.console.setting.value
import net.mamoe.mirai.event.*
import net.mamoe.mirai.message.data.at
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlList
import net.mamoe.yamlkt.YamlMap


object DrawLotsPluginMain : KotlinPlugin() {

    private var drawLots: DrawLots? = null
    private const val lotsPath = "lots.yml"

    object PluginSetting : Setting by DrawLotsPluginMain.loadSetting(){
        val groups by value<MutableList<Long>>()
    }

    override fun onLoad() {
        super.onLoad()
        // 抽签数据读取
        val lotsList =  mutableListOf<DrawLots.Lot>()
        val list = Yaml.default.parseYaml(this.getResource(lotsPath)) as YamlList
        list.forEach {
            val lotMap = (it.content as YamlMap)
            lotsList.add(DrawLots.Lot(lotMap.getString("uid"),lotMap.getString("sign"),lotMap.getString("unSign")))
        }
        drawLots = DrawLots(lotsList)
        logger.info("抽签数据大小：${lotsList.size}")
        for( group in PluginSetting.groups){
            logger.info("group:$group")
        }
        logger.info("DrawLots init success")
    }

    override fun onEnable() {
        super.onEnable()
        //registerCommands()

        subscribeGroupMessages {
            (contains("抽签")){
                //logger.info(senderName + "抽签")
                if (PluginSetting.groups.contains(this.group.id)){
                    this.reply(sender.at() + "\n" +
                            drawLots!!.sign(sender.id))
                }
            }
            (contains("解签")){
                //logger.info(senderName + "解签")
                if (PluginSetting.groups.contains(this.group.id)) {
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
        drawLots = null
        logger.info("onDisable")
    }
//
//    // 注册命令
//    private fun registerCommands() {
//        registerCommand {
//            name = "DrawLots"
//            alias = listOf("DL")
//            description = "DrawLots插件命令管理"
//            usage = "[/DL enable] 打开本群的抽签功能(仅限群里控制)\n" +
//                    "[/DL disable] 关闭本群的抽签功能(仅限群里控制)\n" +
//                    "[/DL enable 群号] 打开指定群的抽签功能\n" +
//                    "[/DL disable 群号] 关闭指定群的抽签功能"
//            onCommand {
//                if (it.isEmpty()) {
//                    return@onCommand false
//                }
//                when (it[0].toLowerCase()) {
//                    "enable" -> {
//                        val groupID: Long  = if (it.size == 1) {
//                            if(this is ContactCommandSender && this.contact is Group){ //判断是否在群里发送的命令
//                                this.contact.id
//                            }else{
//                                return@onCommand false
//                            }
//                        } else {
//                            it[1].toLong()
//                        }
//                        groupList.add(groupID)
//                        this.sendMessage("群${groupID}:已打开抽签功能")
//                        return@onCommand true
//                    }
//                    "disable" -> {
//                        val groupID = if (it.size == 1) {
//                            if(this is ContactCommandSender && this.contact is Group){ //判断是否在群里发送的命令
//                                this.contact.id
//                            }else{
//                                return@onCommand false
//                            }
//                        } else {
//                            it[1].toLong()
//                        }
//                        groupList.remove(groupID)
//                        this.sendMessage("群${groupID}:已关闭抽签功能")
//                        return@onCommand true
//                    }
//                    else -> {
//                        return@onCommand false
//                    }
//                }
//            }
//        }
//    }
}