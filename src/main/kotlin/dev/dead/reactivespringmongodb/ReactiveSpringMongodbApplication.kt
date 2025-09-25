package dev.dead.reactivespringmongodb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@Document
data class Customer(
    @Id val id: String,
    val name: String,
)
interface CustomerRepository : ReactiveCrudRepository<Customer, String>
@RestController
class CustomerRestController(val customerRepository: CustomerRepository) {
    @GetMapping("/customers")
    fun findAll(): Flux<Customer> = customerRepository.findAll()
}

@SpringBootApplication
class ReactiveSpringMongodbApplication

fun main(args: Array<String>) {
    runApplication<ReactiveSpringMongodbApplication>(*args)
}
