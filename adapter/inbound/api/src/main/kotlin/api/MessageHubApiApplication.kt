package com.hjm.messagehub.api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class MessageHubApiApplication

fun main(args: Array<String>) {
    SpringApplication.run(MessageHubApiApplication::class.java, *args)
}
