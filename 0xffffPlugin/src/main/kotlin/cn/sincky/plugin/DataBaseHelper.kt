package cn.sincky.plugin

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object DataBaseHelper {
    lateinit var db: Connection
    private const val INIT_SQL  = "create table if not exists history " +
            "(uri text primary key not null," +
            "title text not null," +
            "author text not null," +
            "link text not null);"
    init {
        try {
            Class.forName("org.sqlite.JDBC");
            db = DriverManager.getConnection("jdbc:sqlite:${FFFFNotifierPluginMain.dataFolder.path}/history.db");
            Logger.info("Opened database successfully");
            execute(INIT_SQL)
            Logger.info("Database init successfully");
        } catch (e: Exception) {
            Logger.error(e.message);
        }
    }

    fun insertEntry(entry: Notifier.Entry){
        try{
            execute(
                "insert into history (uri,title,author,link) " +
                        "values ( '${entry.uri}', '${entry.title}', '${entry.author}', '${entry.link}');"
            )
        }catch (e : Exception){
            Logger.error(e.message)
        }
    }

    fun queryEntry(): List<Notifier.Entry>{
        val result = query("select * from history;")
        val list = mutableListOf<Notifier.Entry>()
        while (result.next()) {
            list.add(Notifier.Entry(result.getString(1),
                result.getString(2),
                result.getString(3),
                result.getString(4)))
        }
        result.close()
        Logger.info("size:${list.size}")
        return list
    }


    private fun execute(sql: String){
        val stmt = db.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    private fun query(sql: String) : ResultSet{
        val stmt = db.createStatement();
        return stmt.executeQuery(sql)
    }


}