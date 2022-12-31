package cn.sincky.mirai.notifier


import notifier.data.Entry
import notifier.util.Log
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object DataBaseHelper {
    private lateinit var db: Connection
    private const val INIT_SQL = "create table if not exists history " +
            "(uri text primary key not null," +
            "title text not null," +
            "author text not null," +
            "link text not null);"


    fun initDatabase(dbPath: String) {
        try {
            Class.forName("org.sqlite.JDBC")
            db = DriverManager.getConnection("jdbc:sqlite:${dbPath}");
            Log.info("Opened database successfully");
            execute(INIT_SQL)
            Log.info("Database init successfully");
        } catch (e: Exception) {
            Log.error(e.message);
        }
    }

    fun insertEntry(entry: Entry) {
        try {
            execute(
                "insert into history (uri,title,author,link) " +
                        "values ( '${entry.uri}', '${entry.title}', '${entry.author}', '${entry.link}');"
            )
        } catch (e: Exception) {
            Log.error(e.message)
        }
    }

    fun clearDb() {
        try {
            execute(
                "delete from history"
            )
        } catch (e: Exception) {
            Log.error(e.message)
        }
    }

    fun queryEntry(): List<Entry> {
        val result = query("select * from history;")
        val list = mutableListOf<Entry>()
        while (result.next()) {
            list.add(
                Entry(
                    result.getString(1),
                    result.getString(2),
                    result.getString(3),
                    result.getString(4)
                )
            )
        }
        result.close()
        Log.info("size:${list.size}")
        return list
    }


    private fun execute(sql: String) {
        val stmt = db.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    private fun query(sql: String): ResultSet {
        val stmt = db.createStatement();
        return stmt.executeQuery(sql)
    }


}