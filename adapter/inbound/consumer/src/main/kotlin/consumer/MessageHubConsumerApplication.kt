package com.hjm.messagehub.consumer

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class MessageHubConsumerApplication

fun main(args: Array<String>) {
    SpringApplication.run(MessageHubConsumerApplication::class.java, *args)
}
