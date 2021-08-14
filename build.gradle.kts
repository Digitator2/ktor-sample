val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.21"
    //id("org.jetbrains.kotlin.plugin.jpa") version "1.5.21"
}

group = "com.example"
version = "0.0.1"
application {
    //mainClass.set("com.example.ApplicationKt")
    mainClass.set("io.ktor.server.netty.EngineMain")
    //applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")

    implementation( "io.ktor:ktor-gson:$ktor_version")

    implementation("io.ktor:ktor-auth:$ktor_version")

    testImplementation("com.h2database:h2:1.4.200")
    implementation("com.h2database:h2:1.4.200")

    implementation("org.postgresql:postgresql:42.2.23" )

}

tasks.create("stage") {
    dependsOn("installDist")
}