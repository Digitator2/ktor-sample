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
import java.lang.StringBuilder
import java.sql.*
import java.util.concurrent.atomic.AtomicInteger


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

    // http://www.h2database.com/html/quickstart.html
    // http://www.h2database.com/html/tutorial.html#connecting_using_jdbc
    // http://h2database.com/html/commands.html
    //conn = DriverManager.getConnection("jdbc:h2:~/test", "", "")  // "jdbc:h2:mem:"
    //conn = DriverManager.getConnection("jdbc:h2:mem:", "", "")  //

    //conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:8080/test", "postgres", "")  //
    //conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1/test", "postgres", "")  //


    val URL_DB = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://127.0.0.1/test"
    val USERNAME = System.getenv("DATABASE_USERNAME") ?: "postgres"
    val PASSWORD = System.getenv("DATABASE_PASSWORD") ?: ""
    conn = DriverManager.getConnection(URL_DB, USERNAME, PASSWORD)  //


// *** postgres Sample

    var stm = conn.createStatement()
    //stm.execute("begin")
    stm.execute("drop table if exists users;")
    stm.execute("create table users (id serial primary key , name varchar (255));")
    // create table if not exists my(id int auto_increment primary key,s text);

    //stm.execute("commit ")
    //stm.execute("begin ")

    //stm = conn.createStatement()
    //stm.execute("insert into users values(188,'one')")

    //stm.execute("rollback ")



    val rs = stm.executeQuery("select * from users")
    if (rs.next()) {
        // rs.getInt("id")
        // rs.getString("name")
        println(rs.getInt(1))
    }

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

routing {

//        get{
//            println("${call.receiveText()}")
//        }

    authenticate("auth-user") {
        get("/") {
            //println("ddd ${call.receiveText()}")
            call.respondText("Hello World!")

        }
    }

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
        //val price = 0

        //val body = call.receiveText() // из тела запроса в строку, ошибки нет, если не заполнено.
        //val body = call.receive<BData>() // из тела запроса, в объект пишем, по совпадающим полям ! исключение возможно, если не подходит

        val body =
            try { call.receive<BData>() } catch (e:Exception) {
               BData("","", SData(""))
            }// из тела запроса, в объект пишем, по совпадающим полям ! исключение возможно, если не подходит

        //println(body.toString())
        call.respondText("uri: $uri \n  data: $data \n  RT: $body \n price: $price")
        //call.respondText("uri: $uri    data: $data")

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

        val nameNewUser = call.request.queryParameters["name"] ?: return@post call.respond(HttpStatusCode.NoContent)

        try {
            //conn.autoCommit = false

            stm.execute("begin")

            stm.execute( "insert into users(name) values(\'$nameNewUser\');" )

            val row = stm.executeQuery("select MAX(id) from users" )

            //stm.execute("commit ")

            //conn.commit();

            if (row.next()) {
                val r = row.getInt(1)
                stm.execute("commit ")
                call.respondText("{id:$r}")

            } else throw Exception("cannot get id for new created user")

        }catch (e:Exception){
            stm.execute("rollback")
            //conn.rollback();
            call.respond(HttpStatusCode.InternalServerError)
        }

        // insert into my values(default,'foo');

        // возвращает ID созданного пользователя

        //call.respondText ()
    }

    get ("/dump/"){


        val row = stm.executeQuery("script to '/dump.txt'")
        //val fileContent = this::class.java.getResource("/dump.txt").readText()
        //val fileContent = this::javaClass.getResource("/dump.txt").readText()

        val fileContent = if(row.next()) { row.getString(1) } else ""
        call.respondText(fileContent)

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
