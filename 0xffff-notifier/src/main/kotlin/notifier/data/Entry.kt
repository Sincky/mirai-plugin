package notifier.data

import com.rometools.rome.feed.synd.SyndEntry

data class Entry(val uri: String, val title: String, val author: String, val link: String)

fun SyndEntry.toEntry(): Entry {
    return Entry(uri, title, author, link)
}
