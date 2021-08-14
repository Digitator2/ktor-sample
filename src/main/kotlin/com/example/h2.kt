package com.example.h2


import com.example.IDb
import io.ktor.application.*
import java.net.URI
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import kotlin.math.log

// http://www.h2database.com/html/quickstart.html
// http://www.h2database.com/html/tutorial.html#connecting_using_jdbc
// http://h2database.com/html/commands.html


//conn = DriverManager.getConnection("jdbc:h2:~/test", "", "")  // in file
//conn = DriverManager.getConnection("jdbc:h2:mem:", "", "")  // in memory

// *** H2 Sample
/*
var stm = conn.createStatement()
stm.execute("drop table if exists users;")
stm.execute("create table users (id int primary key auto_increment not null  , name varchar (255));")
// create table if not exists my(id int auto_increment primary key,s text);

//stm = conn.createStatement()
stm.execute("insert into users values(188,'one')")



val rs = stm.executeQuery("select * from users")
if (rs.next()) {
    // rs.getInt("id")
    // rs.getString("name")
    println(rs.getInt(1))
}
*/

/*
var url = "jdbc:h2:mem:"
try {
    DriverManager.getConnection(url).use { con ->
        con.createStatement().use { stm ->
            stm.executeQuery("SELECT 1+1").use { rs ->
                if (rs.next()) {
                    println(rs.getInt(1))
                }
            }
        }
    }
} catch (ex: SQLException) {
    //val lgr: Unit = Logger.getLogger(JavaSeH2Memory::class.java.getName())
    //lgr.log(Level.SEVERE, ex.message, ex)
    println(ex)
}*/

//Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

object Db : IDb {

    override fun getConnection(): Connection {
        Class.forName("org.h2.Driver")
        return DriverManager.getConnection("jdbc:h2:mem:", "", "")
    }

    override fun Statement.createTableUsers():Boolean {

        return this.execute("create table users (id int primary key auto_increment not null, name varchar (255));")

    }

    override fun Statement.insert(tableName:String, fields:Array<String>, values:Array<String>):Boolean {
        //var str:String = ""
        val strFields = fields.fold("") { a, e -> a + "$e," }.trimEnd(',')
        val strValues = values.fold("") { a, e -> a + "\'$e\'," }.trimEnd(',')

        val str = "insert into $tableName($strFields) values($strValues);"
        println("sss " +str)
        return this.execute(str)
    }

}


