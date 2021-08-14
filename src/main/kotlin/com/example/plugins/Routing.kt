package com.example.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.dom.document
import java.net.URI
import java.sql.*

/*
import com.example.postgres.*
import com.example.postgres.Db.getConnection
import com.example.postgres.Db.createTableUsers
import com.example.postgres.Db.insert
*/


import com.example.h2.*
import com.example.h2.Db
import com.example.h2.Db.getConnection
import com.example.h2.Db.createTableUsers
import com.example.h2.Db.insert



data class BData(val a:String, val b:String, val other:SData)
data class SData(val text:String)

data class User(val id:Int, val name:String)

fun Application.configureRouting() {

    install(ContentNegotiation) {
        gson {
            //setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }

    install(IgnoreTrailingSlash) // игнор "/"

    install(Authentication) {
        /*
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                if (credentials.name == "jetbrains" && credentials.password == "foobar") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

         */
        basic("auth-basic") {
            //request = true
            //realm = "Access to the '/' path"
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "321") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
        basic("auth-user") {
            //request = true
            //realm = "Access to the '/' path"
            validate { credentials ->
                if (credentials.name == "user" && credentials.password == "123") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

    }

    // Transactions
    // https://proselyte.net/tutorials/jdbc/transactions/
    // https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html

    //install(DefaultHeaders) {
      //  header("data", "Some value")
    //}

    // https://github.com/ktorio/ktor-samples/blob/main/generic/samples/httpbin/src/HttpBinApplication.kt

    var conn: Connection? = null
    //val stmt: Statement? = null



    log.info("1")

    conn = Db.getConnection()

    log.info("connected!")

// *** postgres Sample

    val stm = conn.createStatement()
    stm.execute("drop table if exists users;")
    stm.createTableUsers()
    stm.execute("insert into users values(188,'one')")
    stm.execute("insert into users(name) values('two')")

    log.info("st1 passed")

    val rs = stm.executeQuery("select * from users")
    if (rs.next()) {
        // rs.getInt("id")
        // rs.getString("name")
        println(rs.getInt(1))
    }

    log.info("st2 passed")


routing {

    //authenticate("auth-user") {
        get("/") {
            //println("ddd ${call.receiveText()}")
            call.respondText("Hello World!")

        }
    //}

    //authenticate("auth-form") {
    authenticate("auth-basic") {
        get("/some/") {
            call.respondText("ssss")
        }
    }

    post("/some/") {
        val uri = call.request.uri //    "/some"
        val data = call.request.header("data") // берем произвольную переменную из заголовка
        val price = call.request.queryParameters["price"] //  берем параметр из URL /some?price=1000

        //val body = call.receiveText() // из тела запроса в строку, ошибки нет, если не заполнено.
        //val body = call.receive<BData>() // из тела запроса, в объект пишем, по совпадающим полям ! исключение возможно, если не подходит

        val body =
            try { call.receive<BData>() } catch (e:Exception) {
               BData("","", SData(""))
            }// из тела запроса, в объект пишем, по совпадающим полям ! исключение возможно, если не подходит

        call.respondText("uri: $uri \n  data: $data \n  RT: $body \n price: $price")
    }

    get("/users/"){

        // get all users
        val row = stm.executeQuery("select id, name from users")

        val sb = StringBuffer()
        //val sb = StringBuilder()
        sb.append("{users:[")
        while(row.next()){
            val userId = row.getInt(1)
            val userName = row.getString(2)

            sb.append( "{id: $userId, name: $userName}," )
            //r += "{id: $userId, name: $userName},"
        }
        //if(sb.lastOrNull()?: ' ' == ',') sb.setCharAt(sb.count()-1,' ')

        sb.lastOrNull()?.also {
            sb.setCharAt(sb.count()-1,' ')
        }

        //sb.removeSuffix(",,")
        //sb.removePrefix(",,")
        sb.append( "]}" )
        call.respondText(sb.toString())
    }

    get("/users/{id}"){

        // get user with id

        val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)

        val row = stm.executeQuery("select id, name from users where id = $id")

        try {
            if (row.next()) {
                val userId = row.getInt(1)
                val userName = row.getString(2)
                call.respondText("{id:$userId, name:$userName}")
            } else call.respond(HttpStatusCode.BadRequest)
        }catch (e:Exception){
            call.respond(HttpStatusCode.InternalServerError)
        }

    }

    post("/users/"){

        println("u1")

        val nameNewUser = call.request.queryParameters["name"] ?: return@post call.respond(HttpStatusCode.NoContent)

        println("u2")

        try {

            println("u3")

            //conn.autoCommit = false

            //stm.execute("begin")

            //stm.execute( "insert into users(name) values(\'$nameNewUser\');" )
            val ri = stm.insert("users",arrayOf("name"), arrayOf("$nameNewUser"))

            //log("result: ")
            //if(!ri) throw Exception("abcdef")
            println("insert result $ri")

            val row = stm.executeQuery("select MAX(id) from users" )

            //stm.execute("commit ")

            //conn.commit();

            if (row.next()) {
                val r = row.getInt(1)
                //stm.execute("commit ")
                call.respondText("{id:$r}")

            } else throw Exception("cannot get id for new created user")

        }catch (e:Exception){
            //stm.execute("rollback")
            //conn.rollback();
            println("${e.message}")
            call.respond(HttpStatusCode.InternalServerError)
        }

        // insert into my values(default,'foo');

        // возвращает ID созданного пользователя

        //call.respondText ()
    }

    get ("/dump/"){


        //val row = stm.executeQuery("script to '/dump.txt'")
        //val fileContent = if(row.next()) { row.getString(1) } else ""
        //call.respondText(fileContent)

        call.respondText("not implemented")

    }

    get("/some/{id}/model/{par}") {

        println("1")
        val id = call.parameters["id"] ?: "None"
        val par = call.parameters["par"] ?: "None"
        //call.respondText("come get some {$id}")

        call.respondText ("pars: $id $par")

    }


    get("/page/{id}") {
        println("1")
        val id = call.parameters["id"] ?: "None"
        //call.respondText("come get some {$id}")

        call.respondHtml {
            body {
                b {
                    +"Привет"
                    p {}
                    +"из страницы $id !"

                    val nid = id.toIntOrNull()

                    for (i in 0..( nid?:10 )){
                        b {  +"Немножко текста "}

                        button { //+"sd"
                            onClick ="window.alert(\"test $i  $id\")"
                            text("Жми $i $id")

                        }

                        a("https://kotlinlang.org") {
                            target = ATarget.blank
                            +"Main site"
                        }

                            //onClickFunction = { println("clicked") }
                        p{}
                    }


                    document { val r= create.div("panel") {  +"oooooo"}
                        r.setAttribute("panel","1")
                        //r.
                        //getElementById("panel").append { +"asd " }
                        //getElementById()
                    }

                    input {
                        text("inputtt")
                    }

                    form("/v1/settings", method = FormMethod.post) {
                        input(type = InputType.text, name = "key")
                        input(type = InputType.text, name = "value")
                        input(InputType.submit)
                    }

                    table {

                        for (i in 0..(nid ?: 10)) {

                            tr {

                                td {
                                    +"Колонка 1"
                                }
                                td("cc") {
                                    i { +"Колонка 2" }
                                }

                            }

                        }

                    }
                }
            }
        }
    }


}
}
