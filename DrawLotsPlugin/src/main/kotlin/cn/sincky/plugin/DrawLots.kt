package cn.sincky.plugin

import net.mamoe.mirai.console.plugins.ConfigSection
import java.util.*

class DrawLots(lotsPath : String) {

    private var lots: List<ConfigSection>?
    private var lotMap = mutableMapOf<Long,Int>()
    private var day: Int

    /**
     * 抽签类初始化，加载抽签数据
     */
    init{
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        try {
            lots = DrawLotsPluginMain.getResourcesConfig(lotsPath).getConfigSectionList("Lots")
            DrawLotsPluginMain.logger.info("本地抽签数据加载成功,数据大小为："+lots!!.size)
        } catch (e: Exception) {
            e.printStackTrace()
            DrawLotsPluginMain.logger.info("无法加载本地抽签数据")
            lots = null
        }

    }

    /**
     *  抽签
     */
    fun sign(id : Long) : String {
        return if (lots != null){
            checkDay()
            if(lotMap.contains(id)){
                "你已经抽过签了,请输入【解签】查看"
            }else{
                val index = lots!!.indices.random()
                lotMap[id] = index
                val lot = lots!![index]
                "签位：" + lot.getString("uid") + "\n" +
                lot.getString("sign") + "\n" +
                "解签请发送【解签】"
            }
        }else{
            "Lots init failed!"
        }
    }

    /**
     *  解签
     */
    fun unSign(id : Long) : String {
        return if (lots != null){
            checkDay()
            if(lotMap.contains(id)){
                val index: Int = lotMap.getValue(id)
                val lot = lots!![index]
                ("解签：" + lot.getString("uid") + "\n") +
                        ( lot.getString("unSign"))
            }else{
                ("今天你还没有进行过抽签,请输入【抽签】再试试~")
            }
        }else{
            ("Lots init failed!")
        }
    }

    /**
     * 检查当前时间，换了天数就把记录抽签的Map清楚掉
     */
    private fun checkDay(){
        val dayTemp = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if(dayTemp>day){
            day = dayTemp
            lotMap.clear()
        }
    }

}