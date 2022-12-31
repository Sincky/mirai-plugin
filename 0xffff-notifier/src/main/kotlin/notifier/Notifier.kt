package cn.sincky.mirai.notifier

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import kotlinx.coroutines.Runnable
import notifier.data.Entry
import notifier.data.toEntry
import notifier.util.Log
import org.jetbrains.annotations.NotNull
import java.net.URL

object Notifier {
    private var running = false
    private var interval = CHECK_INTERVAL
    private lateinit var publish: (List<Entry>) -> List<Entry>
    private val cacheEntry = mutableSetOf<Entry>()

    init {
        cacheEntry.addAll(DataBaseHelper.queryEntry())
        Log.info("数据库帖子数量:${cacheEntry.size}")
    }

    fun subscribe(subject: (List<Entry>) -> List<Entry>) {
        this.publish = subject
    }

    fun setInterval(interval: Long) {
        this.interval = interval
    }

    fun start() {
        Log.info("计时器开始工作，间隔${interval / 1000 / 60}m")
        running = true
        // new thread to check new post
        Thread(Runnable {
            while (running) {
                Thread.sleep(interval)
                run()
            }
        }).start()
    }

    @NotNull
    private fun getTopicEntries(): List<Entry> {
        val newEntry = mutableListOf<Entry>()
        val feed = SyndFeedInput().build(XmlReader(URL(FF_DISCUSSIONS_URL)))
        Log.info("feed size:${feed.entries.size}")
        val num = if (feed.entries.size < 10) {
            feed.entries.size
        } else {
            10
        }
        feed.entries.subList(0, num).forEach {
            try {
                val entryTemp = it.toEntry()
                if (cacheEntry.add(entryTemp)) {
                    newEntry.add(entryTemp)
                }
            } catch (t: Throwable) {
                Log.error(t.message)
            }
        }
        return newEntry
    }

    public fun initData() {
        val entries = getTopicEntries()
        Log.info("init entry size:${entries.size}")
        if (entries.isNotEmpty()) {
            entries.forEach {
                DataBaseHelper.insertEntry(it)
                Log.info("new entry:${it.author}, ${it.title}, ${it.link}")
            }
        }
    }

    private fun run() {
        try {
            Log.info("check new post")
            // check new post
            val entries = getTopicEntries()

            Log.info("newEntrySize:${entries.size}")
            if (entries.isNotEmpty()) {
                val successEntry = publish(entries)
                successEntry.forEach {
                    DataBaseHelper.insertEntry(it)
                    Log.info("new entry:${it.author}, ${it.title}, ${it.link}")
                }
            }
            Log.info("check new post : done")
        } catch (e: Throwable) {
            Log.error(e.message)
        }
    }

    fun stop() {
        running = false
    }

}