package cn.sincky.plugin

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.events.author
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.PlainText
import java.util.Calendar
import kotlin.math.abs

class AntiRecall {

    private var groupMap = mutableMapOf<Long, Pair<Mutex, MutableList<Triple<Long,Int,MessageChain>>>>()
    private var openMap = mutableMapOf<Long, Boolean>()

    init{
        try {
            GlobalScope.launch {
                while (true){
                    // 间隔4min,删除过时记录消息记录
                    delay(4*60_000)
                    doClearJob()
                }
            }
        }catch (e : Throwable){
            AntiRecallPluginMain.logger.error(e)
        }
    }

    /**
     *  遍历每个群进行清理过时聊天记录
     */
    private suspend fun doClearJob() {
        AntiRecallPluginMain.logger.info("聊天记录定时清除,running")
        groupMap.forEach{
            val mutex = it.value.first
            val list = it.value.second
            val minute = Calendar.getInstance().get(Calendar.MINUTE)
            mutex.withLock {
                updateMap(list,minute)
            }
            AntiRecallPluginMain.logger.info("群${it.key} 聊天记录缓存数为${list.size}")
        }
        AntiRecallPluginMain.logger.info("聊天记录定时清除,done")
    }


    /**
     * 监听到撤回事件，调用此函数进行防撤回
     */
    suspend fun antiRecallByGroupEvent(event: MessageRecallEvent.GroupRecall) {
        val groupId = event.group.id
        if(!checkAntiRecallStatus(groupId)){
            return
        }
        val messageId = event.messageId
        val pair = groupMap[groupId] ?: return
        val mutex = pair.first
        val list = pair.second
        val minute = Calendar.getInstance().get(Calendar.MINUTE)
        mutex.withLock {
            updateMap(list,minute)
            AntiRecallPluginMain.logger.info("群${groupId} 聊天记录缓存数为${list.size}")
            // 检查list是否存在撤回messageId,如果存在 则发送撤回消息
            var triple : Triple<Long, Int, MessageChain>? = null
            list.forEach {
                val id = it.first
                if(messageId==id){
                    event.group.sendMessage(
                        PlainText("检测到撤回消息(${event.author.nameCardOrNick}):\n") +
                        it.third
                    )
                    triple = it
                    return@forEach
                }
            }
            // 删除存在的记录，避免收到两次撤回消息所重复发送消息
            if(triple!=null){
                list.remove(triple)
            }
        }
    }


    /**
     *  保存聊天记录
     */
    suspend fun saveMessage(groupId: Long, message: MessageChain) {
        if(!checkAntiRecallStatus(groupId)){
            return
        }
        // 存聊天记录
        if (!groupMap.containsKey(groupId)) {
            // 创建新的mutex和list
            groupMap[groupId] = Pair(Mutex(),mutableListOf())
        }
        val mutex = groupMap[groupId]!!.first
        val list = groupMap[groupId]!!.second
        val minute = Calendar.getInstance().get(Calendar.MINUTE)
        val messageId = message[MessageSource].id
        // 协程安全
        mutex.withLock {
            //updateMap(list, minute)
            list.add(Triple(messageId,minute,message))
        }

    }

    /**
     * 更新聊天缓存，删除3分钟后的聊天记录
     */
    private fun updateMap(list: MutableList<Triple<Long,Int,MessageChain>>, min: Int) {
        list.removeIf {
            abs(min - it.second) > 3
        }

    }

    /**
     * 设置群的防撤回开关
     */
    fun setAntiRecallStatus(groupID: Long, isOpen: Boolean) {
        openMap[groupID] = isOpen
    }

    /**
     * 检查当前群是否打开防撤回开关
     */
    private fun checkAntiRecallStatus(groupID: Long) : Boolean{
        return if (openMap.containsKey(groupID)) {
            openMap[groupID]!!
        } else {
            openMap[groupID] = false
            false
        }
    }


}

