package dev.dead.reactivespringmongodb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveSpringMongodbApplication

fun main(args: Array<String>) {
    runApplication<ReactiveSpringMongodbApplication>(*args)
}
