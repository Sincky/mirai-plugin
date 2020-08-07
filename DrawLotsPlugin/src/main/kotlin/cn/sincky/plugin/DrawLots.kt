package cn.sincky.plugin

import java.util.*

class DrawLots(private val lotsList: List<Lot>) {

    data class Lot(val uid:String,val sign:String,val unSign:String)

    private var lotMap = mutableMapOf<Long,Int>()
    private var day: Int

    /**
     * 抽签类初始化
     */
    init{
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }

    /**
     *  抽签
     */
    fun sign(id : Long) : String {
        return if (lotsList.isNotEmpty()){
            checkDay()
            if(lotMap.contains(id)){
                "你已经抽过签了,请输入【解签】查看"
            }else{
                val index = lotsList.indices.random()
                lotMap[id] = index
                val lot = lotsList[index]
                "签位：" + lot.uid + "\n" +
                        lot.sign + "\n" +
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
        return if (lotsList.isNotEmpty()){
            checkDay()
            if(lotMap.contains(id)){
                val index: Int = lotMap.getValue(id)
                val lot = lotsList[index]
                ("解签：" + lot.uid + "\n") +
                        ( lot.unSign)
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
        if(dayTemp!=day){
            day = dayTemp
            lotMap.clear()
        }
    }

}

