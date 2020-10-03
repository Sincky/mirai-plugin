package cn.sincky.plugin

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import java.net.URL

object Notifier {
    private var runned = false

    private lateinit var publish: (List<Entry>) -> List<Entry>
    private val cacheEntry = mutableSetOf<Entry>()

    init {
        cacheEntry.addAll(DataBaseHelper.queryEntry())
        Logger.info("数据库帖子数量:${cacheEntry.size}")
    }

    fun subscribe(subject: (List<Entry>) -> List<Entry>) {
        this.publish = subject
        start()
    }

    private fun start() {
        Logger.info("计时器开始工作，间隔${INTERVAL / 1000 / 60}m")
        runned = true
        // new thread to check new post
        Thread(Runnable {
            while (runned) {
                Thread.sleep(INTERVAL)
                run()
            }
        }).start()
    }

    private fun run() {
        try {
            Logger.info("check new post")
            // check new post
            val feed = SyndFeedInput().build(XmlReader(URL(URL)))
            val newEntry = mutableListOf<Entry>()
            feed.entries.subList(0, 10).forEach {
                val entryTemp = it.toEntry()
                if (cacheEntry.add(entryTemp)) {
                    newEntry.add(entryTemp)
                }
            }
            Logger.info("newEntrySize:${newEntry.size}")
            if (newEntry.size > 0) {
                val successEntry = publish(newEntry)
                successEntry.forEach {
                    DataBaseHelper.insertEntry(it)
                    Logger.info("newEntry:${it.title},${it.link}")
                }
            }
            Logger.info("check new post : done")
        } catch (e: Throwable) {
            Logger.error(e)
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