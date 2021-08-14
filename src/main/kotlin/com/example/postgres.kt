package com.example.postgres

import io.ktor.application.*
import java.net.URI
import java.sql.*

import com.example.Db

//conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:8080/test", "postgres", "")  //
//conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1/test", "postgres", "")  //


//val URL_DB = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://127.0.0.1/test"
//val USERNAME = System.getenv("DATABASE_USERNAME") ?: "postgres"
//val PASSWORD = System.getenv("DATABASE_PASSWORD") ?: ""
//conn = DriverManager.getConnection(URL_DB, USERNAME, PASSWORD)  //

// https://devcenter.heroku.com/articles/heroku-postgresql#connecting-in-java

object Pg : Db {

    override fun getConnection():Connection {

        val localStringConn = "postgres://postgres:@127.0.0.1:80/test"

        var sslUse = "?sslmode=require\""
        val env = System.getenv("DATABASE_URL")

        val dbUri = URI ( if(env == null){
            sslUse = ""
            localStringConn
        }else{ env } )

        //val dbUri = URI(System.getenv("DATABASE_URL") ?: localStringConn )

        //val dbUri = URI("postgres://vjvxwkhqjuamfz:3c0b80b92e0918fb1bccac2cb7ff27109c954d98a6974d404d00a053284e3a04@ec2-3-237-55-151.compute-1.amazonaws.com:5432/dcqfd4t5v0394n")

        val username: String = dbUri.getUserInfo().split(":").get(0)
        val password: String = dbUri.getUserInfo().split(":").get(1)
        val dbUrl = "jdbc:postgresql://" +
                dbUri.getHost() + (if(sslUse!="") { ":" + dbUri.getPort() } else "") + dbUri.getPath().toString() + sslUse


        //log.info("dbUrl=$dbUrl  username=$username  password=$password")

        //println("$dbUrl $username $password ")
        return DriverManager.getConnection(dbUrl, username, password)
    }

    override fun Statement.createTableUsers():Boolean {

        return this.execute("create table users (id serial primary key , name varchar (255));")

    }

    override fun Statement.insert(tableName:String, fields:Array<String>, values:Array<String>):Boolean {
        //var str:String = ""
        val strFields = fields.fold("") { a, e -> a + "$e," }.trimEnd(',')
        val strValues = values.fold("") { a, e -> a + "\'$e\'," }.trimEnd(',')
        return this.execute("insert into $tableName($strFields) values($strValues) ")
    }

}