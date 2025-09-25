package dev.dead.reactivespringmongodb

import kotlinx.coroutines.reactive.asFlow
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.coRouter
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
class ReactiveSpringMongodbApplication {
    @Bean
    fun routes(customerRepository: CustomerRepository) = coRouter {
        GET("/customers") {
            ServerResponse.ok()
                .bodyAndAwait(
                    customerRepository
                        .findAll().asFlow()
                )
        }
    }
    //java way
//    @Bean
//    fun routes(customerRepository: CustomerRepository): RouterFunction<ServerResponse?> {
//
//            return route()
//                .GET("/customers", HandlerFunction<ServerResponse> {
//                    ServerResponse.ok().body(customerRepository.findAll())
//                })
//                .build();
//        }
}

fun main(args: Array<String>) {
    runApplication<ReactiveSpringMongodbApplication>(*args)
}
