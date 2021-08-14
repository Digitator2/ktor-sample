package com.example

import java.sql.*

interface IDb {

    fun getConnection(): Connection

    fun Statement.createTableUsers():Boolean

    fun Statement.insert(tableName:String, fields:Array<String>, values:Array<String>):Boolean

}