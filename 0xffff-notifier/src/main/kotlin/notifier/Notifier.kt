package cn.sincky.mirai.notifier

import cn.sincky.mirai.notifier.main.NotifierPlugin
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import kotlinx.coroutines.Runnable
import java.net.URL

object Notifier {
    private var runned = false

    private lateinit var publish: (List<Entry>) -> List<Entry>
    private val cacheEntry = mutableSetOf<Entry>()

    init {
        cacheEntry.addAll(DataBaseHelper.queryEntry())
        NotifierPlugin.logger.info("数据库帖子数量:${cacheEntry.size}")
    }

    fun subscribe(subject: (List<Entry>) -> List<Entry>) {
        this.publish = subject
        start()
    }

    private fun start() {
        NotifierPlugin.logger.info("计时器开始工作，间隔${CHECK_INTERVAL / 1000 / 60}m")
        runned = true
        // new thread to check new post
        Thread(Runnable {
            while (runned) {
                Thread.sleep(CHECK_INTERVAL)
                run()
            }
        }).start()
    }

    private fun run() {
        try {
            NotifierPlugin.logger.info("check new post")
            // check new post
            val feed = SyndFeedInput().build(XmlReader(URL(FF_URL)))
            val newEntry = mutableListOf<Entry>()
            val nums = if (feed.entries.size < 10) {
                feed.entries.size
            } else {
                10
            }
            feed.entries.subList(0, nums).forEach {
                val entryTemp = it.toEntry()
                if (cacheEntry.add(entryTemp)) {
                    newEntry.add(entryTemp)
                }
            }

            NotifierPlugin.logger.info("newEntrySize:${newEntry.size}")
            if (newEntry.size > 0) {
                val successEntry = publish(newEntry)
                successEntry.forEach {
                    DataBaseHelper.insertEntry(it)
                    NotifierPlugin.logger.info("successEntry:${it.title},${it.link}")
                }
            }
            NotifierPlugin.logger.info("check new post : done")
        } catch (e: Throwable) {
            NotifierPlugin.logger.error(e)
        }
    }

    private fun SyndEntry.toEntry(): Entry {
        return Entry(uri, title, author, link)
    }

    fun stop() {
        runned = false
    }

    data class Entry(val uri: String, val title: String, val author: String, val link: String)
}