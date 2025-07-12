package com.foodchain.autotroph

import com.foodchain.autotroph.config.configureRouting
import com.foodchain.autotroph.config.configureSerialization
import com.foodchain.autotroph.config.configureMonitoring
import com.foodchain.autotroph.config.configureDI
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureDI()
    configureSerialization()
    configureMonitoring()
    configureRouting()
}
