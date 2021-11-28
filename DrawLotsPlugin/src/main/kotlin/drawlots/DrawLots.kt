package cn.sincky.mirai.drawlots

import cn.sincky.mirai.drawlots.main.DrawLotsPlugin
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlList
import net.mamoe.yamlkt.YamlMap
import java.util.*

class DrawLots(private val lots: List<Lot>) {

    private var lotMap = mutableMapOf<Long, Int>()
    private var day: Int

    /**
     * 抽签类初始化
     */
    init {
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }

    /**
     *  抽签
     */
    fun sign(id: Long): String {
        return if (lots.isNotEmpty()) {
            checkDay()
            if (lotMap.contains(id)) {
                "你已经抽过签了,请输入【解签】查看"
            } else {
                val index = lots.indices.random()
                lotMap[id] = index
                val lot = lots[index]
                "签位：" + lot.uid + "\n" +
                        lot.sign + "\n" +
                        "解签请发送【解签】"
            }
        } else {
            "Lots init failed!"
        }
    }

    /**
     *  解签
     */
    fun unSign(id: Long): String {
        return if (lots.isNotEmpty()) {
            checkDay()
            if (lotMap.contains(id)) {
                val index: Int = lotMap.getValue(id)
                val lot = lots[index]
                ("解签：" + lot.uid + "\n") +
                        (lot.unSign)
            } else {
                ("今天你还没有进行过抽签,请输入【抽签】再试试~")
            }
        } else {
            ("Lots init failed!")
        }
    }

    /**
     * 检查当前时间，换了天数就把记录抽签的Map清楚掉
     */
    private fun checkDay() {
        val dayTemp = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if (dayTemp != day) {
            day = dayTemp
            lotMap.clear()
        }
    }

    companion object {
        fun loadDrawLotsData(yamlStr: String): DrawLots? {
            return try {
                val lots = readLots(yamlStr)
                DrawLotsPlugin.logger.info("抽取数据大小${lots.size}")
                DrawLotsPlugin.logger.info("initDrawLotsData success")
                DrawLots(lots)
            } catch (t: Throwable) {
                DrawLotsPlugin.logger.error(t)
                null
            }
        }

        private fun readLots(yamlStr: String): List<Lot> {
            val yamlMap = Yaml.Default.decodeYamlFromString(yamlStr) as YamlMap
            val yamlList = yamlMap["Lots"] as YamlList
            val lots = mutableListOf<Lot>()

            yamlList.forEach {

                val lotMap = it as YamlMap
                val lot = Lot(
                    lotMap["uid"].toString(),
                    lotMap["sign"].toString(),
                    lotMap["unSign"].toString()
                )
                lots.add(lot)
            }

            return lots;
        }
    }

}

data class Lot(val uid: String, val sign: String, val unSign: String)