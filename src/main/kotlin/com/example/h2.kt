package com.example.h2

import com.example.Db
import java.net.URI
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

// http://www.h2database.com/html/quickstart.html
// http://www.h2database.com/html/tutorial.html#connecting_using_jdbc
// http://h2database.com/html/commands.html


//conn = DriverManager.getConnection("jdbc:h2:~/test", "", "")  // in file
//conn = DriverManager.getConnection("jdbc:h2:mem:", "", "")  // in memory

object H2 : Db {

    override fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:h2:mem:", "", "")
    }

    override fun Statement.createTableUsers():Boolean {

        return this.execute("create table users (id int primary key auto_increment not null, name varchar (255));")

    }

    override fun Statement.insert(tableName:String, fields:Array<String>, values:Array<String>):Boolean {
        //var str:String = ""
        val strFields = fields.fold("") { a, e -> a + "$e," }.trimEnd(',')
        val strValues = values.fold("") { a, e -> a + "\'$e\'," }.trimEnd(',')
        return this.execute("insert into $tableName($strFields) values($strValues);")
    }

}


