package com.hjm.messagehub.scheduler

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class MessageHubSchedulerApplication

fun main(args: Array<String>) {
    SpringApplication.run(MessageHubSchedulerApplication::class.java, *args)
}
