package com.hjm.messagehub.api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class MessageHubApplication

fun main(args: Array<String>) {
    SpringApplication.run(MessageHubApplication::class.java, *args)
}
