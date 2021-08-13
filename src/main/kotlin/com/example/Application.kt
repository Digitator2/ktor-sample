package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.application.*
import java.sql.DriverManager


fun main(args: Array<String>): Unit {
    Class.forName("org.postgresql.Driver");
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureRouting()
}


/*
fun main() {

    //Class.forName("org.h2.Driver")
    //Class.forName("org.postresql.Driver")
    Class.forName("org.postgresql.Driver");
    //DriverManager.register(new org.postgresql.Driver());

    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        configureRouting()
        configureTemplating()
    }.start(wait = true)
}
*/